package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Locale;
import java.util.Map;

class RudderPreferenceManager {
    // keys
    private static final String RUDDER_PREFS = "rl_prefs";
    private static final String RUDDER_SERVER_CONFIG_KEY = "rl_server_config";
    private static final String RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY = "rl_server_last_updated";
    private static final String RUDDER_TRAITS_KEY = "rl_traits";
    private static final String RUDDER_APPLICATION_INFO_KEY = "rl_application_info_key";
    private static final String RUDDER_APPLICATION_BUILD_KEY = "rl_application_build_key";
    private static final String RUDDER_APPLICATION_VERSION_KEY = "rl_application_version_key";
    private static final String RUDDER_EXTERNAL_ID_KEY = "rl_external_id";
    private static final String RUDDER_OPT_STATUS_KEY = "rl_opt_status";
    private static final String RUDDER_OPT_IN_TIME_KEY = "rl_opt_in_time";
    private static final String RUDDER_OPT_OUT_TIME_KEY = "rl_opt_out_time";
    private static final String RUDDER_ANONYMOUS_ID_KEY = "rl_anonymous_id_key";
    private static final String RUDDER_PERIODIC_WORK_REQUEST_ID_KEY = "rl_periodic_work_request_key";
    private static final String RUDDER_LAST_ACTIVE_TIMESTAMP_KEY = "rl_last_event_timestamp_key";
    private static final String RUDDER_SESSION_ID_KEY = "rl_session_id_key";
    private static final String RUDDER_AUTO_SESSION_TRACKING_STATUS_KEY = "rl_auto_session_tracking_status_key";
    private static final String RUDDER_DMT_HEADER_KEY = "rl_dmt_header_key";

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

    void deleteBuildVersionCode() {
        preferences.edit().remove(RUDDER_APPLICATION_INFO_KEY).apply();
    }

    int getBuildNumber() {
        return preferences.getInt(RUDDER_APPLICATION_BUILD_KEY, -1);
    }

    void saveBuildNumber(int versionCode) {
        preferences.edit().putInt(RUDDER_APPLICATION_BUILD_KEY, versionCode).apply();
    }

    String getVersionName() {
        return preferences.getString(RUDDER_APPLICATION_VERSION_KEY, null);
    }

    void saveVersionName(String versionName) {
        preferences.edit().putString(RUDDER_APPLICATION_VERSION_KEY, versionName).apply();
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
        preferences.edit().putString(RUDDER_ANONYMOUS_ID_KEY, anonymousId).apply();
    }

    String getAnonymousId() {
        return preferences.getString(RUDDER_ANONYMOUS_ID_KEY, null);
    }

    void clearAnonymousId() {
        preferences.edit().remove(RUDDER_ANONYMOUS_ID_KEY).apply();
    }

    void clearCurrentAnonymousIdValue() {
        clearAnonymousId();
        String traits = getTraits();
        if (traits != null) {
            Map<String, Object> traitsMap = Utils.convertToMap(traits);
            traitsMap.remove("anonymousId");
            saveTraits(new Gson().toJson(traitsMap));
        }
    }

    String getCurrentAnonymousIdValue() {
        String anonymousId = getAnonymousId();
        String traits = getTraits();
        if (anonymousId == null && traits != null) {
            Map<String, Object> traitsMap = Utils.convertToMap(traits);
            anonymousId = (String) traitsMap.get("anonymousId");
        }
        return anonymousId;
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

    void saveLastActiveTimestamp(Long time) {
        preferences.edit().putLong(RUDDER_LAST_ACTIVE_TIMESTAMP_KEY, time).apply();
    }

    @Nullable
    Long getLastActiveTimestamp() {
        long time = preferences.getLong(RUDDER_LAST_ACTIVE_TIMESTAMP_KEY, -1);
        return (time == -1) ? null : new Long(time);
    }

    void clearLastActiveTimestamp() {
        preferences.edit().remove(RUDDER_LAST_ACTIVE_TIMESTAMP_KEY).apply();
    }

    void saveSessionId(Long sessionId) {
        preferences.edit().putLong(RUDDER_SESSION_ID_KEY, sessionId).apply();
    }

    void clearSessionId() {
        preferences.edit().remove(RUDDER_SESSION_ID_KEY).apply();
    }

    @Nullable
    Long getSessionId() {
        long sessionId = preferences.getLong(RUDDER_SESSION_ID_KEY, -1);
        if (sessionId == -1) return null;
        else return new Long(sessionId);
    }

    boolean getAutoSessionTrackingStatus() {
        return preferences.getBoolean(RUDDER_AUTO_SESSION_TRACKING_STATUS_KEY, true);
    }

    void saveAutoSessionTrackingStatus(boolean status) {
        preferences.edit().putBoolean(RUDDER_AUTO_SESSION_TRACKING_STATUS_KEY, status).apply();
    }

    void performMigration() {
        int versionCode = getBuildVersionCode();
        if (versionCode != -1) {
            RudderLogger.logDebug(String.format(Locale.US, "RudderPreferenceManager: performMigration: build number stored in %s key, migrating it to %s", RUDDER_APPLICATION_INFO_KEY, RUDDER_APPLICATION_BUILD_KEY));
            deleteBuildVersionCode();
            saveBuildNumber(versionCode);
        }
    }

    void saveAuthToken(@Nullable String dmtHeader) {
        preferences.edit().putString(RUDDER_DMT_HEADER_KEY, dmtHeader).apply();
    }

    @Nullable
    String getAuthToken() {
        return preferences.getString(RUDDER_DMT_HEADER_KEY, null);
    }
}
