package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

class RudderPreferenceManager {
    // keys
    private static final String RUDDER_PREFS = "rl_prefs";
    private static final String RUDDER_SERVER_CONFIG_KEY = "rl_server_config";
    private static final String RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY = "rl_server_last_updated";
    private static final String RUDDER_TRAITS_KEY = "rl_traits";
    private static final String RUDDER_APPLICATION_INFO_KEY = "rl_application_info_key";
    private static final String RUDDER_EXTERNAL_ID_KEY = "rl_external_id";
    private static final String RUDDER_OPT_STATUS_KEY = "rl_opt_status";
    private static final String RUDDER_OPT_IN_TIME_KEY = "rl_opt_in_time";
    private static final String RUDDER_OPT_OUT_TIME_KEY = "rl_opt_out_time";
    private static final String RUDDER_ANONYMOUS_ID_KEY = "rl_anonymous_id_key";

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

    void updateLastUpdatedTime() {
        preferences.edit().putLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY, System.currentTimeMillis()).apply();
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

    String getExternalIds() {
        return preferences.getString(RUDDER_EXTERNAL_ID_KEY, null);
    }

    void saveExternalIds(String externalIdsJson) {
        preferences.edit().putString(RUDDER_EXTERNAL_ID_KEY, externalIdsJson).apply();
    }

    void clearExternalIds() {
        preferences.edit().remove(RUDDER_EXTERNAL_ID_KEY).apply();
    }

    void saveAnonymousId(String anonymousId) {
        System.out.println("Saved anonymousId value : " + anonymousId + " to preferences");
        preferences.edit().putString(RUDDER_ANONYMOUS_ID_KEY, anonymousId).apply();
    }

    String getAnonymousId() {
        System.out.println("Getting anonymousId value from preferences : " + preferences.getString(RUDDER_ANONYMOUS_ID_KEY, null));
        return preferences.getString(RUDDER_ANONYMOUS_ID_KEY, null);
    }

    void saveOptStatus(boolean optStatus) {
        preferences.edit().putBoolean(RUDDER_OPT_STATUS_KEY, optStatus).apply();
    }

    boolean getOptStatus() {
        return preferences.getBoolean(RUDDER_OPT_STATUS_KEY, false);
    }

    void updateOptInTime() {
        preferences.edit().putLong(RUDDER_OPT_IN_TIME_KEY, System.currentTimeMillis()).apply();
    }

    void updateOptOutTime() {
        preferences.edit().putLong(RUDDER_OPT_OUT_TIME_KEY, System.currentTimeMillis()).apply();
    }

    long getOptInTime() {
        return preferences.getLong(RUDDER_OPT_IN_TIME_KEY, -1);
    }

    long getOptOutTime() {
        return preferences.getLong(RUDDER_OPT_OUT_TIME_KEY, -1);
    }
}
