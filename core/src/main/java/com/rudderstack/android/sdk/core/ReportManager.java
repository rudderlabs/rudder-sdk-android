package com.rudderstack.android.sdk.core;

import com.rudderstack.android.ruddermetricsreporterandroid.Metrics;
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter;

public class ReportManager {

    public static final String LABEL_TYPE_OUT_OF_MEMORY = "out_of_memory";

    private ReportManager(){}
    public static final String LABEL_TYPE = "type";
    public static final String LABEL_INTEGRATION = "integration";
    public static final String LABEL_FLUSH_NUMBER_OF_QUEUES = "queues";
    public static final String LABEL_FLUSH_NUMBER_OF_MESSAGES = "messages";
    public static final String LABEL_TYPE_OPT_OUT = "opt_out";
    public static final String LABEL_TYPE_SDK_DISABLED = "sdk_disabled";
    public static final String LABEL_TYPE_MSG_SIZE_INVALID = "msg_size_invalid";
    public static final String LABEL_TYPE_MSG_FILTERED = "msg_filtered";
    public static final String LABEL_TYPE_DESTINATION_DISSENTED = "dissented";
    public static final String LABEL_TYPE_DESTINATION_DISABLED = "disabled";

    private static LongCounter messageCounter;
    private static LongCounter discardedCounter;

    private static LongCounter deviceModeEventCounter;
    private static LongCounter cloudModeEventCounter;
    private static LongCounter deviceModeDiscardedCounter;
    private static final String EVENTS_SUBMITTED_COUNTER_TAG = "submitted_events";
    private static final String EVENTS_DISCARDED_COUNTER_TAG = "discarded_events";
    private static final String DEVICE_MODE_EVENT_COUNTER_TAG = "dm_event";
    private static final String DEVICE_MODE_DISCARD_COUNTER_TAG = "dm_discard";
    private static final String CLOUD_MODE_EVENT_COUNTER_TAG = "cm_event";

    public static void initiate(Metrics metrics) {
        ReportManager.messageCounter = metrics.getLongCounter(EVENTS_SUBMITTED_COUNTER_TAG);
        ReportManager.discardedCounter = metrics.getLongCounter(EVENTS_DISCARDED_COUNTER_TAG);
        ReportManager.deviceModeEventCounter = metrics.getLongCounter(DEVICE_MODE_EVENT_COUNTER_TAG);
        ReportManager.cloudModeEventCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_COUNTER_TAG);
        ReportManager.deviceModeDiscardedCounter = metrics.getLongCounter(DEVICE_MODE_DISCARD_COUNTER_TAG);

    }
    static LongCounter messageCounter() {
        return messageCounter;
    }
    static LongCounter discardedCounter() {
        return discardedCounter;
    }
    static LongCounter deviceModeEventCounter() {
        return deviceModeEventCounter;
    }
    static LongCounter cloudModeEventCounter() {
        return cloudModeEventCounter;
    }
    static LongCounter deviceModeDiscardedCounter() {
        return deviceModeDiscardedCounter;
    }
}
