package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class RudderServerConfigManager {
    private static RudderPreferenceManager preferenceManger;
    private static RudderServerConfigManager instance;
    private static RudderServerConfig serverConfig;
    private static RudderConfig rudderConfig;
    private Map<String, Object> integrationsMap = null;
    private Utils.NetworkResponses receivedError = Utils.NetworkResponses.SUCCESS;


    RudderServerConfigManager(Application _application, String _writeKey, RudderConfig _config) {
        preferenceManger = RudderPreferenceManager.getInstance(_application);
        rudderConfig = _config;
        // fetch server config
        RudderLogger.logDebug(String.format("Downloading server config for writeKey: %s", _writeKey));
        boolean downloadSuccessful =  downloadConfig(_writeKey);
        if (!downloadSuccessful) {
            RudderLogger.logDebug("Server config download failed.Using the last saved config from storage");
            // retrieve last saved config from storage
            serverConfig = retrieveConfig();
            if (serverConfig == null) {
                RudderLogger.logDebug("Server config retrieval failed.No config found in storage");
                RudderLogger.logError(String.format("Failed to fetch server config for writeKey: %s", _writeKey));
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

    private boolean downloadConfig(final String _writeKey) {
        // don't try to download anything if writeKey is not valid
        if (TextUtils.isEmpty(_writeKey)) {
            receivedError = Utils.NetworkResponses.WRITE_KEY_ERROR;
            return false;
        }
        final boolean[] isDone = {false};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isDone[0] = false;
                int retryCount = 0, retryTimeOut = 10;
                while (!isDone[0] && retryCount <= 3) {
                    try {
                        String configUrl = rudderConfig.getControlPlaneUrl() + "sourceConfig";
                        RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: configUrl: %s", configUrl));
                        // create url object
                        URL url = new URL(configUrl);
                        // get connection object
                        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                        // set request method
                        httpConnection.setRequestMethod("GET");
                        // add basic auth_header
                        httpConnection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((_writeKey + ":").getBytes("UTF-8"), Base64.DEFAULT));
                        // create connection
                        httpConnection.connect();
                        RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: response status code: %d", httpConnection.getResponseCode()));
                        if (httpConnection.getResponseCode() == 200) {
                            // get input stream from connection to get output from the server
                            BufferedInputStream bis = new BufferedInputStream(httpConnection.getInputStream());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            int res = bis.read();
                            // read response from the server
                            while (res != -1) {
                                baos.write((byte) res);
                                res = bis.read();
                            }

                            String configJson = baos.toString();
                            RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: configJson: %s", configJson));
                            // save config for future use
                            preferenceManger.updateLastUpdatedTime();
                            preferenceManger.saveConfigJson(configJson);

                            // update server config as well
                            serverConfig = new Gson().fromJson(configJson, RudderServerConfig.class);

                            // reset retry count
                            isDone[0] = true;

                            RudderLogger.logInfo("RudderServerConfigManager: downloadConfig: server config download successful");
                        } else {
                            BufferedInputStream bis = new BufferedInputStream(httpConnection.getErrorStream());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            int res = bis.read();
                            // read response from the server
                            while (res != -1) {
                                baos.write((byte) res);
                                res = bis.read();
                            }
                            RudderLogger.logError("ServerError for FetchingConfig: " + baos.toString());
//                            if (httpConnection.getResponseCode() == 400) {
//                                receivedError = Utils.NetworkResponses.WRITE_KEY_ERROR;
//                                return;
//                            }

                            // TODO : change the logic based on a defined API response or responseCode
                            if (baos.toString().equals("{\"message\":\"Invalid write key\"}")) {
                                receivedError = Utils.NetworkResponses.WRITE_KEY_ERROR;
                                return;
                            }
                            RudderLogger.logInfo("Retrying to download in " + retryTimeOut + "s");

                            retryCount += 1;
                            receivedError = Utils.NetworkResponses.ERROR;
                            Thread.sleep(retryCount * retryTimeOut * 1000);
                        }
                    } catch (Exception ex) {
                        RudderLogger.logError(ex);
                        RudderLogger.logInfo("Retrying to download in " + retryTimeOut + "s");
                        retryCount += 1;
                        try {
                            Thread.sleep(retryCount * retryTimeOut * 1000);
                        } catch (InterruptedException e) {
                            RudderLogger.logError(e);
                        }
                    }
                }
            }
        });
        RudderLogger.logVerbose("Download Thread Id:" + thread.getId());
        thread.start();
        try {
            // wait for download to finish
            thread.join();
        } catch (InterruptedException ex) {
            RudderLogger.logError(ex);
        }
        return isDone[0];
    }

    RudderServerConfig getConfig() {
        if (serverConfig == null) serverConfig = retrieveConfig();
        return serverConfig;
    }

    Utils.NetworkResponses getError() {
        return receivedError;
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
