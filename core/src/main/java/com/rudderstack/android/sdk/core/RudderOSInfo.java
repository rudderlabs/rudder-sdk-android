package com.rudderstack.android.sdk.core;

import android.os.Build;

import com.google.gson.annotations.SerializedName;

class RudderOSInfo {
    @SerializedName("name")
    private final String name = "Android";
    @SerializedName("version")
    private final String version = Build.VERSION.RELEASE;
}
