package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

class RudderServerConfigManager {
    private static RudderServerConfigManager instance;
    private static SharedPreferences preferences;
    private static RudderServerConfig serverConfig;
    private static RudderConfig rudderConfig;
    private Map<String, Object> integrationsMap = null;

    static RudderServerConfigManager getInstance(Application _application, String _writeKey, RudderConfig config) {
        if (instance == null) {
            RudderLogger.logDebug("Creating RudderServerConfigManager instance");
            instance = new RudderServerConfigManager(_application, _writeKey, config);
        }
        return instance;
    }

    private RudderServerConfigManager(Application _application, String _writeKey, RudderConfig _config) {
        preferences = _application.getSharedPreferences("rl_prefs", Context.MODE_PRIVATE);
        serverConfig = retrieveConfig();
        rudderConfig = _config;
        boolean isConfigOutdated = isServerConfigOutDated();
        if (serverConfig == null) {
            RudderLogger.logDebug("Server config is not present in preference storage. downloading config");
            downloadConfig(_writeKey);
        } else {
            if (isConfigOutdated) {
                RudderLogger.logDebug("Server config is outdated. downloading config again");
                downloadConfig(_writeKey);
            } else {
                RudderLogger.logDebug("Server config found. Using existing config");
            }
        }
    }

    // update config if it is older than an day
    private boolean isServerConfigOutDated() {
        long lastUpdatedTime = preferences.getLong("rl_server_update_time", -1);
        RudderLogger.logDebug(String.format(Locale.US, "Last updated config time: %d", lastUpdatedTime));
        RudderLogger.logDebug(String.format(Locale.US, "ServerConfigInterval: %d", rudderConfig.getConfigRefreshInterval()));
        if (lastUpdatedTime == -1) return true;

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUpdatedTime) > (rudderConfig.getConfigRefreshInterval() * 60 * 60 * 1000);
    }

    private RudderServerConfig retrieveConfig() {
        String configJson = preferences.getString("rl_server_config", null);
        RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: retrieveConfig: configJson: %s", configJson));
        if (configJson == null) return null;
        return new Gson().fromJson(configJson, RudderServerConfig.class);
    }

    private void downloadConfig(final String _writeKey) {
        // don't try to download anything if writeKey is not valid
        if (TextUtils.isEmpty(_writeKey)) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDone = false;
                int retryCount = 0, retryTimeOut = 10;
                while (!isDone && retryCount <= 3) {
                    try {
                        String configUrl = Constants.CONFIG_PLANE_URL + "/sourceConfig";
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
                            preferences.edit()
                                    .putLong("rl_server_update_time", System.currentTimeMillis())
                                    .putString("rl_server_config", configJson)
                                    .apply();

                            // update server config as well
                            serverConfig = new Gson().fromJson(configJson, RudderServerConfig.class);

                            // reset retry count
                            isDone = true;

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
                            RudderLogger.logInfo("Retrying to download in " + retryTimeOut + "s");

                            retryCount += 1;
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
        }).start();
    }

    RudderServerConfig getConfig() {
        if (serverConfig == null) serverConfig = retrieveConfig();
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
