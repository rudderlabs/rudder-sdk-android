package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

class RudderServerConfig implements Serializable {
    @SerializedName("source")
    RudderServerConfigSource source;
}
