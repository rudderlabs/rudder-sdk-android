package com.rudderstack.android.sdk.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * utility class for event processing
 * */
class EventRepository implements Application.ActivityLifecycleCallbacks {
    private final List<RudderMessage> eventReplayMessageQueue = Collections.synchronizedList(new ArrayList<RudderMessage>());
    private String authHeaderString;
    private String anonymousIdHeaderString;
    private Context context;
    RudderConfig config;
    private DBPersistentManager dbManager;
    private RudderServerConfigManager configManager;
    private RudderPreferenceManager preferenceManager;
    private RudderEventFilteringPlugin rudderEventFilteringPlugin;
    private Map<String, RudderIntegration<?>> integrationOperationsMap = new HashMap<>();
    private Map<String, RudderClient.Callback> integrationCallbacks = new HashMap<>();

    // initiate lists for messageIds and messages
    private ArrayList<Integer> messageIds = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();


    private boolean isSDKInitialized = false;
    private boolean isSDKEnabled = true;
    private boolean areFactoriesInitialized = false;
    private AtomicBoolean isFirstLaunch = new AtomicBoolean(true);

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
    EventRepository(Application _application, String _writeKey, RudderConfig _config, String _anonymousId, String _advertisingId, String _deviceToken) {
        // 1. set the values of writeKey, config
        try {
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: writeKey: %s", _writeKey));
            this.authHeaderString = Base64.encodeToString((String.format(Locale.US, "%s:", _writeKey)).getBytes("UTF-8"), Base64.DEFAULT);
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: authHeaderString: %s", this.authHeaderString));
        } catch (UnsupportedEncodingException ex) {
            RudderLogger.logError(ex);
        }
        this.context = _application.getApplicationContext();
        this.config = _config;
        RudderLogger.logDebug(String.format("EventRepository: constructor: %s", this.config.toString()));

        try {
            // initiate RudderPreferenceManager
            preferenceManager = RudderPreferenceManager.getInstance(_application);
            if (preferenceManager.getOptStatus()) {
                if (!TextUtils.isEmpty(_anonymousId)) {
                    _anonymousId = null;
                    RudderLogger.logDebug("User Opted out for tracking the activity, hence dropping the anonymousId");
                }
                if (!TextUtils.isEmpty(_advertisingId)) {
                    _advertisingId = null;
                    RudderLogger.logDebug("User Opted out for tracking the activity, hence dropping the advertisingId");
                }
                if (!TextUtils.isEmpty(_deviceToken)) {
                    _deviceToken = null;
                    RudderLogger.logDebug("User Opted out for tracking the activity, hence dropping the device token");
                }
            }

            // 2. initiate RudderElementCache
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderElementCache");
            // We first send the anonymousId to RudderElementCache which will just set the anonymousId static variable in RudderContext class.
            RudderElementCache.initiate(_application, _anonymousId, _advertisingId, _deviceToken);

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

            // check for lifeCycleEvents
            this.checkApplicationUpdateStatus(_application);
            if (config.isTrackLifecycleEvents() || config.isRecordScreenViews()) {
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
                                    RudderLogger.logDebug("EventRepository: initiating event filtering plugin for device mode destinations");
                                    rudderEventFilteringPlugin = new RudderEventFilteringPlugin(serverConfig.source.destinations);
                                } else {
                                    RudderLogger.logDebug("EventRepository: initiateSDK: No native SDKs are found");
                                }

                                // initiate custom factories
                                initiateCustomFactories();
                                areFactoriesInitialized = true;
                                replayMessageQueue();
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

    private void sendApplicationInstalled(int versionCode) {
        // If trackLifeCycleEvents is not allowed then discard the event
        if (!config.isTrackLifecycleEvents()) {
            return;
        }
        RudderLogger.logDebug("Tracking Application Installed");
        RudderMessage message = new RudderMessageBuilder()
                .setEventName("Application Installed")
                .setProperty(
                        new RudderProperty()
                                .putValue("version", versionCode)
                ).build();
        message.setType(MessageType.TRACK);
        dump(message);
    }

    private void sendApplicationUpdated(int previousVersionCode, int newVersionCode) {
        // If either optOut() is set to true or LifeCycleEvents set to false then discard the event
        if (getOptStatus() || !config.isTrackLifecycleEvents()) {
            return;
        }
        // Application Updated event
        RudderLogger.logDebug("Tracking Application Updated");
        RudderMessage message = new RudderMessageBuilder().setEventName("Application Updated")
                .setProperty(
                        new RudderProperty()
                                .putValue("previous_version", previousVersionCode)
                                .putValue("version", newVersionCode)
                ).build();
        message.setType(MessageType.TRACK);
        dump(message);
    }

    private void checkApplicationUpdateStatus(Application application) {
        try {
            int previousVersionCode = preferenceManager.getBuildVersionCode();
            RudderLogger.logDebug("Previous Installed Version: " + previousVersionCode);
            String packageName = application.getPackageName();
            PackageManager packageManager = application.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            int newVersionCode = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                newVersionCode = (int) packageInfo.getLongVersionCode();
            } else {
                newVersionCode = packageInfo.versionCode;
            }
            RudderLogger.logDebug("Current Installed Version: " + newVersionCode);
            if (previousVersionCode == -1) {
                // application was not installed previously, Application Installed event
                preferenceManager.saveBuildVersionCode(newVersionCode);
                sendApplicationInstalled(newVersionCode);
                registerPeriodicFlushWorker();
            } else if (previousVersionCode != newVersionCode) {
                preferenceManager.saveBuildVersionCode(newVersionCode);
                sendApplicationUpdated(previousVersionCode, newVersionCode);
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
                        handleCallBacks(key, nativeOp);
                    } else {
                        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: initiateFactories: destination was null or not enabled for %s", key));
                    }
                } else {
                    RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateFactories: %s is not present in configMap", key));
                }
            }
        }
    }

    private void initiateCustomFactories() {
        if (config == null || config.getCustomFactories() == null || config.getCustomFactories().isEmpty()) {
            RudderLogger.logInfo("EventRepository: initiateCustomFactories: No custom factory found");
            return;
        }
        for (RudderIntegration.Factory customFactory : config.getCustomFactories()) {
            String key = customFactory.key();
            RudderIntegration<?> nativeOp = customFactory.create(null, RudderClient.getInstance(), config);
            RudderLogger.logInfo(String.format(Locale.US, "EventRepository: initiateCustomFactories: Initiated %s custom factory", key));
            integrationOperationsMap.put(key, nativeOp);
            handleCallBacks(key, nativeOp);
        }
    }

    private void handleCallBacks(String key, RudderIntegration nativeOp) {
        if (integrationCallbacks.containsKey(key)) {
            Object nativeInstance = nativeOp.getUnderlyingInstance();
            RudderClient.Callback callback = integrationCallbacks.get(key);
            if (nativeInstance != null && callback != null) {
                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: handleCallBacks: Callback for %s factory invoked", key));
                callback.onReady(nativeInstance);
            } else {
                RudderLogger.logDebug(String.format(Locale.US, "EventRepository: handleCallBacks: Callback for %s factory is null", key));
            }
        }
    }

    private void replayMessageQueue() {
        synchronized (eventReplayMessageQueue) {
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: replayMessageQueue: replaying old messages with factories. Count: %d", eventReplayMessageQueue.size()));
            if (!eventReplayMessageQueue.isEmpty()) {
                for (RudderMessage message : eventReplayMessageQueue) {
                    makeFactoryDump(message, true);
                }
            }
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

                while (true) {
                    synchronized (messageIds) {
                        try {
                            // clear lists for reuse
                            messageIds.clear();
                            messages.clear();
                            checkIfDBThresholdAttained();
                            // fetch enough events to form a batch
                            RudderLogger.logDebug("EventRepository: processor: Fetching events to flush to server");
                           dbManager.fetchEventsFromDB(messageIds, messages, config.getFlushQueueSize());
                            System.out.println("Desu : EventRepository got batch messageIds and messages");
                            Thread.sleep(3000);
                            // if there are enough events to form a batch and flush to server
                            // OR
                            // sleepTimeOut seconds has elapsed since last successful flush and
                            // we have at least one event to flush to server
                            if (messages.size() >= config.getFlushQueueSize() || (!messages.isEmpty() && sleepCount >= config.getSleepTimeOut())) {
                                // form payload JSON form the list of messages
                                String payload = getPayloadFromMessages(messageIds, messages);
                                System.out.println("Desu : EventRepository Generated payload");
                                RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: payload: %s", payload));
                                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: processor: EventCount: %d", messageIds.size()));
                                if (payload != null) {
                                    // send payload to server if it is not null
                                    networkResponse = flushEventsToServer(payload);
                                    System.out.println("Desu : EventRepository Tried Sending events to server");
                                    RudderLogger.logInfo(String.format(Locale.US, "EventRepository: processor: ServerResponse: %s", networkResponse));
                                    // if success received from server
                                    if (networkResponse == Utils.NetworkResponses.SUCCESS) {
                                        // remove events from DB
                                        dbManager.clearEventsFromDB(messageIds);
                                        System.out.println("Desu : EventRepository Successfully Sent events to server");
                                        // reset sleep count to indicate successful flush
                                        sleepCount = 0;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            RudderLogger.logError(ex);
                        }
                    }
                    // increment sleepCount to track total elapsed seconds
                    sleepCount += 1;
                    System.out.println(String.format(Locale.US, "EventRepository: processor: SleepCount: %d", sleepCount));
                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: SleepCount: %d", sleepCount));
                    try {
                        if (networkResponse == Utils.NetworkResponses.WRITE_KEY_ERROR) {
                            RudderLogger.logInfo("Wrong WriteKey. Aborting");
                            break;
                        } else if (networkResponse == Utils.NetworkResponses.ERROR) {
                            RudderLogger.logInfo("flushEvents: Retrying in " + Math.abs(sleepCount - config.getSleepTimeOut()) + "s");
                            System.out.println("Desu : EventRepository Sleep Started");
                            Thread.sleep(Math.abs(sleepCount - config.getSleepTimeOut()) * 1000);
                            System.out.println("Desu : EventRepository Sleep Completed");
                        } else {
                            // retry entire logic in 1 second
                            System.out.println("Desu : EventRepository Sleep Started");
                            Thread.sleep(1000);
                            System.out.println("Desu : EventRepository Sleep Completed");
                        }
                    } catch (Exception ex) {
                        RudderLogger.logError(ex);
                    }
                }
            }

        };
    }

    /*
     * check if the number of events in the db crossed the dbCountThreshold then delete the older events which are in excess.
     */

    private void checkIfDBThresholdAttained() {
        // get current record count from db
        int recordCount = dbManager.getDBRecordCount();
        System.out.println(String.format(Locale.US, "EventRepository: checkIfDBThresholdAttained: DBRecordCount: %d", recordCount));
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: checkIfDBThresholdAttained: DBRecordCount: %d", recordCount));
        // if record count exceeds threshold count, remove older events
        if (recordCount > config.getDbCountThreshold()) {
            // fetch extra old events
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: checkIfDBThresholdAttained: OldRecordCount: %d", (recordCount - config.getDbCountThreshold())));
            // initiate lists for messageId and message
            ArrayList<Integer> messageIds = new ArrayList<>();
            ArrayList<String> messages = new ArrayList<>();
            dbManager.fetchEventsFromDB(messageIds, messages, recordCount - config.getDbCountThreshold());
            // remove events
            dbManager.clearEventsFromDB(messageIds);
        }
    }

    /*
     * create payload string from messages list
     * - we created payload from individual message json strings to reduce the complexity
     * of deserialization and forming the payload object and creating the json string
     * again from the object
     * */
    String getPayloadFromMessages(ArrayList<Integer> messageIds, ArrayList<String> messages) {
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
    Utils.NetworkResponses flushEventsToServer(String payload) {
        try {
            if (TextUtils.isEmpty(this.authHeaderString)) {
                RudderLogger.logError("EventRepository: flushEventsToServer: WriteKey was not correct. Aborting flush to server");
                return null;
            }

            // get endPointUrl form config object
            String dataPlaneEndPoint = config.getDataPlaneUrl() + "v1/batch";
            System.out.println("EventRepository: flushEventsToServer: dataPlaneEndPoint: " + dataPlaneEndPoint);
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
            System.out.println("Printing cause:" + ex.getCause());
            System.out.println("Printing Localized Message: " + ex.getLocalizedMessage());
            System.out.println(ex.getStackTrace());
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
        // if no integrations were set in the RudderOption object passed in that particular event
        // we would fall back to check for the integrations in the RudderOption object passed while initializing the sdk
        if (message.getIntegrations().size() == 0) {
            if (RudderClient.getDefaultOptions() != null && RudderClient.getDefaultOptions().getIntegrations() != null && RudderClient.getDefaultOptions().getIntegrations().size() != 0) {
                message.setIntegrations(RudderClient.getDefaultOptions().getIntegrations());
            }
            // if no RudderOption object is passed while initializing the sdk we would set all the integrations to true
            else {
                message.setIntegrations(prepareIntegrations());
            }
        }
        // If `All` is absent in the integrations object we will set it to true for making All is true by default
        if (!message.getIntegrations().containsKey("All")) {
            message.setIntegrations(prepareIntegrations());
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
                .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
                .create();
        String eventJson = gson.toJson(message);
        makeFactoryDump(message, false);
        RudderLogger.logVerbose(String.format(Locale.US, "EventRepository: dump: message: %s", eventJson));
        if (Utils.getUTF8Length(eventJson) > Utils.MAX_EVENT_SIZE) {
            RudderLogger.logError(String.format(Locale.US, "EventRepository: dump: Event size exceeds the maximum permitted event size(%d)", Utils.MAX_EVENT_SIZE));
            return;
        }
        dbManager.saveEvent(eventJson);
    }

    private void makeFactoryDump(RudderMessage message, boolean fromHistory) {
        synchronized (eventReplayMessageQueue) {
            if (areFactoriesInitialized || fromHistory) {
                //Fetch all the Integrations set by the user, for sending events to any specific device mode destinations
                Map<String, Object> integrationOptions = message.getIntegrations();
                //If 'All' is 'true'
                if ((boolean) integrationOptions.get("All")) {
                    for (String key : integrationOperationsMap.keySet()) {
                        RudderIntegration<?> integration = integrationOperationsMap.get(key);
                        //If integration is not null and if key is either not present or it is set to true, then dump it.
                        if (integration != null)
                            if (!integrationOptions.containsKey(key) || (boolean) integrationOptions.get(key))
                                if (rudderEventFilteringPlugin.isEventAllowed(key, message)) {
                                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: makeFactoryDump: dumping for %s", key));
                                    integration.dump(message);
                                }
                    }
                }
                //If User has set any specific Option.
                else {
                    for (String key : integrationOperationsMap.keySet()) {
                        RudderIntegration<?> integration = integrationOperationsMap.get(key);
                        //If integration is not null and 'key' is set to 'true', then dump it.
                        if (integration != null)
                            if (integrationOptions.containsKey(key) && (boolean) integrationOptions.get(key))
                                if (rudderEventFilteringPlugin.isEventAllowed(key, message)) {
                                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: makeFactoryDump: dumping for %s", key));
                                    integration.dump(message);
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
        if (areFactoriesInitialized) {
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

    void registerPeriodicFlushWorker() {

        if (config.isPeriodicFlushEnabled()) {
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            PeriodicWorkRequest flushPendingEvents =
                    new PeriodicWorkRequest.Builder(FlushEventsWorker.class, 1, TimeUnit.HOURS)
                            .addTag("Flushing Pending Events Periodically")
                            .setConstraints(constraints)
                            .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "flushEvents",
                    ExistingPeriodicWorkPolicy.KEEP,
                    flushPendingEvents);

            System.out.println("Periodic Flush Worker Registered and its Id is " + flushPendingEvents.getId());
        }

//        WorkRequest uploadWorkRequest =
//                new OneTimeWorkRequest.Builder(FlushEventsWorker.class)
//                        .addTag("Flush for one-time")
//                        .build();
//
//        System.out.println("Work Id is : " + uploadWorkRequest.getId());
//
//        WorkManager
//                .getInstance(context)
//                .enqueue(uploadWorkRequest);
//
//
//        System.out.println("Work enqueued");

        // We cannot send Event repository object and config through the data object.
        //  new Data.Builder().put("EVENT_REPOSITORY", this).put("CONFIG", config);


//        // Example of creating constraints, periodic work request and setting constraints to it.
//        Constraints constraints = new Constraints.Builder().setRequiresDeviceIdle(true).setRequiredNetworkType(NetworkType.CONNECTED).build();
//        PeriodicWorkRequest saveRequest =
//                new PeriodicWorkRequest.Builder(FlushEventsWorker.class, 1, TimeUnit.HOURS)
//                        .addTag("Flushing Periodically")
//                        .setConstraints(constraints)
//                        .build();
//


        // We will use enqueueUniquePeriodicWork as we don't want the same work to be enqueued again and again even though this block of code is called everytime the app is launched.
        // Probably we can use the logic for registering the periodic work in the section where we are triggering Application Installed.
        // At the end we need to see we will have only one work running with the given tag regardless of how many times you enqueue it.
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                "sendLogs",
//                ExistingPeriodicWorkPolicy.KEEP,
//                saveRequest);


        //        saveRequest.getId(); // could be the work id, which we can use to check the status of the work.
        // trying to read the work status and print it accordingly, need to figure out how to get access to the lifecycleowner here
//        WorkManager.getInstance(context).getWorkInfoByIdLiveData(uploadWorkRequest.getId()).observe(lifecycleOwner, new Observer<WorkInfo>() {
//            @Override
//            public void onChanged(@Nullable WorkInfo workInfo) {
//                if (workInfo != null) {
//                    Data progress = workInfo.getProgress();
//                    int value = progress.getInt(PROGRESS, 0);
//
//                }
//            }
//        });

    }

    <T> ArrayList<T> getBatch(ArrayList<T> messageDetails) {
        if (messageDetails.size() <= config.getFlushQueueSize()) {
            return messageDetails;
        } else {
            return new ArrayList(messageDetails.subList(0, config.getFlushQueueSize()));
        }
    }


    int getNumberOfBatches(int numberOfEvents) {
        if (numberOfEvents % config.getFlushQueueSize() == 0) {
            return numberOfEvents / config.getFlushQueueSize();
        } else {
            return (numberOfEvents / config.getFlushQueueSize()) + 1;
        }
    }

    void flush() {
        if (areFactoriesInitialized) {
            RudderLogger.logDebug("EventRepository: flush native SDKs");
            for (String key : integrationOperationsMap.keySet()) {
                RudderLogger.logDebug(String.format(Locale.US, "EventRepository: flush for %s", key));
                RudderIntegration<?> integration = integrationOperationsMap.get(key);
                if (integration != null) {
                    integration.flush();
                }
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.NetworkResponses networkResponse = Utils.NetworkResponses.ERROR;
                System.out.println("Desu : Flush Requested for Synchronization");
                synchronized (messageIds) {
                    System.out.println("Desu : Flush got access to Synchronization");
                    messageIds.clear();
                    messages.clear();

                    

                    RudderLogger.logDebug("EventRepository: Flush: Fetching events to flush to server");
                    dbManager.fetchAllEventsFromDB(messageIds, messages);
                    int numberOfBatches = getNumberOfBatches(messages.size());
                    boolean lastBatchFailed = false;
                    for (int i = 1; i <= numberOfBatches && !lastBatchFailed; i++) {
                        lastBatchFailed = true;
                        int retries = 3;
                        while (retries-- > 0) {
                            ArrayList<Integer> batchMessageIds = getBatch(messageIds);
                            ArrayList<String> batchMessages = getBatch(messages);
                            String payload = getPayloadFromMessages(batchMessageIds, batchMessages);
                            System.out.println("Desu : Flush Created Payload");
                            System.out.println(String.format(Locale.US, "EventRepository: flush: payload: %s", payload));
                            System.out.println(String.format(Locale.US, "EventRepository: flush: EventCount: %d", batchMessageIds.size()));
                            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: flush: payload: %s", payload));
                            RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: EventCount: %d", batchMessages.size()));
                            if (payload != null) {
                                // send payload to server if it is not null
                                networkResponse = flushEventsToServer(payload);
                                System.out.println("Desu : flush tried sending events to server");
                                System.out.println(String.format(Locale.US, "EventRepository: flush: ServerResponse: %s", networkResponse));
                                RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: ServerResponse: %s", networkResponse));
                                // if success received from server
                                if (networkResponse == Utils.NetworkResponses.SUCCESS) {
                                    // remove events from DB
                                    System.out.println(String.format("Desu : Successfully sent batch %d / %d ", i, numberOfBatches));
                                    System.out.println("Desu : FLush Succesfully sent events to server");
                                    System.out.println(String.format(Locale.US, "EventRepository: flush: clearingEvents from DB: %s", networkResponse));
                                    RudderLogger.logInfo(String.format(Locale.US, "EventRepository: flush: clearingEvents from DB: %s", networkResponse));
                                    dbManager.clearEventsFromDB(batchMessageIds);
                                    messageIds.removeAll(batchMessageIds);
                                    messages.removeAll(batchMessages);
                                    lastBatchFailed = false;
                                    break;
                                }
                            }
                            System.out.println(String.format("Desu : Failed to send batch %d / %d retrying again in Flush , %d retries left", i, numberOfBatches, retries));
                        }
                        if (lastBatchFailed) {
                            System.out.println(String.format("Desu : Failed to send batch %d / %d after 3 retries , dropping the remaining batches as well", i, numberOfBatches));
                        }
                    }

                }
            }

        }).start();
    }


    void onIntegrationReady(String key, RudderClient.Callback callback) {
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: onIntegrationReady: callback registered for %s", key));
        integrationCallbacks.put(key, callback);
    }

    /**
     * Opts out a user from tracking the activity. When enabled all the events will be dropped by the SDK.
     *
     * @param optOut Boolean value to store optOut status
     */
    void saveOptStatus(boolean optOut) {
        preferenceManager.saveOptStatus(optOut);
        updateOptStatusTime(optOut);
    }

    /**
     * If true, save user optOut time
     * If false, save user optIn time
     *
     * @param optOut Boolean value to update optOut or optIn time
     */
    private void updateOptStatusTime(boolean optOut) {
        if (optOut) {
            preferenceManager.updateOptOutTime();
        } else {
            preferenceManager.updateOptInTime();
        }
    }

    /**
     * @return optOut status
     */
    boolean getOptStatus() {
        return preferenceManager.getOptStatus();
    }

    void updateAnonymousId(@NonNull String anonymousId) {
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: updateAnonymousId: Updating AnonymousId: %s", anonymousId));
        RudderElementCache.updateAnonymousId(anonymousId);
        preferenceManager.saveAnonymousId(RudderContext.getAnonymousId());
        try {
            this.anonymousIdHeaderString = Base64.encodeToString(RudderContext.getAnonymousId().getBytes("UTF-8"), Base64.DEFAULT);
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (config.isRecordScreenViews()) {
            // If user has disabled tracking activities (i.e., set optOut() to true)
            // then discard the event
            if (getOptStatus()) {
                return;
            }
            ScreenPropertyBuilder screenPropertyBuilder = new ScreenPropertyBuilder().setScreenName(activity.getLocalClassName()).isAtomatic(true);
            RudderMessage screenMessage = new RudderMessageBuilder().setEventName(activity.getLocalClassName()).setProperty(screenPropertyBuilder.build()).build();
            screenMessage.setType(MessageType.SCREEN);
            this.dump(screenMessage);
        }
        if (this.config.isTrackLifecycleEvents()) {
            noOfActivities += 1;
            if (noOfActivities == 1) {
                // If user has disabled tracking activities (i.e., set optOut() to true)
                // then discard the event
                if (getOptStatus()) {
                    return;
                }
                RudderMessage trackMessage;
                trackMessage = new RudderMessageBuilder()
                        .setEventName("Application Opened")
                        .setProperty(Utils.trackDeepLink(activity, isFirstLaunch, preferenceManager.getBuildVersionCode()))
                        .build();
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
                // If user has disabled tracking activities (i.e., set optOut() to true)
                // then discard the event
                if (getOptStatus()) {
                    return;
                }
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




