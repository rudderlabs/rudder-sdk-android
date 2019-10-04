package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.telephony.TelephonyManager;

import com.google.gson.annotations.SerializedName;

import static android.content.Context.TELEPHONY_SERVICE;

class RudderNetworkInfo {
    @SerializedName("carrier")
    private String carrier;
    @SerializedName("wifi")
    private boolean isWifiEnabled = false;
    @SerializedName("bluetooth")
    private boolean isBluetoothEnabled = false;
    @SerializedName("cellular")
    private boolean isCellularEnabled = false;

    RudderNetworkInfo(Application application) {
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        this.carrier = telephonyManager != null ? telephonyManager.getNetworkOperatorName() : "NA";
    }
}
