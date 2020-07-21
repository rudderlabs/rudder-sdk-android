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
    private static final String RUDDER_ANONYMOUS_ID = "rl_anonymous_id";
    private static final String RUDDER_USER_ID = "rl_user_id";

    private static SharedPreferences preferences;
    private static RudderPreferenceManager instance;

    // constructor
    private RudderPreferenceManager() {
        preferences = RudderClient.getInstance().getApplication()
          .getSharedPreferences(RUDDER_PREFS, Context.MODE_PRIVATE);
    }

    // access
    static RudderPreferenceManager getInstance() {
        if (instance == null) {
          instance = new RudderPreferenceManager();
        }

        return instance;
    }

    // last config update time from controlPlane
    long getLastUpdatedTime() {
        return preferences.getLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY, -1);
    }

    void updateLastUpdatedTime() {
        preferences.edit().putLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY, System.currentTimeMillis()).apply();
    }

    // config json (response of controlPlane)
    String getConfigJson() {
        return preferences.getString(RUDDER_SERVER_CONFIG_KEY, null);
    }

    void saveConfigJson(String configJson) {
        preferences.edit().putString(RUDDER_SERVER_CONFIG_KEY, configJson).apply();
    }

    // traits
    String getTraits() {
        return preferences.getString(RUDDER_TRAITS_KEY, null);
    }

    void saveTraits(String traitsJson) {
        preferences.edit().putString(RUDDER_TRAITS_KEY, traitsJson).apply();
    }

    // app version
    int getBuildVersionCode() {
        return preferences.getInt(RUDDER_APPLICATION_INFO_KEY, -1);
    }

    void saveBuildVersionCode(int versionCode) {
        preferences.edit().putInt(RUDDER_APPLICATION_INFO_KEY, versionCode).apply();
    }

    // anonymousId
    String getAnonymousId() {
        String anonymousId = preferences.getString(RUDDER_ANONYMOUS_ID, null);
        if (anonymousId == null) {
            preferences.edit().putString(RUDDER_ANONYMOUS_ID, UUID.randomUUID().toString()).apply();
            anonymousId =  preferences.getString(RUDDER_ANONYMOUS_ID, null);
        }
        return anonymousId;
    }

    void resetAnonymousId() {
        preferences.edit().putString(RUDDER_ANONYMOUS_ID, null).apply();
    }

    // userId
    String getUserId() {
        return preferences.getString(RUDDER_USER_ID, null);
    }

    void setUserId(String userId) {
        preferences.edit().putString(RUDDER_USER_ID, userId).apply();
    }
}
