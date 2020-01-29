package com.rudderstack.android.sdk.core;

import android.os.Build;

import com.google.gson.annotations.SerializedName;

class RudderOSInfo {
    @SerializedName("name")
    private String name = "Android";
    @SerializedName("version")
    private String version = Build.VERSION.RELEASE;

     String getName() {
        return name;
    }

     String getVersion() {
        return version;
    }
}
