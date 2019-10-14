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
import java.util.Locale;
import java.util.TimeZone;

class RudderServerConfigManager {
    private static RudderServerConfigManager instance;
    private static SharedPreferences preferences;
    private static RudderServerConfig serverConfig;

    static RudderServerConfigManager getInstance(Application _application, String _writeKey) {
        if (instance == null) instance = new RudderServerConfigManager(_application, _writeKey);
        return instance;
    }

    private RudderServerConfigManager(Application _application, String _writeKey) {
        preferences = _application.getSharedPreferences("rl_prefs", Context.MODE_PRIVATE);
        serverConfig = retrieveConfig();
        if (serverConfig == null || isServerConfigOutDated()) {
            // download and save config
            downloadConfig(_writeKey);
        }
    }

    // update config if it is older than an day
    private boolean isServerConfigOutDated() {
        long lastUpdatedTime = preferences.getLong("server_update_time", -1);
        if (lastUpdatedTime == -1) return true;

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUpdatedTime) > (24 * 60 * 60 * 1000);
    }

    private RudderServerConfig retrieveConfig() {
        String configJson = preferences.getString("server_config", null);
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
                while (!isDone || retryCount <= 3) {
                    try {
                        String configUrl = String.format("https://api.rudderlabs.com/source-config?write_key=%s", _writeKey);
                        // create url object
                        URL url = new URL(configUrl);
                        // get connection object
                        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                        // set request method
                        httpConnection.setRequestMethod("GET");
                        httpConnection.setRequestProperty("Authorization",
                                "Basic " + Base64.encodeToString(
                                        (_writeKey + ":").getBytes("UTF-8"), Base64.DEFAULT));
                        // create connection
                        httpConnection.connect();
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
                            // save config for future use
                            preferences.edit()
                                    .putLong("server_update_time", System.currentTimeMillis())
                                    .putString("server_config", configJson)
                                    .apply();

                            // update server config as well
                            serverConfig = new Gson().fromJson(configJson, RudderServerConfig.class);

                            // reset retry count
                            isDone = true;
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
}
