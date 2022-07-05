package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

class RudderServerDestination implements Serializable {
    @SerializedName("id")
    String destinationId;
    @SerializedName("name")
    String destinationName;
    @SerializedName("enabled")
    boolean isDestinationEnabled;
    @SerializedName("updatedAt")
    String updatedAt;
    @SerializedName("destinationDefinition")
    RudderServerDestinationDefinition destinationDefinition;
    @SerializedName("config")
    Object destinationConfig;
    @SerializedName("areTransformationsConnected")
    boolean areTransformationsConnected;
}
