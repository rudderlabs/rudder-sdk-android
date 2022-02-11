package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class RudderServerConfigManager {
    private static RudderPreferenceManager preferenceManger;
    private static RudderServerConfigManager instance;
    private static RudderServerConfig serverConfig;
    private static RudderConfig rudderConfig;
    private static ReentrantLock lock = new ReentrantLock();
    private Map<String, Object> integrationsMap = null;
    private Utils.NetworkResponses receivedError = Utils.NetworkResponses.SUCCESS;
    private static final String RUDDER_SERVER_CONFIG_FILE_NAME = "RudderServerConfig";

    private static Context context;


    RudderServerConfigManager(Application _application, String _writeKey, RudderConfig _config) {
        preferenceManger = RudderPreferenceManager.getInstance(_application);
        rudderConfig = _config;
        context = _application.getApplicationContext();
        // fetch server config
        fetchConfig(_writeKey);
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


    private void fetchConfig(final String _writeKey) {
        // don't try to download anything if writeKey is not valid
        if (TextUtils.isEmpty(_writeKey)) {
            receivedError = Utils.NetworkResponses.WRITE_KEY_ERROR;
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // download and store config to storage
                downloadConfig(_writeKey);
                // retrieve config from storage

                lock.lock();
                serverConfig = getRudderServerConfig();
                if (serverConfig == null) {
                    RudderLogger.logDebug("Server config retrieval failed.No config found in storage");
                    RudderLogger.logError(String.format("Failed to fetch server config for writeKey: %s", _writeKey));
                }
                lock.unlock();
            }
        });
        RudderLogger.logVerbose("Download Thread Id:" + thread.getId());
        thread.start();
    }

    private void downloadConfig(final String _writeKey) {
        RudderLogger.logDebug(String.format("Downloading server config for writeKey: %s", _writeKey));
        boolean isDone = false;
        int retryCount = 0;
        while (!isDone && retryCount <= 3) {
            try {
                String configUrl = rudderConfig.getControlPlaneUrl() + "sourceConfig?p=android&v="+Constants.RUDDER_LIBRARY_VERSION+"&bv="+android.os.Build.VERSION.SDK_INT;
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
                    try {
                        RudderServerConfig rudderServerConfig = new Gson().fromJson(configJson, RudderServerConfig.class);
                        RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: configJson: %s", configJson));
                        // save config for future use
                        preferenceManger.updateLastUpdatedTime();
                        saveRudderServerConfig(rudderServerConfig);
                        // reset retry count
                        isDone = true;
                        RudderLogger.logInfo("RudderServerConfigManager: downloadConfig: server config download successful");
                    } catch (Exception e) {
                        isDone = false;
                        retryCount += 1;
                        RudderLogger.logError("RudderServerConfigManager: downloadConfig: Failed to parse RudderServerConfig Object, retrying in " + retryCount + "s");
                        e.printStackTrace();
                        try {
                            Thread.sleep(retryCount * 1000);
                        } catch (InterruptedException ex) {
                            RudderLogger.logError(ex);
                        }
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

                    retryCount += 1;
                    RudderLogger.logInfo("downloadConfig: Retrying to download in " + retryCount + "s");
                    receivedError = Utils.NetworkResponses.ERROR;
                    Thread.sleep(retryCount * 1000);
                }
            } catch (Exception ex) {
                RudderLogger.logError(ex);
                retryCount += 1;
                RudderLogger.logInfo("downloadConfig: Retrying to download in " + retryCount + "s");
                try {
                    Thread.sleep(retryCount * 1000);
                } catch (InterruptedException e) {
                    RudderLogger.logError(e);
                }
            }
        }
        if (!isDone) {
            RudderLogger.logDebug("Server config download failed.Using the last saved config from storage");
        }
    }

    static void saveRudderFlushConfig(RudderFlushConfig rudderFlushConfig)
    {
        try {
            FileOutputStream fos = context.openFileOutput("RudderFlushConfig", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(rudderFlushConfig);
            os.close();
            fos.close();
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: saveRudderFlushConfig: Exception while saving RudderServerConfig Object to File");
            e.printStackTrace();
        }
    }

    static RudderFlushConfig getRudderFlushConfig(Context context) {
        RudderFlushConfig rudderFlushConfig = null;
        try {
            if (fileExists(context, "RudderFlushConfig")) {
                FileInputStream fis = context.openFileInput("RudderFlushConfig");
                ObjectInputStream is = new ObjectInputStream(fis);
                rudderFlushConfig = (RudderFlushConfig) is.readObject();
                is.close();
                fis.close();
            }
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: getRudderFlushConfig: Failed to read RudderServerConfig Object from File");
            e.printStackTrace();
        } finally {
            return rudderFlushConfig;
        }
    }

    void saveRudderServerConfig(RudderServerConfig rudderServerConfig) {
        try {
            FileOutputStream fos = context.openFileOutput(RUDDER_SERVER_CONFIG_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(rudderServerConfig);
            os.close();
            fos.close();
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: saveRudderServerConfig: Exception while saving RudderServerConfig Object to File");
            e.printStackTrace();
        }
    }

    static RudderServerConfig getRudderServerConfig() {
        RudderServerConfig rudderServerConfig = null;
        try {
            if (fileExists(context, RUDDER_SERVER_CONFIG_FILE_NAME)) {
                FileInputStream fis = context.openFileInput(RUDDER_SERVER_CONFIG_FILE_NAME);
                ObjectInputStream is = new ObjectInputStream(fis);
                rudderServerConfig = (RudderServerConfig) is.readObject();
                is.close();
                fis.close();
            }
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: getRudderServerConfig: Failed to read RudderServerConfig Object from File");
            e.printStackTrace();
        } finally {
            return rudderServerConfig;
        }
    }

    RudderServerConfig getConfig() {
        RudderServerConfig config = null;
        lock.lock();
        config = serverConfig;
        lock.unlock();
        return config;
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

    static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }
}
