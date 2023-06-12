package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static com.rudderstack.android.sdk.core.RudderNetworkManager.NetworkResponses;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.addEndPoint;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.RequestMethod;
import static com.rudderstack.android.sdk.core.RudderNetworkManager.Result;

class RudderServerConfigManager {

    private RudderPreferenceManager preferenceManger;
    private RudderConfig rudderConfig;
    private Context context;
    private RudderNetworkManager networkManager;
    private RudderServerConfig serverConfig;

    private NetworkResponses receivedError = NetworkResponses.SUCCESS;

    private static final String RUDDER_SERVER_CONFIG_FILE_NAME = "RudderServerConfig";
    private static final ReentrantLock lock = new ReentrantLock();


    RudderServerConfigManager(Application _application, RudderConfig _config, RudderNetworkManager networkManager) {
        this.preferenceManger = RudderPreferenceManager.getInstance(_application);
        this.rudderConfig = _config;
        this.context = _application.getApplicationContext();
        this.networkManager = networkManager;
        // fetch server config
        fetchConfig();
    }

    private void fetchConfig() {

        Thread thread = new Thread(() -> {
            downloadConfig();
            lock.lock();
            serverConfig = getRudderServerConfig();
            if (serverConfig == null) {
                RudderLogger.logError("Failed to fetch server config");
            }
            lock.unlock();
        });
        RudderLogger.logVerbose("Download Thread Id:" + thread.getId());
        thread.start();
    }

    private void downloadConfig() {
        boolean isDone = false;
        int retryCount = 0;
        String endpoint = "sourceConfig?p=android&v=" + BuildConfig.VERSION_NAME + "&bv=" + android.os.Build.VERSION.SDK_INT;
        String requestUrl = addEndPoint(rudderConfig.getControlPlaneUrl(), endpoint);
        while (!isDone && retryCount <= 3) {
            RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: configUrl: %s", requestUrl));
            Result result = networkManager.sendNetworkRequest(null, requestUrl, RequestMethod.GET, false);
            if (result.status == NetworkResponses.SUCCESS) {
                try {
                    RudderServerConfig rudderServerConfig = new Gson().fromJson(result.response, RudderServerConfig.class);
                    RudderLogger.logDebug(String.format(Locale.US, "RudderServerConfigManager: downloadConfig: configJson: %s", result.response));
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
                    sleep(retryCount);
                }
            } else if (result.status == NetworkResponses.WRITE_KEY_ERROR) {
                receivedError = NetworkResponses.WRITE_KEY_ERROR;
                RudderLogger.logError("RudderServerConfigManager: downloadConfig: ServerError for FetchingConfig due to invalid write key " + result.error);
                return;
            } else {
                receivedError = NetworkResponses.ERROR;
                retryCount += 1;
                RudderLogger.logError("RudderServerConfigManager: downloadConfig: ServerError for FetchingConfig: " + result.error);
                RudderLogger.logInfo("RudderServerConfigManager: downloadConfig: Retrying to download in " + retryCount + "s");
                sleep(retryCount);
            }
        }
        if (!isDone) {
            RudderLogger.logDebug("RudderServerConfigManager: downloadConfig: Server config download failed. Using the last saved config from storage");
        }
    }

    void saveRudderServerConfig(RudderServerConfig rudderServerConfig) {
        try(FileOutputStream fos = context.openFileOutput(RUDDER_SERVER_CONFIG_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos)) {

            os.writeObject(rudderServerConfig);
        } catch (Exception e) {
            RudderLogger.logError("RudderServerConfigManager: saveRudderServerConfig: Exception while saving RudderServerConfig Object to File");
            e.printStackTrace();
        }
    }

    private RudderServerConfig getRudderServerConfig() {
        RudderServerConfig rudderServerConfig = null;
        if (!Utils.fileExists(context, RUDDER_SERVER_CONFIG_FILE_NAME)) {
            return null;
        }
            try(FileInputStream fis = context.openFileInput(RUDDER_SERVER_CONFIG_FILE_NAME);
                ObjectInputStream is = new ObjectInputStream(fis)) {

                rudderServerConfig = (RudderServerConfig) is.readObject();
            } catch (Exception e) {
                RudderLogger.logError("RudderServerConfigManager: getRudderServerConfig: Failed to read RudderServerConfig Object from File");
                e.printStackTrace();
            }
        return rudderServerConfig;

    }

    RudderServerConfig getConfig() {
        RudderServerConfig config;
        lock.lock();
        config = serverConfig;
        lock.unlock();
        return config;
    }

    NetworkResponses getError() {
        return receivedError;
    }

    private void sleep(int retryCount) {
        try {
            Thread.sleep(retryCount * 1000L);
        } catch (InterruptedException ex) {
            RudderLogger.logError(String.format(Locale.US, "RudderServerConfigManager: Sleep: Exception while the thread is in sleep %s", ex.getLocalizedMessage()));
        }
    }
}
