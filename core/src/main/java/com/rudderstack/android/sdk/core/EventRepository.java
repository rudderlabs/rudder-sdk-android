package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.FlushUtils.flushEventsToServer;
import static com.rudderstack.android.sdk.core.FlushUtils.flushNativeSdks;
import static com.rudderstack.android.sdk.core.FlushUtils.getPayloadFromMessages;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * utility class for event processing
 * */
class EventRepository implements Application.ActivityLifecycleCallbacks {
    private final List<RudderMessage> eventReplayMessageQueue = Collections.synchronizedList(new ArrayList<RudderMessage>());
    private String authHeaderString;
    private String anonymousIdHeaderString;
    private Context context;
    private RudderConfig config;
    private DBPersistentManager dbManager;
    private RudderServerConfigManager configManager;
    private RudderPreferenceManager preferenceManager;
    private RudderEventFilteringPlugin rudderEventFilteringPlugin;
    private RudderFlushWorkManager rudderFlushWorkManager;
    private RudderUserSession userSession;
    private Map<String, RudderIntegration<?>> integrationOperationsMap = new HashMap<>();
    private Map<String, RudderClient.Callback> integrationCallbacks = new HashMap<>();

    private boolean isSDKInitialized = false;
    private boolean isSDKEnabled = true;
    private boolean areFactoriesInitialized = false;
    private boolean applicationInstalledOrUpdated = false;
    private AtomicBoolean isFirstLaunch = new AtomicBoolean(true);

    private int noOfActivities;


    /*
     * constructor to be called from RudderClient internally.
     * -- tasks to be performed
     * 1. persist the value of config
     * 2. initiate RudderElementCache
     * 3. initiate DBPersistentManager for SQLite operations
     * 4. initiate RudderServerConfigManager
     * 5. initiate FlushWorkManager
     * 6. start processor thread
     * 7. initiate factories
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
            preferenceManager.performMigration();
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
            RudderElementCache.initiate(_application, _anonymousId, _advertisingId, _deviceToken, _config.isAutoCollectAdvertId());

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

            // 5. initiate FlushWorkManager
            RudderFlushConfig rudderFlushConfig = new RudderFlushConfig(config.getDataPlaneUrl(), authHeaderString, anonymousIdHeaderString, config.getFlushQueueSize(), config.getLogLevel());
            this.rudderFlushWorkManager = new RudderFlushWorkManager(context, config, preferenceManager, rudderFlushConfig);

            // 6. start processor thread
            RudderLogger.logDebug("EventRepository: constructor: Initiating processor and factories");
            this.initiateSDK();

            // Session Tracking
            // 7. initiate RudderUserSession
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderUserSession");
            userSession = new RudderUserSession(preferenceManager, config);

            // 8. clear session if automatic session tracking was enabled previously
            // but disabled presently or vice versa.
            boolean previousAutoSessionTrackingStatus = preferenceManager.getAutoSessionTrackingStatus();
            if (previousAutoSessionTrackingStatus != config.isTrackAutoSession()) {
                userSession.clearSession();
            }
            preferenceManager.saveAutoSessionTrackingStatus(config.isTrackAutoSession());

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

    private void sendApplicationInstalled(int currentBuild, String currentVersion) {
        // If trackLifeCycleEvents is not allowed then discard the event
        if (!config.isTrackLifecycleEvents()) {
            return;
        }
        // Session Tracking
        // Automatic tracking session started
        if (config.isTrackAutoSession()) {
            userSession.startSession(Utils.getCurrentTimeSeconds());
        }

        RudderLogger.logDebug("Tracking Application Installed");
        RudderMessage message = new RudderMessageBuilder()
                .setEventName("Application Installed")
                .setProperty(
                        new RudderProperty()
                                .putValue("version", currentVersion)
                                .putValue("build", currentBuild)
                ).build();
        message.setType(MessageType.TRACK);
        dump(message);
    }

    private void sendApplicationUpdated(int previousBuild, int currentBuild, String previousVersion, String currentVersion) {
        // If either optOut() is set to true or LifeCycleEvents set to false then discard the event
        if (getOptStatus() || !config.isTrackLifecycleEvents()) {
            return;
        }
        // Session Tracking
        // Automatic tracking session started
        if (config.isTrackAutoSession()) {
            userSession.startSession(Utils.getCurrentTimeSeconds());
        }

        // Application Updated event
        RudderLogger.logDebug("Tracking Application Updated");
        RudderMessage message = new RudderMessageBuilder().setEventName("Application Updated")
                .setProperty(
                        new RudderProperty()
                                .putValue("previous_version", previousVersion)
                                .putValue("version", currentVersion)
                                .putValue("previous_build", previousBuild)
                                .putValue("build", currentBuild)
                ).build();
        message.setType(MessageType.TRACK);
        dump(message);
    }

    private void checkApplicationUpdateStatus(Application application) {
        try {
            int previousBuild = preferenceManager.getBuildNumber();
            String previousVersion = preferenceManager.getVersionName();
            RudderLogger.logDebug("Previous Installed Version: " + previousVersion);
            RudderLogger.logDebug("Previous Installed Build: " + previousBuild);
            String packageName = application.getPackageName();
            PackageManager packageManager = application.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            int currentBuild = 0;
            String currentVersion = packageInfo.versionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentBuild = (int) packageInfo.getLongVersionCode();
            } else {
                currentBuild = packageInfo.versionCode;
            }
            RudderLogger.logDebug("Current Installed Version: " + currentVersion);
            RudderLogger.logDebug("Current Installed Build: " + currentBuild);
            if (previousBuild == -1) {
                // application was not installed previously, Application Installed event
                applicationInstalledOrUpdated = true;
                preferenceManager.saveBuildNumber(currentBuild);
                preferenceManager.saveVersionName(currentVersion);
                sendApplicationInstalled(currentBuild, currentVersion);
                rudderFlushWorkManager.registerPeriodicFlushWorker();
            } else if (previousBuild != currentBuild) {
                applicationInstalledOrUpdated = true;
                preferenceManager.saveBuildNumber(currentBuild);
                preferenceManager.saveVersionName(currentVersion);
                sendApplicationUpdated(previousBuild, currentBuild, previousVersion, currentVersion);
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
                final ArrayList<Integer> messageIds = new ArrayList<>();
                final ArrayList<String> messages = new ArrayList<>();

                while (true) {
                    synchronized (MessageUploadLock.UPLOAD_LOCK) {
                        try {
                            // clear lists for reuse
                            messageIds.clear();
                            messages.clear();
                            checkIfDBThresholdAttained(messageIds, messages);
                            // fetch enough events to form a batch
                            RudderLogger.logDebug("EventRepository: processor: Fetching events to flush to server");
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
                                    networkResponse = flushEventsToServer(payload, config.getDataPlaneUrl(), authHeaderString, anonymousIdHeaderString);
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
                        } catch (Exception ex) {
                            RudderLogger.logError(ex);
                        }
                    }
                    // increment sleepCount to track total elapsed seconds
                    sleepCount += 1;
                    RudderLogger.logDebug(String.format(Locale.US, "EventRepository: processor: SleepCount: %d", sleepCount));
                    try {
                        if (networkResponse == Utils.NetworkResponses.WRITE_KEY_ERROR) {
                            RudderLogger.logInfo("Wrong WriteKey. Aborting");
                            break;
                        } else if (networkResponse == Utils.NetworkResponses.ERROR) {
                            RudderLogger.logInfo("flushEvents: Retrying in " + Math.abs(sleepCount - config.getSleepTimeOut()) + "s");
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
     * check if the number of events in the db crossed the dbCountThreshold then delete the older events which are in excess.
     */

    private void checkIfDBThresholdAttained(ArrayList<Integer> messageIds, ArrayList<String> messages) {
        // get current record count from db
        int recordCount = dbManager.getDBRecordCount();
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: checkIfDBThresholdAttained: DBRecordCount: %d", recordCount));
        // if record count exceeds threshold count, remove older events
        if (recordCount > config.getDbCountThreshold()) {
            // fetch extra old events
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: checkIfDBThresholdAttained: OldRecordCount: %d", (recordCount - config.getDbCountThreshold())));
            dbManager.fetchEventsFromDB(messageIds, messages, recordCount - config.getDbCountThreshold());
            // remove events
            dbManager.clearEventsFromDB(messageIds);
        }
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

        // Session Tracking
        message.setSession(userSession);
        if (config.isTrackLifecycleEvents() && config.isTrackAutoSession()) {
            if (userSession != null) {
                userSession.setLastEventTimeStamp(new Date().getTime());
            }
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

    void flushSync() {
//        synchronized (this){
        FlushUtils.flush(areFactoriesInitialized, integrationOperationsMap,
                config.getFlushQueueSize(), config.getDataPlaneUrl(),
                dbManager, authHeaderString, anonymousIdHeaderString);
//        }
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

    void cancelPeriodicFlushWorker() {
        rudderFlushWorkManager.cancelPeriodicFlushWorker();
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
                // Session Tracking
                // Automatic tracking session started
                if (config.isTrackAutoSession()) {
                    if (!applicationInstalledOrUpdated) {
                        if (userSession != null) {
                            userSession.startSessionIfNeeded();
                        }
                    } else {
                        applicationInstalledOrUpdated = false;
                    }
                }
                RudderMessage trackMessage;
                trackMessage = new RudderMessageBuilder()
                        .setEventName("Application Opened")
                        .setProperty(Utils.trackDeepLink(activity, isFirstLaunch, preferenceManager.getVersionName()))
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

    public void shutDown() {
        if (areFactoriesInitialized && integrationOperationsMap != null) {
            flushNativeSdks(integrationOperationsMap);
        }
    }

    public void startSession(String sessionId) {
        if (userSession == null) { return; }
        if (config != null) {
            if (config.isTrackAutoSession()) {
                endSession();
                config.setTrackAutoSession(false);
            }
        }
        userSession.startSession(sessionId);
    }

    public void endSession() {
        if (userSession == null) { return; }
        if (config != null) {
            if (config.isTrackAutoSession()) {
                config.setTrackAutoSession(false);
            }
        }
        userSession.clearSession();
    }
}