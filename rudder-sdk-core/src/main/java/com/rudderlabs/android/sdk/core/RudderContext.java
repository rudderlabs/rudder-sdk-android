package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.util.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class RudderContext {
    @SerializedName("app")
    private RudderApp app;
    @SerializedName("traits")
    private Map<String, Object> traits;
    @SerializedName("library")
    private RudderLibraryInfo libraryInfo;
    @SerializedName("os")
    private RudderOSInfo osInfo;
    @SerializedName("screen")
    private RudderScreenInfo screenInfo;
    @SerializedName("userAgent")
    private String userAgent;
    @SerializedName("locale")
    private String locale = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
    @SerializedName("device")
    private RudderDeviceInfo deviceInfo;
    @SerializedName("network")
    private RudderNetwork networkInfo;
    @SerializedName("timezone")
    private String timezone = Utils.getTimeZone();

    private RudderContext() {
        // stop instantiating without application instance.
        // cachedContext is used every time, once initialized
    }

    RudderContext(Application application) {
        String deviceId = Utils.getDeviceId(application);

        this.app = new RudderApp(application);

        // get saved traits from prefs. if not present create new one and save
        SharedPreferences preferences = application.getSharedPreferences(Utils.RUDDER_PREFS, Context.MODE_PRIVATE);
        String traitsJson = preferences.getString(Utils.RUDDER_TRAITS_KEY, null);
        RudderLogger.logDebug(String.format(Locale.US, "Traits from persistence storage%s", traitsJson));
        if (traitsJson == null) {
            RudderTraits traits = new RudderTraits(deviceId);
            traitsJson = new Gson().toJson(traits);
            this.traits = Utils.convertToMap(traitsJson);
            preferences.edit().putString(Utils.RUDDER_TRAITS_KEY, traitsJson).apply();
            RudderLogger.logDebug("New traits has been saved");
        } else {
            this.traits = Utils.convertToMap(traitsJson);
            RudderLogger.logDebug("Using old traits from persistence");
        }

        this.screenInfo = new RudderScreenInfo(application);
        this.userAgent = System.getProperty("http.agent");
        this.deviceInfo = new RudderDeviceInfo(deviceId);
        this.networkInfo = new RudderNetwork(application);
        this.osInfo = new RudderOSInfo();
        this.libraryInfo = new RudderLibraryInfo();
    }

    void updateTraits(RudderTraits traits) {
        // if traits is null reset the traits to a new one with only anonymousId
        if (traits == null) {
            this.traits = Utils.convertToMap(new Gson().toJson(new RudderTraits(this.getDeviceId())));
        }

        // convert the whole traits to map and take care of the extras
        Map<String, Object> traitsMap = Utils.convertToMap(new Gson().toJson(traits));
        if (traits != null && traits.getExtras() != null) traitsMap.putAll(traits.getExtras());

        // update traits object here
        this.traits = traitsMap;
    }

    void persistTraits() {
        // persist updated traits to sharedPreference
        try {
            if (RudderClient.getInstance() != null && RudderClient.getInstance().getApplication() != null) {
                SharedPreferences preferences = RudderClient.getInstance()
                        .getApplication()
                        .getSharedPreferences(Utils.RUDDER_PREFS, Context.MODE_PRIVATE);
                preferences.edit().putString(Utils.RUDDER_TRAITS_KEY, new Gson().toJson(this.traits)).apply();
            }
        } catch (NullPointerException ex) {
            RudderLogger.logError(ex);
        }
    }

    Map<String, Object> getTraits() {
        return traits;
    }

    void updateTraitsMap(Map<String, Object> traits) {
        this.traits = traits;
    }

    String getDeviceId() {
        return deviceInfo.getDeviceId();
    }
}
