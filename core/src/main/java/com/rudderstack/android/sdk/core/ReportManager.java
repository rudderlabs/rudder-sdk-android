package com.rudderstack.android.sdk.core;

import com.rudderstack.android.ruddermetricsreporterandroid.Metrics;
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter;

import java.util.Map;

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

    private static LongCounter messageCounter = null;
    private static LongCounter discardedCounter = null;

    private static LongCounter deviceModeEventCounter = null;
    private static LongCounter cloudModeEventCounter = null;
    private static LongCounter deviceModeDiscardedCounter = null;
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
    static void incrementMessageCounter(int value, Map<String, String> attributes) {
        if (messageCounter != null) {
            messageCounter.add(value, attributes);
        }
    }
    static void incrementMessageCounter(int value) {
        if (messageCounter != null) {
            messageCounter.add(value);
        }
    }
    static void incrementDiscardedCounter(int value, Map<String, String> attributes) {
        if(discardedCounter != null){
            discardedCounter.add(value, attributes);
        }
    }
    static void incrementDiscardedCounter(int value) {
        if(discardedCounter != null){
            discardedCounter.add(value);
        }
    }
    static void incrementDeviceModeEventCounter(int value, Map<String, String> attributes) {
        if(deviceModeEventCounter != null){
            deviceModeEventCounter.add(value, attributes);
        }
    }
    static void incrementDeviceModeEventCounter(int value) {
        if(deviceModeEventCounter != null){
            deviceModeEventCounter.add(value);
        }
    }
    static void incrementCloudModeEventCounter(int value, Map<String, String> attributes) {
        if(cloudModeEventCounter != null){
            cloudModeEventCounter.add(value, attributes);
        }
    }
    static void incrementCloudModeEventCounter(int value) {
        if(cloudModeEventCounter != null){
            cloudModeEventCounter.add(value);
        }
    }
    static void incrementDeviceModeDiscardedCounter(int value, Map<String, String> attributes) {
        if(deviceModeDiscardedCounter != null){
            deviceModeDiscardedCounter.add(value, attributes);
        }
    }
    static void incrementDeviceModeDiscardedCounter(int value) {
        if(deviceModeDiscardedCounter != null){
            deviceModeDiscardedCounter.add(value);
        }
    }
}
