package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

class RudderServerConfigSource implements Serializable {
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
