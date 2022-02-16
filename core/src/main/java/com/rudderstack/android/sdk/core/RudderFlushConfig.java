package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RudderFlushConfig implements Serializable {
    @SerializedName("dataPlaneUrl")
    String dataPlaneUrl;
    @SerializedName("authHeaderString")
    String authHeaderString;
    @SerializedName("anonymousHeaderString")
    String anonymousHeaderString;
    @SerializedName("logLevel")
    int logLevel;
    @SerializedName("flushQueueSize")
    int flushQueueSize;

    public String getDataPlaneUrl() {
        return dataPlaneUrl;
    }

    public String getAuthHeaderString() {
        return authHeaderString;
    }

    public String getAnonymousHeaderString() {
        return anonymousHeaderString;
    }

    public int getFlushQueueSize() {
        return flushQueueSize;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public RudderFlushConfig(String dataPlaneUrl, String authHeaderString, String anonymousHeaderString, int flushQueueSize, int logLevel) {
        this.dataPlaneUrl = dataPlaneUrl;
        this.authHeaderString = authHeaderString;
        this.anonymousHeaderString = anonymousHeaderString;
        this.flushQueueSize = flushQueueSize;
        this.logLevel = logLevel;
    }
}
