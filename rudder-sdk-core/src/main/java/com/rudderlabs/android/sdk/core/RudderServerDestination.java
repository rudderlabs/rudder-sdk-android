package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;

class RudderServerDestination {
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
}
