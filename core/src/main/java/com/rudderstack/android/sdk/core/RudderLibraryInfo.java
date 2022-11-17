package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class RudderLibraryInfo {
    RudderLibraryInfo(String version){
        this.version = version;
    }
    @SerializedName("name")
    private String name = BuildConfig.LIBRARY_PACKAGE_NAME;
    @SerializedName("version")
    private String version;
}
