package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * utility class for event processing
 * */
class EventRepository {
    private String authHeaderString;
    private RudderConfig config;
    private DBPersistentManager dbManager;
    private RudderServerConfigManager configManager;
    private Map<String, RudderIntegration> integrationOperationsMap = new HashMap<>();
    private final ArrayList<RudderMessage> eventReplayMessage = new ArrayList<>();
    private Map<String, RudderClient.Callback> integrationCallbacks = new HashMap<>();

    private boolean initiated = false;

    /*
     * constructor to be called from RudderClient internally.
     * -- tasks to be performed
     * 1. persist the value of config
     * 2. initiate RudderElementCache
     * 3. initiate DBPersistentManager for SQLite operations
     * 4. initiate RudderServerConfigManager
     * 5. start processor thread
     * 6. initiate factories
     * */
    EventRepository(Application _application, String _writeKey, RudderConfig _config) {
        // 1. set the values of writeKey, config
        try {
            RudderLogger.logDebug(String.format("EventRepository: constructor: writeKey: %s", _writeKey));
            this.authHeaderString = Base64.encodeToString((String.format(Locale.US, "%s:", _writeKey)).getBytes("UTF-8"), Base64.DEFAULT);
            RudderLogger.logDebug(String.format("EventRepository: constructor: authHeaderString: %s", this.authHeaderString));
        } catch (UnsupportedEncodingException ex) {
            RudderLogger.logError(ex);
        }
        this.config = _config;
        RudderLogger.logDebug(String.format("EventRepository: constructor: %s", this.config.toString()));

        try {
            // 2. initiate RudderElementCache
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderElementCache");
            RudderElementCache.initiate(_application);

            // 3. initiate DBPersistentManager for SQLite operations
            RudderLogger.logDebug("EventRepository: constructor: Initiating DBPersistentManager");
            this.dbManager = DBPersistentManager.getInstance(_application);

            // 4. initiate RudderServerConfigManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderServerConfigManager");
            this.configManager = RudderServerConfigManager.getInstance(_application, _writeKey, _config);

            // 5. start processor thread
            RudderLogger.logDebug("EventRepository: constructor: Starting Processor thread");
            Thread processorThread = new Thread(getProcessorRunnable());
            processorThread.start();

            // 6. initiate factories
            RudderLogger.logDebug("EventRepository: constructor: Initiating factories");
            this.initiateFactories();

            this.initiated = true;
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    private boolean isFactoryInitialized = false;

    private void initiateFactories() {
        // initiate factory initialization after 10s
        // let the factories capture everything they want to capture
        if (config == null || config.getFactories() == null || config.getFactories().isEmpty()) {
            RudderLogger.logInfo("EventRepository: initiateFactories: No native SDK factory found");
            isFactoryInitialized = true;
            return;
        }
        // initiate factories if client is initialized properly
        final RudderClient client = RudderClient.getInstance();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int retryCount = 0;
                    while (!isFactoryInitialized && retryCount <= 5) {
                        RudderServerConfig serverConfig = configManager.getConfig();

                        if (serverConfig != null && serverConfig.source != null && serverConfig.source.destinations != null) {
                            List<RudderServerDestination> destinations = serverConfig.source.destinations;

                            if (destinations.isEmpty()) {
                                RudderLogger.logInfo("EventRepository: initiateFactories: No destination found in the config");
                            } else {
                                Map<String, RudderServerDestination> destinationConfigMap = new HashMap<>();
                                for (RudderServerDestination destination : destinations) {
                                    destinationConfigMap.put(destination.destinationDefinition.displayName, destination);
                                }

                                for (RudderIntegration.Factory factory : config.getFactories()) {
                                    // if factory is present in the config
                                    String key = factory.key();
                                    if (destinationConfigMap.containsKey(key)) {
                                        RudderServerDestination destination = destinationConfigMap.get(key);
                                        // initiate factory if destination is enabled from the dashboard
                                        if (destination != null && destination.isDestinationEnabled) {
                                            Object destinationConfig = destination.destinationConfig;
                                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: Initiating %s native SDK factory", key));
                                            RudderIntegration<?> nativeOp = factory.create(destinationConfig, client, config);
                                            RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: Initiated %s native SDK factory", key));
                                            integrationOperationsMap.put(key, nativeOp);
                                            if (integrationCallbacks.containsKey(key)) {
                                                Object nativeInstance = nativeOp.getUnderlyingInstance();
                                                RudderClient.Callback callback = integrationCallbacks.get(key);
                                                if (nativeInstance != null && callback != null) {
                                                    RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: Callback for %s factory invoked", key));
                                                    callback.onReady(nativeInstance);
                                                } else {
                                                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: Callback for %s factory is null", key));
                                                }
                                            }
                                        } else {
                                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: destination was null or not enabled for %s", key));
                                        }
                                    } else {
                                        RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: %s is not present in configMap", key));
                                    }
                                }
                            }

                            isFactoryInitialized = true;
                            synchronized (eventReplayMessage) {
                                RudderLogger.logDebug("EventRepository: initiateFactories: replaying old messages with factory");
                                ArrayList<RudderMessage> tempList = new ArrayList<>(eventReplayMessage);
                                if (!tempList.isEmpty()) {
                                    for (RudderMessage message : tempList) {
                                        makeFactoryDump(message);
                                    }
                                }
                                eventReplayMessage.clear();
                            }
                        } else {
                            retryCount += 1;
                            RudderLogger.logDebug("EventRepository: initiateFactories: retry count: " + retryCount);
                            Thread.sleep(10000);
                        }
                    }
                } catch (Exception ex) {
                    RudderLogger.logError(ex);
                }
            }
        }).start();
    }

    private Runnable getProcessorRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // initiate sleepCount
                int sleepCount = 0;

                // initiate lists for messageId and message
                ArrayList<Integer> messageIds = new ArrayList<>();
                ArrayList<String> messages = new ArrayList<>();

                while (true) {
                    try {
                        // clear lists for reuse
                        messageIds.clear();
                        messages.clear();

                        // get current record count from db
                        int recordCount = dbManager.getDBRecordCount();
                        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: DBRecordCount: %d", recordCount));
                        // if record count exceeds threshold count, remove older events
                        if (recordCount > config.getDbCountThreshold()) {
                            // fetch extra old events
                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: OldRecordCount: %d", (recordCount - config.getDbCountThreshold())));
                            dbManager.fetchEventsFromDB(messageIds, messages, recordCount - config.getDbCountThreshold());
                            // remove events
                            dbManager.clearEventsFromDB(messageIds);
                            // clear lists for reuse
                            messageIds.clear();
                            messages.clear();
                        }

                        // fetch enough events to form a batch
                        RudderLogger.logDebug("Fetching events to flush to sever");
                        dbManager.fetchEventsFromDB(messageIds, messages, config.getFlushQueueSize());
                        // if there are enough events to form a batch and flush to server
                        // OR
                        // sleepTimeOut seconds has elapsed since last successful flush and
                        // we have at least one event to flush to server
                        if (messages.size() >= config.getFlushQueueSize() || (!messages.isEmpty() && sleepCount >= config.getSleepTimeOut())) {
                            // form payload JSON form the list of messages
                            String payload = getPayloadFromMessages(messages);
                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: payload: %s", payload));
                            if (payload != null) {
                                // send payload to server if it is not null
                                String response = flushEventsToServer(payload);
                                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: processor: ServerResponse: %s", response));
                                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: processor: EventCount: %d", messages.size()));
                                // if success received from server
                                if (response != null && response.equalsIgnoreCase("OK")) {
                                    // remove events from DB
                                    dbManager.clearEventsFromDB(messageIds);
                                    // reset sleep count to indicate successful flush
                                    sleepCount = 0;
                                }
                            }
                        }
                        // increment sleepCount to track total elapsed seconds
                        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: SleepCount: %d", sleepCount));
                        sleepCount += 1;
                        // retry entire logic in 1 second
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        RudderLogger.logError(ex);
                    }
                }
            }
        };
    }

    /*
     * create payload string from messages list
     * - we created payload from individual message json strings to reduce the complexity
     * of deserialization and forming the payload object and creating the json string
     * again from the object
     * */
    private String getPayloadFromMessages(ArrayList<String> messages) {
        try {
            RudderLogger.logDebug("EventRepository: getPayloadFromMessages: recordCount: " + messages.size());
            String sentAtTimestamp = Utils.getTimeStamp();
            RudderLogger.logDebug("EventRepository: getPayloadFromMessages: sentAtTimestamp: " + sentAtTimestamp);
            // get string builder
            StringBuilder builder = new StringBuilder();
            // append initial json token
            builder.append("{");
            // append sent_at time stamp
            builder.append("\"sentAt\":\"").append(sentAtTimestamp).append("\",");
            // initiate batch array in the json
            builder.append("\"batch\": [");
            // loop through messages list and add in the builder
            for (int index = 0; index < messages.size(); index++) {
                String message = messages.get(index);
                // strip last ending object character
                message = message.substring(0, message.length() - 1);
                // add sentAt time stamp
                message = String.format("%s,\"sentAt\":\"%s\"}", message, sentAtTimestamp);
                // finally add message string to builder
                builder.append(message);
                // if not last item in the list, add a ","
                if (index != messages.size() - 1) {
                    builder.append(",");
                }
            }
            // close batch array in the json
            builder.append("]");
            // append closing token in the json
            builder.append("}");
            // finally return the entire payload
            return builder.toString();
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
        return null;
    }

    /*
     * flush events payload to server and return response as String
     * */
    private String flushEventsToServer(String payload) throws IOException {
        if (TextUtils.isEmpty(this.authHeaderString)) {
            RudderLogger.logError("EventRepository: flushEventsToServer: WriteKey was not correct. Aborting flush to server");
            return null;
        }

        // get endPointUrl form config object
        String endPointUri = config.getEndPointUri() + "v1/batch";
        RudderLogger.logDebug("EventRepository: flushEventsToServer: endPointRepository: " + endPointUri);

        // create url object
        URL url = new URL(endPointUri);
        // get connection object
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        // set connection object to return output
        httpConnection.setDoOutput(true);
        //  set content type for network request
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setRequestProperty("Authorization", "Basic " + this.authHeaderString);
        // set request method
        httpConnection.setRequestMethod("POST");
        // get output stream and write payload content
        OutputStream os = httpConnection.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        osw.write(payload);
        osw.flush();
        osw.close();
        os.close();
        // create connection
        httpConnection.connect();
        // get input stream from connection to get output from the server
        if (httpConnection.getResponseCode() == 200) {
            BufferedInputStream bis = new BufferedInputStream(httpConnection.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int res = bis.read();
            // read response from the server
            while (res != -1) {
                baos.write((byte) res);
                res = bis.read();
            }
            // finally return response when reading from server is completed
            return baos.toString();
        } else {
            BufferedInputStream bis = new BufferedInputStream(httpConnection.getErrorStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int res = bis.read();
            // read response from the server
            while (res != -1) {
                baos.write((byte) res);
                res = bis.read();
            }
            // finally return response when reading from server is completed
            RudderLogger.logError("EventRepository: flushEventsToServer: ServerError: " + baos.toString());

            return null;
        }
    }

    /*
     * generic method for dumping all the events
     * */
    void dump(@NonNull RudderMessage message) {
        if (!initiated) return;

        makeFactoryDump(message);
        String eventJson = new Gson().toJson(message);
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: dump: message: %s", eventJson));
        dbManager.saveEvent(eventJson);
    }

    private void makeFactoryDump(RudderMessage message) {
        if (isFactoryInitialized) {
            RudderLogger.logDebug("EventRepository: makeFactoryDump: dumping message to native sdk factories");
            message.setIntegrations(prepareIntegrations());
            for (String key : integrationOperationsMap.keySet()) {
                RudderLogger.logDebug(String.format(Locale.US, "EventRepository: makeFactoryDump: dumping for %s", key));
                RudderIntegration integration = integrationOperationsMap.get(key);
                if (integration != null) {
                    integration.dump(message);
                }
            }
        } else {
            RudderLogger.logDebug("EventRepository: makeFactoryDump: factories are not initialized. dumping to replay queue");
            eventReplayMessage.add(message);
        }
    }

    private Map<String, Object> prepareIntegrations() {
//        if (this.configManager.getConfig() == null) {
        Map<String, Object> integrationPlaceholder = new HashMap<>();
        integrationPlaceholder.put("All", true);
        return integrationPlaceholder;
//        } else {
//            return this.configManager.getIntegrations();
//        }
    }

    void reset() {
        RudderLogger.logDebug("EventRepository: reset: resetting the SDK");
        dbManager.deleteAllEvents();
    }

    void onIntegrationReady(String key, RudderClient.Callback callback) {
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: onIntegrationReady: callback registered for %s", key));
        integrationCallbacks.put(key, callback);
    }

    void shutdown() {
        // TODO: decide shutdown behavior
    }

    void optOut() {
        // TODO:  decide optout functionality and restrictions
    }
}
