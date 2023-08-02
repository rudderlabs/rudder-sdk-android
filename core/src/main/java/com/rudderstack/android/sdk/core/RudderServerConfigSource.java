package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RudderServerConfigSource implements Serializable {
    @SerializedName("id")
    String sourceId;
    @SerializedName("name")
    String sourceName;
    @SerializedName("config")
    SourceConfiguration sourceConfiguration;
    @SerializedName("enabled")
    boolean isSourceEnabled;
    @SerializedName("updatedAt")
    String updatedAt;
    @SerializedName("destinations")
    List<RudderServerDestination> destinations;
    @SerializedName("dataplanes")
    Map<RudderDataResidencyServer, List<RudderDataResidencyUrls>> dataResidencyUrls;

    public List<RudderServerDestination> getDestinations() {
        return destinations;
    }
}
