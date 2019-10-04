package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.telephony.TelephonyManager;
import com.google.gson.annotations.SerializedName;

import static android.content.Context.TELEPHONY_SERVICE;

class RudderNetworkInfo {
    @SerializedName("rl_carrier")
    private String carrier;

    RudderNetworkInfo(Application application) {
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        this.carrier = telephonyManager != null ? telephonyManager.getNetworkOperatorName() : "NA";
    }
}
