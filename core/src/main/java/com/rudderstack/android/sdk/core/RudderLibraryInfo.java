package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class RudderLibraryInfo {
    @SerializedName("name")
    private final String name = BuildConfig.LIBRARY_PACKAGE_NAME;
    @SerializedName("version")
    private final String version = BuildConfig.VERSION_NAME;
}
