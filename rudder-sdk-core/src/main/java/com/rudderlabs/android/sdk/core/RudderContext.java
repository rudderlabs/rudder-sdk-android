package com.rudderlabs.android.sdk.core;

import android.app.Application;

import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.util.Utils;

import java.util.Locale;

class RudderContext {
    @SerializedName("rl_app")
    private RudderApp app;
    @SerializedName("rl_traits")
    private RudderTraits traits;
    @SerializedName("rl_library")
    private RudderLibraryInfo libraryInfo;
    @SerializedName("rl_os")
    private RudderOSInfo osInfo;
    @SerializedName("rl_screen")
    private RudderScreenInfo screenInfo;
    @SerializedName("rl_user_agent")
    private String userAgent;
    @SerializedName("rl_locale")
    private String locale = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
    @SerializedName("rl_device")
    private RudderDeviceInfo deviceInfo;
    @SerializedName("rl_network")
    private RudderNetworkInfo networkInfo;
    @SerializedName("rl_platform")
    private String platform = "Android";

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
        this.networkInfo = new RudderNetworkInfo(application);
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
