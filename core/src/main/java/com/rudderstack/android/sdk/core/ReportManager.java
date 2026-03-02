package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Map;

/**
 * ReportManager is a no-op class. Metrics and error collection has been removed.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class ReportManager {

    public static final String LABEL_TYPE_OUT_OF_MEMORY = "out_of_memory";
    public static final String LABEL_TYPE_PAYLOAD_NULL = "payload_null";
    public static final String LABEL_TYPE_REQUEST_TIMEOUT = "request_timeout";

    private ReportManager() {
    }

    public static final String LABEL_TYPE = "type";
    public static final String LABEL_INTEGRATION = "integration";
    public static final String LABEL_FLUSH_NUMBER_OF_QUEUES = "queues";
    public static final String LABEL_FLUSH_NUMBER_OF_MESSAGES = "messages";
    public static final String LABEL_TYPE_OPT_OUT = "opt_out";
    public static final String LABEL_TYPE_SDK_DISABLED = "sdk_disabled";
    public static final String LABEL_TYPE_MSG_SIZE_INVALID = "msg_size_invalid";
    public static final String LABEL_TYPE_BATCH_SIZE_INVALID = "batch_size_invalid";
    public static final String LABEL_TYPE_MSG_FILTERED = "msg_filtered";
    public static final String LABEL_TYPE_DESTINATION_DISSENTED = "dissented";
    public static final String LABEL_TYPE_DESTINATION_DISABLED = "disabled";

    public static final String LABEL_TYPE_SOURCE_CONFIG_URL_INVALID = "control_plane_url_invalid";
    public static final String LABEL_TYPE_DATA_PLANE_URL_INVALID = "data_plane_url_invalid";
    public static final String LABEL_TYPE_SOURCE_DISABLED = "source_disabled";
    public static final String LABEL_TYPE_CREATED = "created";
    public static final String LABEL_TYPE_MIGRATE_TO_ENCRYPT = "migrate_to_encrypt";
    public static final String LABEL_TYPE_MIGRATE_TO_DECRYPT = "migrate_to_decrypt";
    public static final String LABEL_TYPE_FAIL_BAD_REQUEST = "bad_request";
    public static final String LABEL_TYPE_FAIL_WRITE_KEY = "writekey_invalid";
    public static final String LABEL_TYPE_FAIL_RESOURCE_NOT_FOUND = "resource_not_found";
    public static final String LABEL_TYPE_FAIL_MAX_RETRY = "max_retries_exhausted";

    public static final String METADATA_SECTION_PERSISTENCE = "persistence";
    public static final String METADATA_SECTION_GZIP = "gzip";

    public static final String METADATA_PERSISTENCE_KEY_IS_ENCRYPTED = "encrypted";
    public static final String METADATA_GZIP_KEY_IS_ENABLED = "enabled";

    // No-op: Stats collection has been removed
    static void enableStatsCollection(Application application, String writeKey,
                                      @NonNull SourceConfiguration.StatsCollection statsCollection, String dataPlaneUrl) {
        // No-op
    }

    static void incrementMessageCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementMessageCounter(int value) {
        // No-op
    }

    static void incrementDiscardedCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementDiscardedCounter(int value) {
        // No-op
    }

    static void incrementDeviceModeEventCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementDeviceModeEventCounter(int value) {
        // No-op
    }

    static void incrementCloudModeEventCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementCloudModeEventCounter(int value) {
        // No-op
    }

    static void incrementDeviceModeDiscardedCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementDeviceModeDiscardedCounter(int value) {
        // No-op
    }

    static void incrementSourceConfigDownloadSuccessCounter(int value) {
        // No-op
    }

    static void incrementSourceConfigDownloadAbortCounter(int value) {
        // No-op
    }

    static void incrementSourceConfigDownloadAbortCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementSourceConfigDownloadRetryCounter(int value) {
        // No-op
    }

    static void incrementCloudModeUploadSuccessCounter(int value) {
        // No-op
    }

    static void incrementCloudModeUploadRetryCounter(int value) {
        // No-op
    }

    static void incrementCloudModeUploadAbortCounter(int value) {
        // No-op
    }

    static void incrementCloudModeUploadAbortCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementDMTSubmittedCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementDMTEventSuccessResponseCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementDMTRetryCounter(int value) {
        // No-op
    }

    static void incrementDMTErrorCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void incrementDbEncryptionCounter(int value, Map<String, String> attributes) {
        // No-op
    }

    static void incrementWorkManagerCallCounter(int value) {
        // No-op
    }

    static void incrementWorkManagerInitCounter(int value) {
        // No-op
    }

    public static void reportError(Throwable throwable) {
        // No-op
    }

    public static void leaveBreadcrumb(String message, Map<String, Object> value) {
        // No-op
    }

    public static void leaveBreadcrumb(String message, String key, Object value) {
        // No-op
    }

    static boolean isStatsReporterAvailable() {
        return false;
    }
}
