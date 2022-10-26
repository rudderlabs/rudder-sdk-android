package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
    @SerializedName("dataPlaneUrls")
    Map<String, String> dataResidencyUrls;
}
