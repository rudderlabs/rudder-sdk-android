package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class RudderServerConfig {
    @SerializedName("source")
    RudderServerConfigSource source;
}
