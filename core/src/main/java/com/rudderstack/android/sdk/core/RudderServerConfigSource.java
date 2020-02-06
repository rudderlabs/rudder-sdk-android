package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.util.List;

class RudderServerConfigSource {
    @SerializedName("id")
    String sourceId;
    @SerializedName("name")
    String sourceName;
    @SerializedName("enabled")
    boolean isSourceEnabled;
    @SerializedName("updatedAt")
    String updatedAt;
    @SerializedName("destinations")
    List<RudderServerDestination> destinations;
}
