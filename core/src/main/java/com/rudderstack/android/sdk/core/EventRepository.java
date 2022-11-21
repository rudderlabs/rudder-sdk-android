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
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.consent.ConsentFilterHandler;
import com.rudderstack.android.sdk.core.consent.RudderConsentFilter;
import com.rudderstack.android.sdk.core.util.MessageUploadLock;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * utility class for event processing
 * */
class EventRepository {
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
    private final Map<String, RudderIntegration<?>> integrationOperationsMap = new HashMap<>();
    private final Map<String, RudderClient.Callback> integrationCallbacks = new HashMap<>();
    private RudderNetworkManager networkManager;

    private ApplicationLifeCycleManager applicationLifeCycleManager;

    private boolean isSDKInitialized = false;
    private boolean isSDKEnabled = true;
    private boolean areFactoriesInitialized = false;
    private final AtomicBoolean isFirstLaunch = new AtomicBoolean(true);

    private RudderCloudModeManager cloudModeManager;
    private RudderDeviceModeManager deviceModeManager;

    private @Nullable
    ConsentFilterHandler consentFilterHandler = null;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();
    private String dataPlaneUrl;

    private static final String CHARSET_UTF_8 = "UTF-8";

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
    EventRepository(Application _application, RudderConfig _config, Identifiers identifiers) {
        // 1. set the values of writeKey, config
        updateAuthHeaderString(identifiers.writeKey);
        this.context = _application.getApplicationContext();
        this.config = _config;
        RudderLogger.logDebug(String.format("EventRepository: constructor: %s", this.config.toString()));

        try {
            // initiate RudderPreferenceManager
            initiatePreferenceManager(_application, config, identifiers);
            updateAnonymousIdHeaderString();

            // 3. initiate DBPersistentManager for SQLite operations
            RudderLogger.logDebug("EventRepository: constructor: Initiating DBPersistentManager and starting Handler thread");
            this.dbManager = DBPersistentManager.getInstance(_application);
            this.dbManager.checkForMigrations();
            this.dbManager.startHandlerThread();

            // 4. initiate RudderServerConfigManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderServerConfigManager");
            this.configManager = new RudderServerConfigManager(_application, _config, networkManager);

            // 5. Initiate RudderNetWorkManager for making Network Requests
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderNetworkManager");
            this.networkManager = new RudderNetworkManager(authHeaderString, anonymousIdHeaderString, getSavedAuthToken());

            // 6. initiate FlushWorkManager
            rudderFlushWorkManager = new RudderFlushWorkManager(context, config, preferenceManager);

            // 7. initiate RudderUserSession for tracking sessions
            // 8. Initiate ApplicationLifeCycleManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating ApplicationLifeCycleManager");
            this.applicationLifeCycleManager = new ApplicationLifeCycleManager(_application, preferenceManager, this, rudderFlushWorkManager, config);

            // 9. Initiate Cloud Mode Manager and Device mode Manager
            RudderLogger.logDebug("EventRepository: constructor: Initiating processor and factories");
            this.cloudModeManager = new RudderCloudModeManager(dbManager, networkManager, config);
            this.deviceModeManager = new RudderDeviceModeManager(dbManager, networkManager, config);

            this.initiateSDK(_config.getConsentFilter());




        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
    }

    private void updateAnonymousIdHeaderString() throws UnsupportedEncodingException {
        String anonymousId = RudderContext.getAnonymousId();
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousId: %s", anonymousId));
        this.anonymousIdHeaderString = Base64.encodeToString(anonymousId.getBytes(CHARSET_UTF_8), Base64.DEFAULT);
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousIdHeaderString: %s", this.anonymousIdHeaderString));
    }



    private void initiatePreferenceManager(Application application, RudderConfig config, Identifiers identifiers) {
        preferenceManager = RudderPreferenceManager.getInstance(application);
        preferenceManager.performMigration();
        RudderLogger.logDebug("EventRepository: constructor: Initiating RudderElementCache");
        // 2. initiate RudderElementCache
        if (preferenceManager.getOptStatus()) {
            RudderLogger.logDebug("User Opted out for tracking the activity, hence dropping the identifiers");
            RudderElementCache.initiate(application, null, null, null, config.isAutoCollectAdvertId());
        } else {
            // We first send the anonymousId to RudderElementCache which will just set the anonymousId static variable in RudderContext class.
            RudderElementCache.initiate(application, identifiers.anonymousId,
                    identifiers.advertisingId, identifiers.deviceToken, config.isAutoCollectAdvertId());
        }
    }

    private void updateAuthHeaderString(String writeKey) {

        try {
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: writeKey: %s", writeKey));
            this.authHeaderString = Base64.encodeToString((String.format(Locale.US, "%s:", writeKey)).getBytes(CHARSET_UTF_8), Base64.DEFAULT);
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: authHeaderString: %s", this.authHeaderString));
        } catch (UnsupportedEncodingException ex) {
            RudderLogger.logError(ex);
        }
    }

    //used for testing purpose
    @VisibleForTesting
    EventRepository() {
        //using this constructor requires mocking of all objects that are initialised in
        //proper constructor
    }

    private void initiateSDK(@Nullable RudderConsentFilter consentFilter) {
        new Thread(() -> {
            try {
                int retryCount = 0;
                while (!isSDKInitialized && retryCount <= 5) {
                    RudderNetworkManager.NetworkResponses receivedError = configManager.getError();
                    RudderServerConfig serverConfig = configManager.getConfig();
                    if (serverConfig != null) {
                        isSDKEnabled = serverConfig.source.isSourceEnabled;
                        if (isSDKEnabled) {
                            dataPlaneUrl = getDataPlaneUrlWrtResidencyConfig(serverConfig, config);
                            if (dataPlaneUrl == null) {
                                RudderLogger.logError(Constants.Logs.DATA_PLANE_URL_ERROR);
                                return;
                            }
                            cloudModeManager.startCloudModeProcessor();
                            RudderLogger.logDebug("EventRepository: initiateSDK: Initiating Device Mode Manager");
                            deviceModeManager.initiate(serverConfig);

                            RudderLogger.logDebug("DataPlaneUrl is set to: " + dataPlaneUrl);

                            saveFlushConfig();

                            // initiate processor
//                            initiateProcessor();
                            if (consentFilter != null)
                                this.consentFilterHandler = new ConsentFilterHandler(serverConfig.source, consentFilter);
                            // initiate factories
                            setupNativeFactoriesWithFiltering(serverConfig.source.getDestinations());

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
                    } else if (receivedError == RudderNetworkManager.NetworkResponses.WRITE_KEY_ERROR) {
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
        }).start();
    }

    private void setupNativeFactoriesWithFiltering(List<RudderServerDestination> destinations) {
        if (destinations == null) {
            RudderLogger.logDebug("EventRepository: initiateSDK: No native SDKs are found");
            return;

        }
        List<RudderServerDestination> consentedDestinations = consentFilterHandler !=
                null ? consentFilterHandler.filterDestinationList(destinations) : destinations;
        initiateFactories(consentedDestinations);
        RudderLogger.logDebug("EventRepository: initiating event filtering plugin for device mode destinations");
        rudderEventFilteringPlugin = new RudderEventFilteringPlugin(consentedDestinations);
    }

    @Nullable
    @VisibleForTesting
    String getDataPlaneUrlWrtResidencyConfig(RudderServerConfig serverConfig, RudderConfig config) {
        RudderDataResidencyManager rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        String dataPlaneUrl = rudderDataResidencyManager.getDataResidencyUrl();
        if (Utils.isEmpty(dataPlaneUrl)) {
            dataPlaneUrl = config.getDataPlaneUrl();
        }
        return dataPlaneUrl;
    }

    private void saveFlushConfig() {
        RudderFlushConfig rudderFlushConfig = new RudderFlushConfig(dataPlaneUrl, authHeaderString, anonymousIdHeaderString, config.getFlushQueueSize(), config.getLogLevel());
        rudderFlushWorkManager.saveRudderFlushConfig(rudderFlushConfig);
    }

   /* private void initiateProcessor() {
        RudderLogger.logDebug("EventRepository: initiateSDK: Initiating processor");
        Thread processorThread = new Thread(getProcessorRunnable());
        processorThread.start();
    }*/

    private void sendApplicationInstalled(int currentBuild, String currentVersion) {
        // If trackLifeCycleEvents is not allowed then discard the event
        if (!config.isTrackLifecycleEvents()) {
            return;
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
        processMessage(message);
    }

    private void sendApplicationUpdated(int previousBuild, int currentBuild, String previousVersion, String currentVersion) {
        // If either optOut() is set to true or LifeCycleEvents set to false then discard the event
        if (getOptStatus() || !config.isTrackLifecycleEvents()) {
            return;
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
        processMessage(message);
    }

    private void updatePreferenceManagerWithApplicationUpdateStatus(Application application) {
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
                preferenceManager.saveBuildNumber(currentBuild);
                preferenceManager.saveVersionName(currentVersion);
                sendApplicationInstalled(currentBuild, currentVersion);
                rudderFlushWorkManager.registerPeriodicFlushWorker();
            } else if (previousBuild != currentBuild) {
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
            return;
        }
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

    /*
     * check if the number of events in the db crossed the dbCountThreshold then delete the older events which are in excess.
     */

   /* private void checkIfDBThresholdAttained(ArrayList<Integer> messageIds, ArrayList<String> messages) {
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
    }*/


    /*
     * generic method for dumping all the events
     * */
    void processMessage(@NonNull RudderMessage message) {
        if (!isSDKEnabled) {
            return;
        }
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: dump: eventName: %s", message.getEventName()));

        applyRudderOptionsToMessageIntegrations(message);
        RudderMessage updatedMessage = updateMessageWithConsentedDestinations(message);
        applicationLifeCycleManager.applySessionTracking(updatedMessage);

        String eventJson = gson.toJson(updatedMessage);
        makeFactoryDump(updatedMessage, false);
        RudderLogger.logVerbose(String.format(Locale.US, "EventRepository: dump: message: %s", eventJson));
        if (isMessageJsonExceedingMaxSize(eventJson)) {
            RudderLogger.logError(String.format(Locale.US, "EventRepository: dump: Event size exceeds the maximum permitted event size(%d)", Utils.MAX_EVENT_SIZE));
            return;
        }
        dbManager.saveEvent(eventJson, new EventInsertionCallback(message, deviceModeManager));
    }

    private boolean isMessageJsonExceedingMaxSize(String eventJson) {
        return Utils.getUTF8Length(eventJson) > Utils.MAX_EVENT_SIZE;
    }

    @VisibleForTesting
    void applyRudderOptionsToMessageIntegrations(RudderMessage message) {
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
        if (dataPlaneUrl == null) {
            RudderLogger.logError(Constants.Logs.DATA_PLANE_URL_FLUSH_ERROR);
            return;
        }
        FlushUtils.flush(areFactoriesInitialized, integrationOperationsMap,
                config.getFlushQueueSize(), dataPlaneUrl,
                dbManager,networkManager);
    }

    private Map<String, Object> prepareIntegrations() {
        Map<String, Object> integrationPlaceholder = new HashMap<>();
        integrationPlaceholder.put("All", true);
        return integrationPlaceholder;
    }

    void reset() {
        deviceModeManager.reset();
        RudderLogger.logDebug("EventRepository: reset: resetting the SDK");
        applicationLifeCycleManager.reset();
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
        refreshAuthToken();
    }
    void refreshAuthToken() {
        preferenceManager.saveAuthToken(null);
        networkManager.updateDMTHeaderString(null);
    }
    void cancelPeriodicFlushWorker() {
        rudderFlushWorkManager.cancelPeriodicFlushWorker();
    }

    void onIntegrationReady(String key, RudderClient.Callback callback) {
        deviceModeManager.addCallBackForIntegration(key, callback);
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: onIntegrationReady: callback registered for %s", key));
        integrationCallbacks.put(key, callback);
    }

    void updateAuthToken(@NonNull String authToken) {
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: updateAuthToken: Updating AuthToken: %s", authToken));
        preferenceManager.saveAuthToken(authToken);
        networkManager.updateDMTHeaderString(authToken);
    }

    private @Nullable String getSavedAuthToken(){
        return preferenceManager.getAuthToken();
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
            this.anonymousIdHeaderString = Base64.encodeToString(RudderContext.getAnonymousId().getBytes(CHARSET_UTF_8), Base64.DEFAULT);
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
        networkManager.updateAnonymousIdHeaderString();
    }

    public void shutDown() {
        if (areFactoriesInitialized && integrationOperationsMap != null) {
            flushNativeSdks(integrationOperationsMap);
        }
        deviceModeManager.flush();
    }

    public void startSession(Long sessionId) {
       applicationLifeCycleManager.startSession(sessionId);
    }

    public void endSession() {
        applicationLifeCycleManager.endSession();
    }

    private RudderMessage updateMessageWithConsentedDestinations(RudderMessage message) {
        RudderClient rudderClient = RudderClient.getInstance();
        if (rudderClient == null)
            return message;

        if (consentFilterHandler == null) {
            return message;
        }
        return applyConsentFiltersToMessage(message, consentFilterHandler, configManager.getConfig());
    }

    @VisibleForTesting
    @NonNull
    RudderMessage applyConsentFiltersToMessage(@NonNull RudderMessage rudderMessage,
                                               @NonNull ConsentFilterHandler consentFilter,
                                               RudderServerConfig serverConfig) {
        if (serverConfig == null) {
            return rudderMessage;
        }
        RudderServerConfigSource sourceConfig = serverConfig.source;
        if (sourceConfig == null)
            return rudderMessage;
        return consentFilter.applyConsent(rudderMessage);
    }

    //model
    static class Identifiers {
        public Identifiers(String writeKey, String deviceToken, String anonymousId, String advertisingId) {
            this.writeKey = writeKey;
            this.deviceToken = deviceToken;
            this.anonymousId = anonymousId;
            this.advertisingId = advertisingId;
        }

        private final String writeKey;
        private final String deviceToken;
        private final String anonymousId;
        private final String advertisingId;


    }
}