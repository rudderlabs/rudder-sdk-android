package com.rudderstack.android.sdk.core;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Pair;

import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.util.Utils;

import static com.rudderstack.android.sdk.core.util.Utils.isOnClassPath;

import androidx.annotation.Nullable;

class RudderDeviceInfo {
    @SerializedName("id")
    @Nullable
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

    RudderDeviceInfo(String advertisingId, String token, boolean collectDeviceId, RudderPreferenceManager preferenceManager) {

        if (collectDeviceId) {
            this.deviceId = Utils.getDeviceId(RudderClient.getApplication());
        }

        // update the advertisingId value in persistence, if user specifies one again
        // if the user didn't pass any advertisingId, then try reading it from preferences
        if (advertisingId != null && !advertisingId.isEmpty()) {
            preferenceManager.saveAdvertisingId(advertisingId);
            this.advertisingId = advertisingId;
        } else {
            this.advertisingId = preferenceManager.getAdvertisingId();
        }
        this.adTrackingEnabled = (this.advertisingId != null);

        if (token != null && !token.isEmpty()) {
            this.token = token;
        }
    }

    @Nullable
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
        this.adTrackingEnabled = (this.advertisingId != null);
        if (RudderClient.getApplication() != null) {
            RudderPreferenceManager preferenceManager = RudderPreferenceManager.getInstance(RudderClient.getApplication());
            preferenceManager.saveAdvertisingId(advertisingId);
        }
    }

    void setAutoCollectedAdvertisingId(String advertisingId) {
        this.advertisingId = advertisingId;
    }

    String getAdvertisingId() {
        return this.advertisingId;
    }

    boolean isAdTrackingEnabled() {
        return this.adTrackingEnabled;
    }

    void clearAdvertisingId() {
        this.advertisingId = null;
        this.adTrackingEnabled = (this.advertisingId != null);
        if (RudderClient.getApplication() != null) {
            RudderPreferenceManager preferenceManager = RudderPreferenceManager.getInstance(RudderClient.getApplication());
            preferenceManager.clearAdvertisingId();
        }
    }
}
