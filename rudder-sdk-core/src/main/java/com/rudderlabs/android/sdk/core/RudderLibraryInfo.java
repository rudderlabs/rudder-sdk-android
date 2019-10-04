package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.BuildConfig;

class RudderLibraryInfo {
    @SerializedName("name")
    private String name = BuildConfig.APPLICATION_ID;
    @SerializedName("version")
    private String version = BuildConfig.VERSION_NAME;
}
