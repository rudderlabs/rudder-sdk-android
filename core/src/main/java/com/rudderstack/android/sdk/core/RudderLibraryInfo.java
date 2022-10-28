package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class RudderLibraryInfo {
    @SerializedName("name")
    private String name = BuildConfig.LIBRARY_PACKAGE_NAME;
    @SerializedName("version")
    private String version = Constants.RUDDER_LIBRARY_VERSION;
}
