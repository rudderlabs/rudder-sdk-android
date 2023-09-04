package com.rudderstack.android.sdk.core;


import static com.rudderstack.android.sdk.core.ReportManager.LABEL_TYPE;
import static com.rudderstack.android.sdk.core.ReportManager.LABEL_TYPE_DATA_PLANE_URL_INVALID;
import static com.rudderstack.android.sdk.core.ReportManager.LABEL_TYPE_SOURCE_DISABLED;
import static com.rudderstack.android.sdk.core.ReportManager.incrementDiscardedCounter;
import static com.rudderstack.android.sdk.core.ReportManager.initiateRudderReporter;
import static com.rudderstack.android.sdk.core.ReportManager.isStatsReporterAvailable;
import static com.rudderstack.android.sdk.core.util.Utils.lifeCycleDependenciesExists;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.ruddermetricsreporterandroid.Metrics;
import com.rudderstack.android.sdk.core.consent.ConsentFilterHandler;
import com.rudderstack.android.sdk.core.consent.RudderConsentFilter;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
 * utility class for event processing
 * */
class EventRepository {
    private String authHeaderString;
    private String anonymousIdHeaderString;
    private RudderConfig config;
    private DBPersistentManager dbManager;
    private RudderServerConfigManager configManager;
    private RudderPreferenceManager preferenceManager;
    private RudderFlushWorkManager rudderFlushWorkManager;
    private RudderNetworkManager networkManager;
    private RudderDataResidencyManager dataResidencyManager;
    private Application application;

    private RudderUserSessionManager userSessionManager;
    private LifeCycleManagerCompat lifeCycleManagerCompat;
    private @Nullable
    AndroidXLifeCycleManager androidXlifeCycleManager = null;

    private boolean isSDKInitialized = false;
    private boolean isSDKEnabled = true;

    private RudderCloudModeManager cloudModeManager;
    private RudderDeviceModeManager deviceModeManager;
    private ApplicationLifeCycleManager applicationLifeCycleManager;

    private @Nullable
    ConsentFilterHandler consentFilterHandler = null;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();
    private String dataPlaneUrl;
    private final String writeKey;

    private static final String CHARSET_UTF_8 = "UTF-8";

    // Handler instance associated with the main thread
    static final Handler HANDLER =
            new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    RudderLogger.logError("EventRepository: HANDLER: handleMessage: Unknown handler message received: " + msg.what);
                }
            };

    /*
     * constructor to be called from RudderClient internally.
     * */
    EventRepository(Application _application, RudderConfig _config, Identifiers identifiers) {
        // 1. set the values of writeKey, config
        updateAuthHeaderString(identifiers.writeKey);
        Context context = _application.getApplicationContext();
        this.config = _config;
        this.application = _application;
        this.writeKey = identifiers.writeKey;
        RudderLogger.logDebug(String.format("EventRepository: constructor: %s", this.config.toString()));

        try {

            // initiate RudderPreferenceManager
            initiatePreferenceManager(_application);

            clearAnonymousIdIfRequired();

            // initiate RudderElementCache
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderElementCache");
            initiateRudderElementCache(application, config, identifiers);
            updateAnonymousIdHeaderString();

            // initiate DBPersistentManager for SQLite operations
            RudderLogger.logDebug("EventRepository: constructor: Initiating DBPersistentManager and starting Handler thread");
            initializeDbManager(_application);

            // initiate RudderNetworkManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderNetworkManager");
            this.networkManager = new RudderNetworkManager(authHeaderString, anonymousIdHeaderString, getSavedAuthToken(), config.isGzipEnabled());

            if (identifiers.authToken != null) {
                updateAuthToken(identifiers.authToken);
            }

            // initiate RudderServerConfigManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderServerConfigManager");
            this.configManager = new RudderServerConfigManager(_application, _config, networkManager);

            // initiate RudderFlushWorkManager
            rudderFlushWorkManager = new RudderFlushWorkManager(context, config, preferenceManager);

            // initiate RudderDataResidencyManager
            this.dataResidencyManager = new RudderDataResidencyManager(config);

            // initiate Cloud Mode Manager and Device mode Manager
            RudderLogger.logDebug("EventRepository: constructor: Initiating processor and factories");
            this.cloudModeManager = new RudderCloudModeManager(dbManager, networkManager, config, dataResidencyManager);
            this.deviceModeManager = new RudderDeviceModeManager(dbManager, networkManager, config, dataResidencyManager);

            // initiate SDK
            this.initiateSDK(_config.getConsentFilter());

            // initiate RudderUserSessionManager
            this.userSessionManager = new RudderUserSessionManager(this.preferenceManager, this.config);
            this.userSessionManager.startSessionTracking();

            // initiate ApplicationLifeCycleManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating ApplicationLifeCycleManager");
            AppVersion appVersion = new AppVersion(application);
            this.applicationLifeCycleManager = new ApplicationLifeCycleManager(config, appVersion, rudderFlushWorkManager, this, preferenceManager);
            this.applicationLifeCycleManager.trackApplicationUpdateStatus();

            initializeLifecycleTracking(applicationLifeCycleManager);

            //initiate rudder reporter
            RudderServerConfig serverConfig = configManager.getConfig();
            if(serverConfig != null && serverConfig.source != null
                    && serverConfig.source.sourceConfiguration != null) {
                enableStatsCollection(serverConfig.source.sourceConfiguration.getStatsCollection());
            }
            else {
                RudderLogger.logDebug("EventRepository: constructor: Metrics collection is disabled");
            }
        } catch (Exception ex) {
            RudderLogger.logError("EventRepository: constructor: Exception occurred: " + ex.getMessage());
            RudderLogger.logError(ex.getCause());
        }
    }


    // If the collectDeviceId flag is set to false, then check if deviceId is being used as anonymousId, if yes then clear it
    private void clearAnonymousIdIfRequired() {
        if (this.config.isCollectDeviceId()) return;
        String currentAnonymousIdValue = this.preferenceManager.getCurrentAnonymousIdValue();
        String deviceId = Utils.getDeviceId(application);
        if (currentAnonymousIdValue == null || deviceId == null) return;
        if (currentAnonymousIdValue.equals(deviceId)) {
            RudderLogger.logDebug("EventRepository: clearAnonymousIdIfRequired: Starting from version 1.18.0, we are breaking the relation between anonymousId and device Id. Hence clearing the anonymousId");
            this.preferenceManager.clearCurrentAnonymousIdValue();
        }
    }

    private void initializeLifecycleTracking(ApplicationLifeCycleManager applicationLifeCycleManager) {
        if (config.isNewLifeCycleEvents()) {
            boolean isAndroidXConfigSuccess = configureAndroidXLifeCycleTracking(applicationLifeCycleManager);
            if (!isAndroidXConfigSuccess) {
                config.setNewLifeCycleEvents(false);
            }
        }
        this.lifeCycleManagerCompat = new LifeCycleManagerCompat(this, config, applicationLifeCycleManager, userSessionManager);
        this.application.registerActivityLifecycleCallbacks(this.lifeCycleManagerCompat);


    }

    private boolean configureAndroidXLifeCycleTracking(ApplicationLifeCycleManager applicationLifeCycleManager) {
        if (lifeCycleDependenciesExists()) {
            this.androidXlifeCycleManager = new AndroidXLifeCycleManager(applicationLifeCycleManager, userSessionManager);
            run(() -> ProcessLifecycleOwner.get().getLifecycle().addObserver(androidXlifeCycleManager));
            return true;
        } else {
            RudderLogger.logWarn("EventRepository: constructor: Required Dependencies are not present in the classpath. " +
                    "Please add them to enable new lifecycle events. Using lifecycle callbacks");
        }

        return false;
    }

    private void initializeDbManager(Application application) {
        RudderConfig.DBEncryption dbEncryption = config.getDbEncryption();
        DBPersistentManager.DbManagerParams dbManagerParams = new DBPersistentManager.DbManagerParams(dbEncryption.enable,
                dbEncryption.getPersistenceProviderFactoryClassName(), dbEncryption.key);
        this.dbManager = DBPersistentManager.getInstance(application,dbManagerParams);
        dbManager.checkForMigrations();
        dbManager.startHandlerThread();
    }

    private void initiatePreferenceManager(Application application) {
        preferenceManager = RudderPreferenceManager.getInstance(application);
        preferenceManager.performMigration();
    }

    private void initiateRudderElementCache(Application application, RudderConfig config, Identifiers identifiers) {
        if (preferenceManager.getOptStatus()) {
            RudderLogger.logDebug("User Opted out for tracking the activity, hence dropping the identifiers");
            RudderElementCache.initiate(application, null, null, null, config.isAutoCollectAdvertId(), config.isCollectDeviceId());
        } else {
            // We first send the anonymousId to RudderElementCache which will just set the anonymousId static variable in RudderContext class.
            RudderElementCache.initiate(application, identifiers.anonymousId,
                    identifiers.advertisingId, identifiers.deviceToken, config.isAutoCollectAdvertId(), config.isCollectDeviceId());
        }
    }

    private void updateAnonymousIdHeaderString() throws UnsupportedEncodingException {
        String anonymousId = RudderContext.getAnonymousId();
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousId: %s", anonymousId));
        this.anonymousIdHeaderString = Base64.encodeToString(anonymousId.getBytes(CHARSET_UTF_8), Base64.NO_WRAP);
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousIdHeaderString: %s", this.anonymousIdHeaderString));
    }

    private void updateAuthHeaderString(String writeKey) {
        try {
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: writeKey: %s", writeKey));
            this.authHeaderString = Base64.encodeToString((String.format(Locale.US, "%s:", writeKey)).getBytes(CHARSET_UTF_8), Base64.NO_WRAP);
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
        this.writeKey = "";
    }

    private void initiateSDK(@Nullable RudderConsentFilter consentFilter) {
        new Thread(() -> {
            try {
                int retryCount = 0;
                while (!isSDKInitialized && retryCount <= 10) {
                    if (retryCount > 0) {
                        ReportManager.incrementSourceConfigDownloadRetryCounter(1);
                    }
                    RudderNetworkManager.NetworkResponses receivedError = configManager.getError();
                    RudderServerConfig serverConfig = configManager.getConfig();
                    if (serverConfig != null) {
                        isSDKEnabled = serverConfig.source.isSourceEnabled;
                        if (isSDKEnabled) {
                            if(serverConfig.source.sourceConfiguration != null)
                                enableStatsCollection(serverConfig.source.sourceConfiguration.getStatsCollection());
                            dataResidencyManager.setDataResidencyUrls(serverConfig);
                            dataPlaneUrl = dataResidencyManager.getDataPlaneUrl();
                            if (dataPlaneUrl == null) {
                                RudderLogger.logError(Constants.Logs.DATA_PLANE_URL_ERROR);
                                ReportManager.incrementSourceConfigDownloadAbortCounter(1,
                                        Collections.singletonMap(LABEL_TYPE, LABEL_TYPE_DATA_PLANE_URL_INVALID));
                                return;
                            }
                            if (consentFilter != null)
                                this.consentFilterHandler = new ConsentFilterHandler(serverConfig.source, consentFilter);
                            cloudModeManager.startCloudModeProcessor();
                            RudderLogger.logDebug("EventRepository: initiateSDK: Initiating Device Mode Manager");
                            deviceModeManager.initiate(serverConfig, consentFilterHandler);

                            RudderLogger.logDebug("DataPlaneUrl is set to: " + dataPlaneUrl);
                            ReportManager.incrementSourceConfigDownloadSuccessCounter(1);
                            saveFlushConfig();
                        } else {
                            ReportManager.incrementSourceConfigDownloadAbortCounter(1,
                                    Collections.singletonMap(LABEL_TYPE, LABEL_TYPE_SOURCE_DISABLED));
                            RudderLogger.logDebug("EventRepository: initiateSDK: source is disabled in the dashboard");
                            RudderLogger.logDebug("Flushing persisted events");
                            dbManager.flushEvents();
                        }

                        isSDKInitialized = true;
                    } else if (receivedError == RudderNetworkManager.NetworkResponses.WRITE_KEY_ERROR) {
                        RudderLogger.logError("WRONG WRITE_KEY");
                        // we do not need this metric
//                        ReportManager.incrementSourceConfigDownloadAbortCounter(1,
//                                Collections.singletonMap(LABEL_TYPE, LABEL_TYPE_WRITE_KEY_INVALID));
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

    private void enableStatsCollection( @NonNull SourceConfiguration.StatsCollection statsCollection) {
        if(!isStatsReporterAvailable()){
            if(statsCollection.getMetrics().isEnabled()){
                RudderLogger.logDebug("EventRepository: Enabling Metrics Collection:");
                initiateRudderReporter(application, writeKey );
            }
            return;
        }
        Metrics rudderMetrics = ReportManager.getMetrics();
        if(rudderMetrics == null)
            return;
        boolean metricsCollection = statsCollection.getMetrics().isEnabled();
        if (!metricsCollection) {
            RudderLogger.logDebug("EventRepository: Disabling Metrics Collection:");
            rudderMetrics.enable(false);
        }
    }

    private void saveFlushConfig() {
        RudderFlushConfig rudderFlushConfig = new RudderFlushConfig(dataPlaneUrl, authHeaderString,
                anonymousIdHeaderString, config.getFlushQueueSize(), config.getLogLevel(), config.isGzipEnabled(),
                config.getDbEncryption().enable, config.getDbEncryption().key);
        rudderFlushWorkManager.saveRudderFlushConfig(rudderFlushConfig);
    }


    /*
     * generic method for dumping all the events
     * */
    void processMessage(@NonNull RudderMessage message) {
        if (!isSDKEnabled) {
            incrementDiscardedCounter(1, Collections.singletonMap(LABEL_TYPE, ReportManager.LABEL_TYPE_SDK_DISABLED));
            return;
        }
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: dump: eventName: %s", message.getEventName()));

        applyRudderOptionsToMessageIntegrations(message);
        RudderMessage updatedMessage = updateMessageWithConsentedDestinations(message);
        userSessionManager.applySessionTracking(updatedMessage);

        String eventJson = gson.toJson(updatedMessage);
        RudderLogger.logVerbose(String.format(Locale.US, "EventRepository: dump: message: %s", eventJson));
        if (isMessageJsonExceedingMaxSize(eventJson)) {
            incrementDiscardedCounter(1, Collections.singletonMap(LABEL_TYPE, ReportManager.LABEL_TYPE_MSG_SIZE_INVALID));
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

    void flushSync() {
        if (dataPlaneUrl == null) {
            RudderLogger.logError(Constants.Logs.DATA_PLANE_URL_FLUSH_ERROR);
            return;
        }
        deviceModeManager.flush();
        FlushUtils.flushToServer(
                config.getFlushQueueSize(), dataPlaneUrl,
                dbManager, networkManager);
    }

    private Map<String, Object> prepareIntegrations() {
        Map<String, Object> integrationPlaceholder = new HashMap<>();
        integrationPlaceholder.put("All", true);
        return integrationPlaceholder;
    }

    void reset() {
        deviceModeManager.reset();
        RudderLogger.logDebug("EventRepository: reset: resetting the SDK");
        userSessionManager.reset();
        refreshAuthToken();
    }

    void refreshAuthToken() {
        preferenceManager.saveAuthToken(null);
        networkManager.updateDMTCustomToken(null);
    }

    void cancelPeriodicFlushWorker() {
        rudderFlushWorkManager.cancelPeriodicFlushWorker();
    }

    void onIntegrationReady(String key, RudderClient.Callback callback) {
        deviceModeManager.addCallBackForIntegration(key, callback);
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: onIntegrationReady: callback registered for %s", key));
    }

    void updateAuthToken(@NonNull String authToken) {
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: updateAuthToken: Updating AuthToken: %s", authToken));
        preferenceManager.saveAuthToken(authToken);
        networkManager.updateDMTCustomToken(authToken);
    }

    private @Nullable
    String getSavedAuthToken() {
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
     * Executes the runnable on the main thread of the App
     */
    private void run(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            HANDLER.post(runnable);
        }
    }

    /**
     * @return optOut status
     */
    boolean getOptStatus() {
        if (preferenceManager == null)
            return false;
        return preferenceManager.getOptStatus();
    }

    void updateAnonymousId(@NonNull String anonymousId) {
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: updateAnonymousId: Updating AnonymousId: %s", anonymousId));
        RudderElementCache.updateAnonymousId(anonymousId);
        preferenceManager.saveAnonymousId(RudderContext.getAnonymousId());
        try {
            this.anonymousIdHeaderString = Base64.encodeToString(RudderContext.getAnonymousId().getBytes(CHARSET_UTF_8), Base64.NO_WRAP);
        } catch (Exception ex) {
            RudderLogger.logError(ex.getCause());
        }
        networkManager.updateAnonymousIdHeaderString();
    }

    public void shutDown() {
        this.deviceModeManager.flush();
        this.application.unregisterActivityLifecycleCallbacks(lifeCycleManagerCompat);
        if (androidXlifeCycleManager != null)
            run(() -> ProcessLifecycleOwner.get().getLifecycle().removeObserver(androidXlifeCycleManager));
    }

    public void startSession(Long sessionId) {
        userSessionManager.startSession(sessionId);
    }

    public void endSession() {
        userSessionManager.endSession();
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
        public Identifiers(String writeKey, String deviceToken, String anonymousId, String advertisingId, String authToken) {
            this.writeKey = writeKey;
            this.deviceToken = deviceToken;
            this.anonymousId = anonymousId;
            this.advertisingId = advertisingId;
            this.authToken = authToken;
        }

        private final String writeKey;
        private final String deviceToken;
        private final String anonymousId;
        private final String advertisingId;
        private final String authToken;
    }
}