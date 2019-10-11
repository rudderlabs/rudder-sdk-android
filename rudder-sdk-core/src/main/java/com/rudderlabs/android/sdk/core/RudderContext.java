package com.rudderlabs.android.sdk.core;

import android.app.Application;

import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.util.Utils;

import java.util.Locale;

class RudderContext {
    @SerializedName("app")
    private RudderApp app;
    @SerializedName("traits")
    private RudderTraits traits;
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
        this.traits = new RudderTraits(deviceId);
        this.screenInfo = new RudderScreenInfo(application);
        this.userAgent = System.getProperty("http.agent");
        this.deviceInfo = new RudderDeviceInfo(deviceId);
        this.networkInfo = new RudderNetwork(application);
        this.osInfo = new RudderOSInfo();
        this.libraryInfo = new RudderLibraryInfo();
    }

    void updateTraits(RudderTraits traits) {
        this.traits = traits;
    }

    public RudderTraits getTraits() {
        return traits;
    }

    public String getDeviceId() {
        return deviceInfo.getDeviceId();
    }
}
