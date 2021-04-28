package com.rudderstack.android.sdk.core;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * utility class for event processing
 * */
class EventRepository implements Application.ActivityLifecycleCallbacks {
    private final List<RudderMessage> eventReplayMessageQueue = Collections.synchronizedList(new ArrayList<RudderMessage>());
    private String authHeaderString;
    private String anonymousIdHeaderString;
    private RudderConfig config;
    private DBPersistentManager dbManager;
    private RudderServerConfigManager configManager;
    private RudderPreferenceManager preferenceManager;
    private Map<String, RudderIntegration<?>> integrationOperationsMap = new HashMap<>();
    private Map<String, RudderClient.Callback> integrationCallbacks = new HashMap<>();

    private boolean isSDKInitialized = false;
    private boolean isSDKEnabled = true;
    private boolean isFactoryInitialized;
    private int noOfActivities;

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
    EventRepository(Application _application, String _writeKey, RudderConfig _config, String _anonymousId, String _advertisingId) {
        // 1. set the values of writeKey, config
        try {
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: writeKey: %s", _writeKey));
            this.authHeaderString = Base64.encodeToString((String.format(Locale.US, "%s:", _writeKey)).getBytes("UTF-8"), Base64.DEFAULT);
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: authHeaderString: %s", this.authHeaderString));
        } catch (UnsupportedEncodingException ex) {
            RudderLogger.logError(ex);
        }
        this.config = _config;
        RudderLogger.logDebug(String.format("EventRepository: constructor: %s", this.config.toString()));

        try {
            // 2. initiate RudderElementCache
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderElementCache");
            RudderElementCache.initiate(_application, _anonymousId, _advertisingId);

            String anonymousId = RudderContext.getAnonymousId();
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousId: %s", anonymousId));
            this.anonymousIdHeaderString = Base64.encodeToString(anonymousId.getBytes("UTF-8"), Base64.DEFAULT);
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousIdHeaderString: %s", this.anonymousIdHeaderString));

            // 3. initiate DBPersistentManager for SQLite operations
            RudderLogger.logDebug("EventRepository: constructor: Initiating DBPersistentManager");
            this.dbManager = DBPersistentManager.getInstance(_application);

            // 4. initiate RudderServerConfigManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderServerConfigManager");
            this.configManager = new RudderServerConfigManager(_application, _writeKey, _config);

            // 5. start processor thread
            RudderLogger.logDebug("EventRepository: constructor: Initiating processor and factories");
            this.initiateSDK();

            // initiate RudderPreferenceManager and check for lifeCycleEvents
            preferenceManager = RudderPreferenceManager.getInstance(_application);
            if (config.isTrackLifecycleEvents() || config.isRecordScreenViews()) {
                this.checkApplicationUpdateStatus(_application);
                _application.registerActivityLifecycleCallbacks(this);
            }
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    private void initiateSDK() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int retryCount = 0;
                    while (!isSDKInitialized && retryCount <= 5) {
                        Utils.NetworkResponses receivedError = configManager.getError();
                        RudderServerConfig serverConfig = configManager.getConfig();
                        if (serverConfig != null) {
                            isSDKEnabled = serverConfig.source.isSourceEnabled;
                            if (isSDKEnabled) {
                                // initiate processor
                                RudderLogger.logDebug("EventRepository: initiateSDK: Initiating processor");
                                Thread processorThread = new Thread(getProcessorRunnable());
                                processorThread.start();

                                // initiate factories
                                if (serverConfig.source.destinations != null) {
                                    initiateFactories(serverConfig.source.destinations);
                                } else {
                                    RudderLogger.logDebug("EventRepository: initiateSDK: No native SDKs are found");
                                }
                            } else {
                                RudderLogger.logDebug("EventRepository: initiateSDK: source is disabled in the dashboard");
                                RudderLogger.logDebug("Flushing persisted events");
                                dbManager.flushEvents();
                            }

                            isSDKInitialized = true;
                        } else if (receivedError == Utils.NetworkResponses.WRITE_KEY_ERROR) {
                            RudderLogger.logError("WRONG WRITE_KEY");
                            break;
                        } else {
                            retryCount += 1;
                            RudderLogger.logDebug("EventRepository: initiateFactories: retry count: " + retryCount);
                            RudderLogger.logInfo("initiateSDK: Retrying in " + retryCount * 2 + "s");
                            Thread.sleep(retryCount * 2 * 1000);
                        }
                    }
                } catch (Exception ex) {
                    RudderLogger.logError(ex);
                }
            }
        }).start();
    }

    private void checkApplicationUpdateStatus(Application application) {
        try {
            int previousVersionCode = preferenceManager.getBuildVersionCode();
            RudderLogger.logDebug("Previous Installed Version: " + previousVersionCode);
            String packageName = application.getPackageName();
            PackageManager packageManager = application.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            int versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = (int) packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
            RudderLogger.logDebug("Current Installed Version: " + versionCode);

            if (previousVersionCode == -1) {
                // application was not installed previously, Application Installed events
                RudderLogger.logDebug("Tracking Application Installed");
                preferenceManager.saveBuildVersionCode(versionCode);
                RudderMessage message = new RudderMessageBuilder().setEventName("Application Installed").build();
                message.setType(MessageType.TRACK);
                dump(message);
            } else if (previousVersionCode != versionCode) {
                // application updated
                RudderLogger.logDebug("Tracking Application Updated");
                preferenceManager.saveBuildVersionCode(versionCode);
                RudderMessage message = new RudderMessageBuilder().setEventName("Application Updated").build();
                message.setType(MessageType.TRACK);
                dump(message);
            }
        } catch (PackageManager.NameNotFoundException ex) {
            RudderLogger.logError(ex);
        }
    }

    private void initiateFactories(List<RudderServerDestination> destinations) {
        // initiate factory initialization after 10s
        // let the factories capture everything they want to capture
        if (config == null || config.getFactories() == null || config.getFactories().isEmpty()) {
            RudderLogger.logInfo("EventRepository: initiateFactories: No native SDK factory found");
            isFactoryInitialized = true;
            return;
        }
        // initiate factories if client is initialized properly
        if (destinations.isEmpty()) {
            RudderLogger.logInfo("EventRepository: initiateFactories: No destination found in the config");
        } else {
            // check for multiple destinations
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
                        RudderIntegration<?> nativeOp = factory.create(destinationConfig, RudderClient.getInstance(), config);
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

        synchronized (eventReplayMessageQueue) {
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: replaying old messages with factory. Count: %d", eventReplayMessageQueue.size()));
            if (!eventReplayMessageQueue.isEmpty()) {
                for (RudderMessage message : eventReplayMessageQueue) {
                    makeFactoryDump(message, true);
                }
            }
            isFactoryInitialized = true;
            eventReplayMessageQueue.clear();
        }

    }

    private Runnable getProcessorRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // initiate sleepCount
                int sleepCount = 0;
                Utils.NetworkResponses networkResponse = Utils.NetworkResponses.SUCCESS;

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
                            String payload = getPayloadFromMessages(messageIds, messages);
                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: payload: %s", payload));
                            RudderLogger.logInfo(String.format(Locale.US, "EventRepository: processor: EventCount: %d", messageIds.size()));
                            if (payload != null) {
                                // send payload to server if it is not null
                                networkResponse = flushEventsToServer(payload);
                                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: processor: ServerResponse: %s", networkResponse));
                                // if success received from server
                                if (networkResponse == Utils.NetworkResponses.SUCCESS) {
                                    // remove events from DB
                                    dbManager.clearEventsFromDB(messageIds);
                                    // reset sleep count to indicate successful flush
                                    sleepCount = 0;
                                }
                            }
                        }
                        // increment sleepCount to track total elapsed seconds
                        sleepCount += 1;
                        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: SleepCount: %d", sleepCount));
                        if (networkResponse == Utils.NetworkResponses.WRITE_KEY_ERROR) {
                            RudderLogger.logInfo("Wrong WriteKey. Aborting");
                            break;
                        } else if (networkResponse == Utils.NetworkResponses.ERROR) {
                            RudderLogger.logInfo("flushEvents: Retrying in " + sleepCount + "s");
                            Thread.sleep(Math.abs(sleepCount - config.getSleepTimeOut()) * 1000);
                        } else {
                            // retry entire logic in 1 second
                            Thread.sleep(1000);
                        }
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
    private String getPayloadFromMessages(ArrayList<Integer> messageIds, ArrayList<String> messages) {
        try {
            RudderLogger.logDebug("EventRepository: getPayloadFromMessages: recordCount: " + messages.size());
            String sentAtTimestamp = Utils.getTimeStamp();
            RudderLogger.logDebug("EventRepository: getPayloadFromMessages: sentAtTimestamp: " + sentAtTimestamp);
            // initialize ArrayLists to store current batch
            ArrayList<Integer> batchMessageIds = new ArrayList<>();
            // get string builder
            StringBuilder builder = new StringBuilder();
            // append initial json token
            builder.append("{");
            // append sent_at time stamp
            builder.append("\"sentAt\":\"").append(sentAtTimestamp).append("\",");
            // initiate batch array in the json
            builder.append("\"batch\": [");
            int totalBatchSize = Utils.getUTF8Length(builder) + 2; // we add 2 characters at the end
            int messageSize;
            // loop through messages list and add in the builder
            for (int index = 0; index < messages.size(); index++) {
                String message = messages.get(index);
                // strip last ending object character
                message = message.substring(0, message.length() - 1);
                // add sentAt time stamp
                message = String.format("%s,\"sentAt\":\"%s\"},", message, sentAtTimestamp);
                // add message size to batch size
                messageSize = Utils.getUTF8Length(message);
                totalBatchSize += messageSize;
                // check batch size
                if (totalBatchSize >= Utils.MAX_BATCH_SIZE) {
                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: getPayloadFromMessages: MAX_BATCH_SIZE reached at index: %d | Total: %d", index, totalBatchSize));
                    break;
                }
                // finally add message string to builder
                builder.append(message);
                // add message to batch ArrayLists
                batchMessageIds.add(messageIds.get(index));
            }
            if (builder.charAt(builder.length() - 1) == ',') {
                // remove trailing ','
                builder.deleteCharAt(builder.length() - 1);
            }
            // close batch array in the json
            builder.append("]");
            // append closing token in the json
            builder.append("}");
            // retain all events belonging to the batch
            messageIds.retainAll(batchMessageIds);
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
    private Utils.NetworkResponses flushEventsToServer(String payload) {
        try {
            if (TextUtils.isEmpty(this.authHeaderString)) {
                RudderLogger.logError("EventRepository: flushEventsToServer: WriteKey was not correct. Aborting flush to server");
                return null;
            }

            // get endPointUrl form config object
            String dataPlaneEndPoint = config.getDataPlaneUrl() + "v1/batch";
            RudderLogger.logDebug("EventRepository: flushEventsToServer: dataPlaneEndPoint: " + dataPlaneEndPoint);

            // create url object
            URL url = new URL(dataPlaneEndPoint);
            // get connection object
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            // set connection object to return output
            httpConnection.setDoOutput(true);
            //  set content type for network request
            httpConnection.setRequestProperty("Content-Type", "application/json");
            // set authorization header
            httpConnection.setRequestProperty("Authorization", String.format(Locale.US, "Basic %s", this.authHeaderString));
            // set anonymousId header for definitive routing
            httpConnection.setRequestProperty("AnonymousId", this.anonymousIdHeaderString);
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
                if (baos.toString().equalsIgnoreCase("OK")) {
                    return Utils.NetworkResponses.SUCCESS;
                }
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
                String errorResp = baos.toString();
                RudderLogger.logError("EventRepository: flushEventsToServer: ServerError: " + errorResp);
                // return null as request made is not successful
                if (errorResp.toLowerCase().contains("invalid write key")) {
                    return Utils.NetworkResponses.WRITE_KEY_ERROR;
                }
            }
        } catch (Exception ex) {
            RudderLogger.logError(ex);
        }
        return Utils.NetworkResponses.ERROR;
    }

    /*
     * generic method for dumping all the events
     * */
    void dump(@NonNull RudderMessage message) {
        if (!isSDKEnabled) {
            return;
        }

        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: dump: eventName: %s", message.getEventName()));
        Map<String, Object> integrations = new HashMap<>();
        integrations.put("All", true);
        message.setIntegrations(integrations);

        makeFactoryDump(message, false);
        String eventJson = new Gson().toJson(message);
        RudderLogger.logVerbose(String.format(Locale.US, "EventRepository: dump: message: %s", eventJson));
        if (Utils.getUTF8Length(eventJson) > Utils.MAX_EVENT_SIZE) {
            RudderLogger.logError(String.format(Locale.US, "EventRepository: dump: Event size exceeds the maximum permitted event size(%d)", Utils.MAX_EVENT_SIZE));
            return;
        }
        dbManager.saveEvent(eventJson);
    }

    private void makeFactoryDump(RudderMessage message, boolean fromHistory) {
        synchronized (eventReplayMessageQueue) {
            if (isFactoryInitialized || fromHistory) {
                //If user doesn't set RudderOption, both while initializing the sdk and/or making any calls
                //then simply dump the message.
                if((message.getRudderOption() == null || (message.getRudderOption()!=null && message.getRudderOption().getIntegrations().size() == 0)) && RudderClient.getDefaultOptions() == null) {
                    for (String key : integrationOperationsMap.keySet()) {
                        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: makeFactoryDump: dumping for %s", key));
                        RudderIntegration<?> integration = integrationOperationsMap.get(key);
                        if(integration != null) {
                            integration.dump(message);
                        }
                    }
                }
                //If any Option is set i.e., while initializing the sdk and/or making any calls
                else {
                    // If user sets RudderOptions, both when initializing the sdk,
                    // we need to fetch the option from DefaultOption variable and set the Integration.
                    if(message.getRudderOption() != null && message.getRudderOption().getIntegrations().size() != 0 && RudderClient.getDefaultOptions()!=null && RudderClient.getDefaultOptions().getIntegrations()!=null && RudderClient.getDefaultOptions().getIntegrations().size()!=0)
                    {
                        message.setIntegrations(RudderClient.getDefaultOptions().getIntegrations());
                    }
                    //Now fetch all the RudderOption set by the user, for sending events to any specific device mode destinations
                    Map<String, Object> enabledIntegration = message.getIntegrations();
                    for (String key : integrationOperationsMap.keySet()) {
                        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: makeFactoryDump: dumping for %s", key));
                        RudderIntegration<?> integration = integrationOperationsMap.get(key);
                        if (integration != null){
                            //If User has set the Option 'All' as 'true'
                            if(enabledIntegration.containsKey("All") && (boolean) enabledIntegration.get("All")){
                                //If option for the particular Integration/Key is not present, then dump it
                                if(enabledIntegration.containsKey(key) == false){
                                    integration.dump(message);
                                    continue;
                                }
                                //If option for the particular Integration/Key is present and it is set to true, then dump it.
                                if((boolean) enabledIntegration.get(key)){
                                    integration.dump(message);
                                }
                            }
                            // If User has not set the Option 'All' as 'true' i.e., 'All' is 'false'
                            // Then if Option has been set by user then dump else ignore.
                            else if(enabledIntegration.containsKey(key) && (boolean) enabledIntegration.get(key)){
                                integration.dump(message);
                            }
                        }
                    }
                }
            } else {
                RudderLogger.logDebug("EventRepository: makeFactoryDump: factories are not initialized. dumping to replay queue");
                eventReplayMessageQueue.add(message);
            }
        }
    }

    private Map<String, Object> prepareIntegrations() {
        Map<String, Object> integrationPlaceholder = new HashMap<>();
        integrationPlaceholder.put("All", true);
        return integrationPlaceholder;
    }

    void reset() {
        RudderLogger.logDebug("EventRepository: reset: resetting the SDK");
        if (isFactoryInitialized) {
            RudderLogger.logDebug("EventRepository: resetting native SDKs");
            for (String key : integrationOperationsMap.keySet()) {
                RudderLogger.logDebug(String.format(Locale.US, "EventRepository: reset for %s", key));
                RudderIntegration<?> integration = integrationOperationsMap.get(key);
                if (integration != null) {
                    integration.reset();
                }
            }
        } else {
            RudderLogger.logDebug("EventRepository: reset: factories are not initialized. ignored");
        }
    }

    void flush() {
        if (isFactoryInitialized) {
            RudderLogger.logDebug("EventRepository: flush native SDKs");
            for (String key : integrationOperationsMap.keySet()) {
                RudderLogger.logDebug(String.format(Locale.US, "EventRepository: flush for %s", key));
                RudderIntegration<?> integration = integrationOperationsMap.get(key);
                if (integration != null) {
                    integration.flush();
                }
            }
        }
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

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (config.isRecordScreenViews()) {
            ScreenPropertyBuilder screenPropertyBuilder = new ScreenPropertyBuilder().setScreenName(activity.getLocalClassName()).isAtomatic(true);
            RudderMessage screenMessage = new RudderMessageBuilder().setEventName(activity.getLocalClassName()).setProperty(screenPropertyBuilder.build()).build();
            screenMessage.setType(MessageType.SCREEN);
            this.dump(screenMessage);
        }
        if (this.config.isTrackLifecycleEvents()) {
            noOfActivities += 1;
            if (noOfActivities == 1) {
                // no previous activity present. Application Opened
                RudderMessage trackMessage = new RudderMessageBuilder().setEventName("Application Opened").build();
                trackMessage.setType(MessageType.TRACK);
                this.dump(trackMessage);
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (this.config.isTrackLifecycleEvents()) {
            noOfActivities -= 1;
            if (noOfActivities == 0) {
                RudderMessage message = new RudderMessageBuilder().setEventName("Application Backgrounded").build();
                message.setType(MessageType.TRACK);
                this.dump(message);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
