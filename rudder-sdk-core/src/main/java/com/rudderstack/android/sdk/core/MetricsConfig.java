package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.util.Utils;

class MetricsConfig {
    @SerializedName("enabled")
    private boolean isEnabled = Utils.METRICS_ENABLED;
    @SerializedName("configResponseTime")
    private int configResponseTime;
    @SerializedName("dataPlaneResponseTime")
    private int dataPlaneResponseTime;
    @SerializedName("eventSize")
    private int eventSize;
    @SerializedName("averageBatchSize")
    private int averageBatchSize;
    @SerializedName("retryCountConfigPlane")
    private int retryCountConfigPlane;
    @SerializedName("retryCountDataPlane")
    private int retryCountDataPlane;
    @SerializedName("writeKey")
    private String writeKey;
    @SerializedName("dataPlaneUrl")
    private String dataPlaneUrl;

    boolean isEnabled() {
        return isEnabled;
    }

    int getConfigResponseTime() {
        return configResponseTime;
    }

    int getDataPlaneResponseTime() {
        return dataPlaneResponseTime;
    }

    int getEventSize() {
        return eventSize;
    }

    int getAverageBatchSize() {
        return averageBatchSize;
    }

    int getRetryCountConfigPlane() {
        return retryCountConfigPlane;
    }

    int getRetryCountDataPlane() {
        return retryCountDataPlane;
    }

    public String getWriteKey() {
        return writeKey;
    }

    public String getDataPlaneUrl() {
        return dataPlaneUrl;
    }
}
