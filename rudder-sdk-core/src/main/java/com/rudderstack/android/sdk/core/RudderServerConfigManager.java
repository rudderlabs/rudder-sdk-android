package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class RudderServerConfigManager {
    private static RudderPreferenceManager preferenceManger;
    private static RudderServerConfigManager instance;
    private static RudderServerConfig serverConfig;
    private static RudderConfig rudderConfig;
    private RudderHttpClient rudderHttpClient;
    private MetricsStatsManager metricsStatsManager;
    private DBPersistentManager dbPersistentManager;
    private Map<String, Object> integrationsMap = null;

    static RudderServerConfigManager getInstance(Application _application, RudderConfig config) {
        if (instance == null) {
            RudderLogger.logDebug("Creating RudderServerConfigManager instance");
            instance = new RudderServerConfigManager(_application, config);
        }
        return instance;
    }

    private RudderServerConfigManager(Application _application, RudderConfig _config) {
        rudderHttpClient = RudderHttpClient.getInstance();
        dbPersistentManager = DBPersistentManager.getInstance(_application);
        metricsStatsManager = MetricsStatsManager.getInstance(_application);
        preferenceManger = RudderPreferenceManager.getInstance(_application);
        serverConfig = retrieveConfig();
        rudderConfig = _config;
        boolean isConfigOutdated = isServerConfigOutDated();
        if (serverConfig == null) {
            RudderLogger.logDebug("Server config is not present in preference storage. downloading config");
            downloadConfig();
        } else {
            if (isConfigOutdated) {
                RudderLogger.logDebug("Server config is outdated. downloading config again");
                downloadConfig();
            } else {
                RudderLogger.logDebug("Server config found. Using existing config");
            }
        }
    }

    // update config if it is older than an day
    private boolean isServerConfigOutDated() {
        long lastUpdatedTime = preferenceManger.getLastUpdatedTime();
        RudderLogger.logDebug(String.format(Locale.US, "Last updated config time: %d", lastUpdatedTime));
        RudderLogger.logDebug(String.format(Locale.US, "ServerConfigInterval: %d", rudderConfig.getConfigRefreshInterval()));
        if (lastUpdatedTime == -1) return true;

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUpdatedTime) > (rudderConfig.getConfigRefreshInterval() * 60 * 60 * 1000);
    }

    private RudderServerConfig retrieveConfig() {
        String configJson = preferenceManger.getConfigJson();
        RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: retrieveConfig: configJson: %s", configJson));
        if (configJson == null) return null;
        return new Gson().fromJson(configJson, RudderServerConfig.class);
    }

    private void downloadConfig() {
        // don't try to download anything if writeKey is not valid
        if (TextUtils.isEmpty(RudderClient.getWriteKey())) return;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDone = false;
                int retryCount = 0, retryTimeOut = 10;
                String configUrl = null;
                configUrl = String.format(Locale.US, "%s/sourceConfig?writeKey=%s&fingerPrint=%s", Constants.CONFIG_PLANE_URL, RudderClient.getWriteKey(), preferenceManger.getRudderStatsFingerPrint());
                RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: configUrl: %s", configUrl));
                while (!isDone && retryCount <= 3) {
                    long requestStartTime = System.currentTimeMillis();
                    String configJson = rudderHttpClient.get(configUrl, null);
                    long requestEndTime = System.currentTimeMillis();
                    if (metricsStatsManager.isEnabled()) {
                        dbPersistentManager.recordConfigPlaneRequest(configJson != null ? 1 : 0, (requestEndTime - requestStartTime));
                    }
                    if (configJson != null) {
                        RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: configJson: %s", configJson));
                        // save config for future use
                        preferenceManger.updateLastUpdatedTime();
                        preferenceManger.saveConfigJson(configJson);
                        // update server config as well
                        serverConfig = new Gson().fromJson(configJson, RudderServerConfig.class);
                        // reset retry count
                        isDone = true;
                        RudderLogger.logInfo("RudderServerConfigManager: downloadConfig: server config download successful");
                    } else {
                        retryCount += 1;
                        try {
                            Thread.sleep(retryCount * retryTimeOut * 1000);
                        } catch (InterruptedException e) {
                            // ignored
                        }
                    }
                }
            }
        });
        RudderLogger.logVerbose("Download Thread Id:" + thread.getId());
        thread.start();
    }

    RudderServerConfig getConfig() {
        if (serverConfig == null) serverConfig =

                retrieveConfig();
        return serverConfig;
    }

    Map<String, Object> getIntegrations() {
        if (integrationsMap == null) {
            this.integrationsMap = new HashMap<>();
            for (RudderServerDestination destination : serverConfig.source.destinations) {
                if (!this.integrationsMap.containsKey(destination.destinationDefinition.definitionName))
                    this.integrationsMap.put(destination.destinationDefinition.definitionName, destination.isDestinationEnabled);
            }
        }
        return this.integrationsMap;
    }
}
