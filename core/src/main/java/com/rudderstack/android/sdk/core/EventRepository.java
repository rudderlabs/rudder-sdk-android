package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.RudderNetworkManager.NetworkResponses;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.addEndPoint;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import java.nio.charset.StandardCharsets;
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
    private RudderNetworkManager networkManager;
    private DBPersistentManager dbManager;
    private RudderServerConfigManager configManager;
    private RudderPreferenceManager preferenceManager;
    private RudderCloudModeManager cloudModeManager;
    private RudderDeviceModeManager deviceModeManager;
    private RudderFlushWorkManager rudderFlushWorkManager;

    static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();

    private boolean isSDKInitialized = false;
    private boolean isSDKEnabled = true;

    /**
     * constructor to be called from RudderClient internally.
     * tasks to be performed
     * 1. Set the values of writeKey, config
     * 2. Initiate RudderPreferenceManager
     * 3. Initiate RudderElementCache
     * 4. Initiate DBPersistentManager for SQLite operations
     * 5. Initiate RudderNetWorkManager for making Network Requests
     * 6. initiate RudderServerConfigManager
     * 7. Initiate FlushWorkManager
     * 8. Initiate Cloud Mode Manager and Device mode Manager
     * 9. Initiate ApplicationLifeCycleManager
     */
    EventRepository(Application _application, String _writeKey, RudderConfig _config, String _anonymousId, String _advertisingId, String _deviceToken) {
        // 1. set the values of writeKey, config
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: writeKey: %s", _writeKey));
        this.authHeaderString = Base64.encodeToString((String.format(Locale.US, "%s:", _writeKey)).getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: authHeaderString: %s", this.authHeaderString));

        Context context = _application.getApplicationContext();
        this.config = _config;
        RudderLogger.logDebug(String.format("EventRepository: constructor: %s", this.config.toString()));

        try {
            //2. Initiate RudderPreferenceManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderPreferenceManager");
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

            // 3. Initiate RudderElementCache
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderElementCache");
            // We first send the anonymousId to RudderElementCache which will just set the anonymousId static variable in RudderContext class.
            RudderElementCache.initiate(_application, _anonymousId, _advertisingId, _deviceToken, _config.isAutoCollectAdvertId());

            String anonymousId = RudderContext.getAnonymousId();
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousId: %s", anonymousId));
            this.anonymousIdHeaderString = Base64.encodeToString(anonymousId.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
            RudderLogger.logDebug(String.format(Locale.US, "EventRepository: constructor: anonymousIdHeaderString: %s", this.anonymousIdHeaderString));

            // 4. Initiate DBPersistentManager for SQLite operations
            RudderLogger.logDebug("EventRepository: constructor: Initiating DBPersistentManager");
            this.dbManager = DBPersistentManager.getInstance(_application);

            // 5. Initiate RudderNetWorkManager for making Network Requests
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderNetworkManager");
            this.networkManager = new RudderNetworkManager(authHeaderString, anonymousIdHeaderString);

            // 6. initiate RudderServerConfigManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderServerConfigManager");
            this.configManager = new RudderServerConfigManager(_application, _config, networkManager);

            // 7. Initiate FlushWorkManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating RudderFlushWorkManager");
            RudderFlushConfig rudderFlushConfig = new RudderFlushConfig(config.getDataPlaneUrl(), authHeaderString, anonymousIdHeaderString, config.getFlushQueueSize(), config.getLogLevel());
            this.rudderFlushWorkManager = new RudderFlushWorkManager(context, config, preferenceManager, rudderFlushConfig);

            // 8. Initiate Cloud Mode Manager and Device mode Manager
            RudderLogger.logDebug("EventRepository: constructor: Initiating processor and factories");
            this.cloudModeManager = new RudderCloudModeManager(dbManager, networkManager, config);
            this.deviceModeManager = new RudderDeviceModeManager(dbManager, networkManager, config);
            this.initiateSDK();

            // 9. Initiate ApplicationLifeCycleManager
            RudderLogger.logDebug("EventRepository: constructor: Initiating ApplicationLifeCycleManager");
            new ApplicationLifeCycleManager(_application, preferenceManager, this, rudderFlushWorkManager, config);

        } catch (Exception ex) {
            RudderLogger.logError(String.format("EventRepository: constructor: Exception Initializing the EventRepository Instance due to %s", ex.getLocalizedMessage()));
        }
    }

    private void initiateSDK() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int retryCount = 0;
                    while (!isSDKInitialized && retryCount <= 5) {
                        RudderServerConfig serverConfig = configManager.getConfig();
                        if (serverConfig != null) {
                            isSDKEnabled = serverConfig.source.isSourceEnabled;
                            if (isSDKEnabled) {
                                RudderLogger.logDebug("EventRepository: initiateSDK: Initiating Cloud Mode processor");
                                cloudModeManager.startCloudModeProcessor();
                                RudderLogger.logDebug("EventRepository: initiateSDK: Initiating Device Mode Manager");
                                deviceModeManager.initiate(serverConfig);
                            } else {
                                RudderLogger.logDebug("EventRepository: initiateSDK: source is disabled in the dashboard");
                                RudderLogger.logDebug("EventRepository: initiateSDK: Flushing persisted events");
                                dbManager.flushEvents();
                            }
                            isSDKInitialized = true;
                        } else if (configManager.getError() == NetworkResponses.WRITE_KEY_ERROR) {
                            RudderLogger.logError("EventRepository: initiateSDK: WRONG WRITE_KEY");
                            break;
                        } else {
                            retryCount += 1;
                            RudderLogger.logDebug("EventRepository: initiateSDK: retry count: " + retryCount);
                            RudderLogger.logInfo("EventRepository: initiateSDK: Retrying in " + retryCount * 2 + "s");
                            Thread.sleep(retryCount * 2 * 1000L);
                        }
                    }
                } catch (Exception ex) {
                    RudderLogger.logError(String.format(Locale.US, "EventRepository: initiateSDK: Exception initializing the SDK due to %s", ex.getLocalizedMessage()));
                }
            }
        }).start();
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
        String eventJson = gson.toJson(message);
        if (Utils.getUTF8Length(eventJson) > Utils.MAX_EVENT_SIZE) {
            RudderLogger.logError(String.format(Locale.US, "EventRepository: dump: Event size exceeds the maximum permitted event size(%d)", Utils.MAX_EVENT_SIZE));
            return;
        }
        RudderLogger.logVerbose(String.format(Locale.US, "EventRepository: dump: message: %s", eventJson));
        dbManager.saveEvent(eventJson);
        deviceModeManager.makeFactoryDump(message, false);
    }

    void reset() {
        deviceModeManager.reset();
    }

    void flushSync() {
        deviceModeManager.flush();
        FlushUtils.flush(config.getFlushQueueSize(), addEndPoint(config.getDataPlaneUrl(), RudderCloudModeManager.BATCH_ENDPOINT), dbManager, networkManager);
    }

    private Map<String, Object> prepareIntegrations() {
        Map<String, Object> integrationPlaceholder = new HashMap<>();
        integrationPlaceholder.put("All", true);
        return integrationPlaceholder;
    }

    void cancelPeriodicFlushWorker() {
        rudderFlushWorkManager.cancelPeriodicFlushWorker();
    }

    void onIntegrationReady(String key, RudderClient.Callback callback) {
        deviceModeManager.addCallBackForIntegration(key, callback);
    }

    void updateAnonymousId(@NonNull String anonymousId) {
        RudderLogger.logDebug(String.format(Locale.US, "EventRepository: updateAnonymousId: Updating AnonymousId: %s", anonymousId));
        RudderElementCache.updateAnonymousId(anonymousId);
        preferenceManager.saveAnonymousId(RudderContext.getAnonymousId());
        networkManager.updateAnonymousIdHeaderString();
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

    public void shutDown() {
        deviceModeManager.flush();
    }
}