package com.rudderstack.android.sdk.core;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class RudderContext {
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
    @SerializedName("anonymousId")
    private String anonymousId;

    private RudderContext() {
        // stop instantiating without application instance.
        // cachedContext is used every time, once initialized
    }

    RudderContext(Application application) {
        String deviceId = Utils.getDeviceId(application);


        this.app = new RudderApp(application);

        // get saved traits from prefs. if not present create new one and save

        RudderPreferenceManager preferenceManger = RudderPreferenceManager.getInstance(application);
        preferenceManger.saveAnonymousId(preferenceManger.getAnonymousId());
        this.anonymousId = preferenceManger.getAnonymousId();
        String traitsJson = preferenceManger.getTraits();
        RudderLogger.logDebug(String.format(Locale.US, "Traits from persistence storage%s", traitsJson));
        RudderLogger.logDebug(("This is anonymous ID" + preferenceManger.getAnonymousId()));
        if (traitsJson == null) {
            RudderTraits traits = new RudderTraits(anonymousId);
            this.traits = Utils.convertToMap(new Gson().toJson(traits));
            this.persistTraits();
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
            RudderPreferenceManager preferenceManger = RudderPreferenceManager.getInstance(RudderClient.getInstance().getApplication());
            this.anonymousId = preferenceManger.getAnonymousId();
            traits = new RudderTraits(this.anonymousId);
        }

        // convert the whole traits to map and take care of the extras
        Map<String, Object> traitsMap = Utils.convertToMap(new Gson().toJson(traits));
        if (traits.getExtras() != null) traitsMap.putAll(traits.getExtras());

        // update traits object here
        this.traits = traitsMap;
    }

    void persistTraits() {
        // persist updated traits to sharedPreference
        try {
            if (RudderClient.getInstance() != null && RudderClient.getInstance().getApplication() != null) {
                RudderPreferenceManager preferenceManger = RudderPreferenceManager.getInstance(RudderClient.getInstance().getApplication());
                preferenceManger.saveTraits(new Gson().toJson(this.traits));
                preferenceManger.saveAnonymousId(preferenceManger.getAnonymousId());
            }
        } catch (NullPointerException ex) {
            RudderLogger.logError(ex);
        }
    }

    public Map<String, Object> getTraits() {
        return traits;
    }

    void updateTraitsMap(Map<String, Object> traits) {
        this.traits = traits;
    }

    String getDeviceId() {
        return deviceInfo.getDeviceId();
    }

    String getAnonymousId() {
        return this.anonymousId;

    }

    public void putDeviceToken(String token) {
        this.deviceInfo.setToken(token);
    }
}
