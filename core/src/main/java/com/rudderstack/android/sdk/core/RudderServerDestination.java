package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RudderServerDestination implements Serializable {
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
    @SerializedName("shouldApplyDeviceModeTransformation")
    boolean shouldApplyDeviceModeTransformation;
    @SerializedName("propagateEventsUntransformedOnError")
    boolean propagateEventsUntransformedOnError;

    public String getDestinationId() {
        return destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public boolean isDestinationEnabled() {
        return isDestinationEnabled;
    }

    public RudderServerDestinationDefinition getDestinationDefinition() {
        return destinationDefinition;
    }

    public Object getDestinationConfig() {
        return destinationConfig;
    }
}
