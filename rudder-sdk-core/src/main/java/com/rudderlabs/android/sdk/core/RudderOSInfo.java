package com.rudderlabs.android.sdk.core;

import android.os.Build;
import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.BuildConfig;

class RudderOSInfo {
    @SerializedName("name")
    private String name = "Android";
    @SerializedName("version")
    private String version = Build.VERSION.RELEASE;
}
