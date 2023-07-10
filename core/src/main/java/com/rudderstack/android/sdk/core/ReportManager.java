package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import com.rudderstack.android.ruddermetricsreporterandroid.Metrics;
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter;

import java.util.Map;

public class ReportManager {

    public static final String LABEL_TYPE_OUT_OF_MEMORY = "out_of_memory";

    private ReportManager() {
    }

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

    public static final String LABEL_TYPE_SOURCE_CONFIG_URL_INVALID = "data_plane_url_invalid";
    public static final String LABEL_TYPE_SOURCE_DISABLED = "source_disabled";
    public static final String LABEL_TYPE_WRITE_KEY_INVALID = "writekey_invalid";


    private static LongCounter messageCounter = null;
    private static LongCounter discardedCounter = null;

    private static LongCounter deviceModeEventCounter = null;
    private static LongCounter cloudModeEventCounter = null;
    private static LongCounter deviceModeDiscardedCounter = null;
    private static LongCounter cloudModeUploadSuccessCounter = null;
    private static LongCounter cloudModeUploadAbortCounter = null;
    private static LongCounter cloudModeUploadRetryCounter = null;
    private static LongCounter sourceConfigDownloadRetryCounter = null;
    private static LongCounter sourceConfigDownloadSuccessCounter = null;
    private static LongCounter sourceConfigDownloadAbortCounter = null;


    private static final String EVENTS_SUBMITTED_COUNTER_TAG = "submitted_events";
    private static final String EVENTS_DISCARDED_COUNTER_TAG = "discarded_events";
    private static final String DEVICE_MODE_EVENT_COUNTER_TAG = "dm_event";
    private static final String DEVICE_MODE_DISCARD_COUNTER_TAG = "dm_discard";
    private static final String CLOUD_MODE_EVENT_COUNTER_TAG = "cm_event";
    private static final String CLOUD_MODE_EVENT_UPLOAD_SUCCESS_COUNTER_TAG = "cm_attempt_success";
    private static final String CLOUD_MODE_EVENT_UPLOAD_ABORT_COUNTER_TAG = "cm_attempt_abort";
    private static final String CLOUD_MODE_EVENT_UPLOAD_RETRY_COUNTER_TAG = "cm_attempt_retry";
    private static final String SOURCE_CONFIG_DOWNLOAD_SUCCESS_COUNTER_TAG = "sc_attempt_success";
    private static final String SOURCE_CONFIG_DOWNLOAD_RETRY_COUNTER_TAG = "sc_attempt_retry";
    private static final String SOURCE_CONFIG_DOWNLOAD_ABORT_COUNTER_TAG = "sc_attempt_abort";

    private static Metrics metrics = null;

    static @Nullable Metrics getMetrics() {
        return metrics;
    }
    public static void initiate(Metrics metrics) {
        ReportManager.metrics = metrics;
        ReportManager.messageCounter = metrics.getLongCounter(EVENTS_SUBMITTED_COUNTER_TAG);
        ReportManager.discardedCounter = metrics.getLongCounter(EVENTS_DISCARDED_COUNTER_TAG);
        ReportManager.deviceModeEventCounter = metrics.getLongCounter(DEVICE_MODE_EVENT_COUNTER_TAG);
        ReportManager.cloudModeEventCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_COUNTER_TAG);
        ReportManager.deviceModeDiscardedCounter = metrics.getLongCounter(DEVICE_MODE_DISCARD_COUNTER_TAG);

        cloudModeUploadSuccessCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_UPLOAD_SUCCESS_COUNTER_TAG);
        cloudModeUploadAbortCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_UPLOAD_ABORT_COUNTER_TAG);
        cloudModeUploadRetryCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_UPLOAD_RETRY_COUNTER_TAG);

        sourceConfigDownloadRetryCounter = metrics.getLongCounter(SOURCE_CONFIG_DOWNLOAD_RETRY_COUNTER_TAG);
        sourceConfigDownloadSuccessCounter = metrics.getLongCounter(SOURCE_CONFIG_DOWNLOAD_SUCCESS_COUNTER_TAG);
        sourceConfigDownloadAbortCounter = metrics.getLongCounter(SOURCE_CONFIG_DOWNLOAD_ABORT_COUNTER_TAG);

    }
    private static void incrementCounter(LongCounter counter, int value, Map<String, String> attributes) {
        if (counter != null) {
            counter.add(value, attributes);
        }
    }
    private static void incrementCounter(LongCounter counter, int value) {
        if (counter != null) {
            counter.add(value);
        }
    }

    static void incrementMessageCounter(int value, Map<String, String> attributes) {
        incrementCounter(messageCounter, value, attributes);
    }

    static void incrementMessageCounter(int value) {
        incrementCounter(messageCounter, value);
    }

    static void incrementDiscardedCounter(int value, Map<String, String> attributes) {
        incrementCounter(discardedCounter, value, attributes);
    }

    static void incrementDiscardedCounter(int value) {
        incrementCounter(discardedCounter, value);
    }

    static void incrementDeviceModeEventCounter(int value, Map<String, String> attributes) {
        incrementCounter(deviceModeEventCounter, value, attributes);
    }

    static void incrementDeviceModeEventCounter(int value) {
        incrementCounter(deviceModeEventCounter, value);
    }

    static void incrementCloudModeEventCounter(int value, Map<String, String> attributes) {
        incrementCounter(cloudModeEventCounter, value, attributes);
    }

    static void incrementCloudModeEventCounter(int value) {
        incrementCounter(cloudModeEventCounter, value);
    }

    static void incrementDeviceModeDiscardedCounter(int value, Map<String, String> attributes) {
        incrementCounter(deviceModeDiscardedCounter, value, attributes);
    }

    static void incrementDeviceModeDiscardedCounter(int value) {
        incrementCounter(deviceModeDiscardedCounter, value);
    }
    static void incrementSourceConfigDownloadSuccessCounter(int value) {
        incrementCounter(sourceConfigDownloadSuccessCounter, value);
    }
    static void incrementSourceConfigDownloadAbortCounter(int value) {
        incrementCounter(sourceConfigDownloadAbortCounter, value);
    }
    static void incrementSourceConfigDownloadAbortCounter(int value, Map<String, String> attributes) {
        incrementCounter(sourceConfigDownloadAbortCounter, value, attributes);
    }
    static void incrementSourceConfigDownloadRetryCounter(int value) {
        incrementCounter(sourceConfigDownloadRetryCounter, value);
    }
    static void incrementCloudModeUploadSuccessCounter(int value) {
        incrementCounter(cloudModeUploadSuccessCounter, value);
    }
    static void incrementCloudModeUploadRetryCounter(int value) {
        incrementCounter(cloudModeUploadRetryCounter, value);
    }
    static void incrementCloudModeUploadAbortCounter(int value) {
        incrementCounter(cloudModeUploadAbortCounter, value);
    }
    static void incrementCloudModeUploadAbortCounter(int value, Map<String, String> attributes) {
        incrementCounter(cloudModeUploadAbortCounter, value, attributes);
    }
}
