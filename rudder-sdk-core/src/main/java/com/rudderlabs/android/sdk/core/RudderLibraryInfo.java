package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.BuildConfig;

class RudderLibraryInfo {
    @SerializedName("rl_name")
    private String name = BuildConfig.APPLICATION_ID;
    @SerializedName("rl_version")
    private String version = BuildConfig.VERSION_NAME;
}
