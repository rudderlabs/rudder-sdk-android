package com.rudderstack.android.sdk.core;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Pair;

import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.util.Utils;

import static com.rudderstack.android.sdk.core.util.Utils.isOnClassPath;

class RudderDeviceInfo {
    @SerializedName("id")
    private String deviceId;
    @SerializedName("manufacturer")
    private String manufacturer = Build.MANUFACTURER;
    @SerializedName("model")
    private String model = Build.MODEL;
    @SerializedName("name")
    private String name = Build.DEVICE;
    @SerializedName("type")
    private String type = "Android";
    @SerializedName("token")
    private String token;
    @SerializedName("adTrackingEnabled")
    private Boolean adTrackingEnabled;
    @SerializedName("advertisingId")
    private String advertisingId;

    RudderDeviceInfo(String advertisingId) {
        this.deviceId = Utils.getDeviceId(RudderClient.getApplication());
        if (advertisingId != null && !advertisingId.isEmpty()) {
            this.advertisingId = advertisingId;
            this.adTrackingEnabled = true;
        }
    }

    String getDeviceId() {
        return deviceId;
    }

    void setToken(String token) {
        this.token = token;
    }

    void setAdTrackingEnabled(boolean enabled) {
        this.adTrackingEnabled = enabled;
    }

    void setAdvertisingId(String advertisingId) {
        this.advertisingId = advertisingId;
    }

    String getAdvertisingId() {
        return this.advertisingId;
    }

    boolean isAdTrackingEnabled() {
        return this.adTrackingEnabled;
    }
}
