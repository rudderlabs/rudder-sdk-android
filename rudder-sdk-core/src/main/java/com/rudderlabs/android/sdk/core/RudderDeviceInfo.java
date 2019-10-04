package com.rudderlabs.android.sdk.core;

import android.os.Build;
import com.google.gson.annotations.SerializedName;

class RudderDeviceInfo {
    @SerializedName("rl_id")
    private String deviceId;
    @SerializedName("rl_manufacturer")
    private String manufacturer = Build.MANUFACTURER;
    @SerializedName("rl_model")
    private String model = Build.MODEL;
    @SerializedName("rl_name")
    private String name = Build.DEVICE;

    RudderDeviceInfo(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
