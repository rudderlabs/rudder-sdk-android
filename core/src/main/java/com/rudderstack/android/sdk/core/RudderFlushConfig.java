package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RudderFlushConfig implements Serializable {
    static final String RUDDER_FLUSH_CONFIG_FILE_NAME = "RudderFlushConfig";
    @SerializedName("dataPlaneUrl")
    String dataPlaneUrl;
    @SerializedName("authHeaderString")
    String authHeaderString;
    @SerializedName("anonymousHeaderString")
    String anonymousHeaderString;

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

    @SerializedName("flushQueueSize")
    int flushQueueSize;

    public RudderFlushConfig(String dataPlaneUrl, String authHeaderString, String anonymousHeaderString, int flushQueueSize) {
        this.dataPlaneUrl = dataPlaneUrl;
        this.authHeaderString = authHeaderString;
        this.anonymousHeaderString = anonymousHeaderString;
        this.flushQueueSize = flushQueueSize;
    }
}
