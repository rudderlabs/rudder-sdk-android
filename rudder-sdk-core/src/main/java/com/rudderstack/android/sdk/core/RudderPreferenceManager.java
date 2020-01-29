package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

class RudderPreferenceManager {
    // keys
    private static final String RUDDER_PREFS = "rl_prefs";
    private static final String RUDDER_SERVER_CONFIG_KEY = "rl_server_config";
    private static final String RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY = "rl_server_last_updated";
    private static final String RUDDER_TRAITS_KEY = "rl_traits";
    private static final String RUDDER_APPLICATION_INFO_KEY = "rl_application_info_key";
    private static final String RUDDER_STATS_FINGER_PRINT = "rl_stats_finger_print";
    private static final String RUDDER_STATS_CONFIG_JSON = "rl_stats_config_json";
    private static final String RUDDER_STATS_BEGIN_TIME = "rl_stats_begin_time";

    private static SharedPreferences preferences;
    private static RudderPreferenceManager instance;

    private RudderPreferenceManager(Application application) {
        preferences = application.getSharedPreferences(RUDDER_PREFS, Context.MODE_PRIVATE);
    }

    static RudderPreferenceManager getInstance(Application application) {
        if (instance == null) instance = new RudderPreferenceManager(application);

        return instance;
    }

    long getLastUpdatedTime() {
        return preferences.getLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY, -1);
    }

    String getConfigJson() {
        return preferences.getString(RUDDER_SERVER_CONFIG_KEY, null);
    }

    void updateLastUpdatedTime() {
        preferences.edit().putLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY, System.currentTimeMillis()).apply();
    }

    void saveConfigJson(String configJson) {
        preferences.edit().putString(RUDDER_SERVER_CONFIG_KEY, configJson).apply();
    }

    String getTraits() {
        return preferences.getString(RUDDER_TRAITS_KEY, null);
    }

    void saveTraits(String traitsJson) {
        preferences.edit().putString(RUDDER_TRAITS_KEY, traitsJson).apply();
    }

    int getBuildVersionCode() {
        return preferences.getInt(RUDDER_APPLICATION_INFO_KEY, -1);
    }

    void saveBuildVersionCode(int versionCode) {
        preferences.edit().putInt(RUDDER_APPLICATION_INFO_KEY, versionCode).apply();
    }

    String getRudderStatsFingerPrint() {
        String fingerPrint = preferences.getString(RUDDER_STATS_FINGER_PRINT, null);
        if (fingerPrint == null) {
            // if fingerprint is not present, create and persist
            fingerPrint = UUID.randomUUID().toString();
            preferences.edit().putString(RUDDER_STATS_FINGER_PRINT, fingerPrint).apply();
        }
        return fingerPrint;
    }

    void persistStatsConfigJson(String statsConfigJson) {
        preferences.edit().putString(RUDDER_STATS_CONFIG_JSON, statsConfigJson).apply();
    }

    String getStatsConfigJson() {
        return preferences.getString(RUDDER_STATS_CONFIG_JSON, null);
    }

    long getStatsBeginTime() {
        long beginTime = preferences.getLong(RUDDER_STATS_BEGIN_TIME, -1);
        if (beginTime == -1) {
            beginTime = System.currentTimeMillis();
            updateRudderStatsBeginTime(beginTime);
        }
        return beginTime;
    }

    void updateRudderStatsBeginTime(long timestamp) {
        preferences.edit().putLong(RUDDER_STATS_BEGIN_TIME, timestamp).apply();
    }

}
