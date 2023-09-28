package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.rudderstack.android.ruddermetricsreporterandroid.Configuration;
import com.rudderstack.android.ruddermetricsreporterandroid.DefaultRudderReporter;
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata;
import com.rudderstack.android.ruddermetricsreporterandroid.Metrics;
import com.rudderstack.android.ruddermetricsreporterandroid.RudderReporter;
import com.rudderstack.android.ruddermetricsreporterandroid.error.BreadcrumbType;
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorClient;
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter;
import com.rudderstack.gsonrudderadapter.GsonAdapter;

import java.util.Collections;
import java.util.Map;

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
    public static final String LABEL_TYPE_FAIL_MAX_RETRY = "max_retries_exhausted";
//    public static final String LABEL_TYPE_WRITE_KEY_INVALID = "writekey_invalid";

    private static final long METRICS_UPLOAD_INTERVAL = 30_000;
    private static final long METRICS_FLUSH_COUNT = 10;

    private static LongCounter messageCounter = null;
    private static LongCounter discardedCounter = null;

    private static LongCounter deviceModeEventCounter = null;
    private static LongCounter cloudModeEventCounter = null;
    private static LongCounter dmtEventSubmittedCounter = null;
    private static LongCounter dmtEventSuccessResponseCounter = null;
    private static LongCounter dmtEventRetryCounter = null;
    private static LongCounter dmtEventAbortCounter = null;
    private static LongCounter deviceModeDiscardedCounter = null;
    private static LongCounter cloudModeUploadSuccessCounter = null;
    private static LongCounter cloudModeUploadAbortCounter = null;
    private static LongCounter cloudModeUploadRetryCounter = null;
    private static LongCounter sourceConfigDownloadRetryCounter = null;
    private static LongCounter sourceConfigDownloadSuccessCounter = null;
    private static LongCounter sourceConfigDownloadAbortCounter = null;
    private static LongCounter dbEncryptionCounter = null;

    private static LongCounter workManagerInitializationCounter = null;


    private static final String EVENTS_SUBMITTED_COUNTER_TAG = "submitted_events";
    private static final String EVENTS_DISCARDED_COUNTER_TAG = "discarded_events";
    private static final String DEVICE_MODE_EVENT_COUNTER_TAG = "dm_event";
    private static final String DEVICE_MODE_DISCARD_COUNTER_TAG = "dm_discard";
    private static final String CLOUD_MODE_EVENT_COUNTER_TAG = "cm_event";
    private static final String DMT_SUBMITTED_COUNTER_TAG = "dmt_submitted";
    private static final String DMT_RESPONSE_COUNTER_TAG = "dmt_response";
    private static final String DMT_DISCARD_COUNTER_TAG = "dmt_discard";
    private static final String DMT_RETRY_COUNTER_TAG = "dmt_retry";
    private static final String CLOUD_MODE_EVENT_UPLOAD_SUCCESS_COUNTER_TAG = "cm_attempt_success";
    private static final String CLOUD_MODE_EVENT_UPLOAD_ABORT_COUNTER_TAG = "cm_attempt_abort";
    private static final String CLOUD_MODE_EVENT_UPLOAD_RETRY_COUNTER_TAG = "cm_attempt_retry";
    private static final String SOURCE_CONFIG_DOWNLOAD_SUCCESS_COUNTER_TAG = "sc_attempt_success";
    private static final String SOURCE_CONFIG_DOWNLOAD_RETRY_COUNTER_TAG = "sc_attempt_retry";

    private static final String SOURCE_CONFIG_DOWNLOAD_ABORT_COUNTER_TAG = "sc_attempt_abort";
    private static final String FLUSH_WORKER_INIT_COUNTER_TAG = "flush_worker_init";
    private static final String ENCRYPTED_DB_COUNTER_TAG = "db_encrypt";

    private static final String METRICS_URL_DEV = "https://sdk-metrics.dev-rudder.rudderlabs.com/";
    private static final String METRICS_URL_PROD = "https://sdk-metrics.rudderstack.com/";
    private static Metrics metrics = null;
    private static ErrorClient errorStatsClient = null;

    public static final String METADATA_SECTION_PERSISTENCE = "persistence";
    public static final String METADATA_SECTION_GZIP = "gzip";

    public static final String METADATA_PERSISTENCE_KEY_IS_ENCRYPTED = "encrypted";
    public static final String METADATA_GZIP_KEY_IS_ENABLED = "enabled";

    public static void initiate(@Nullable Metrics metrics, @Nullable ErrorClient errorStatsClient) {
        ReportManager.metrics = metrics;
        ReportManager.errorStatsClient = errorStatsClient;
        if (metrics != null)
            createCounters(metrics);

    }

    private static void createCounters(@NonNull Metrics metrics) {
        ReportManager.messageCounter = metrics.getLongCounter(EVENTS_SUBMITTED_COUNTER_TAG);
        ReportManager.discardedCounter = metrics.getLongCounter(EVENTS_DISCARDED_COUNTER_TAG);
        ReportManager.deviceModeEventCounter = metrics.getLongCounter(DEVICE_MODE_EVENT_COUNTER_TAG);
        ReportManager.cloudModeEventCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_COUNTER_TAG);
        ReportManager.dmtEventSubmittedCounter = metrics.getLongCounter(DMT_SUBMITTED_COUNTER_TAG);
        ReportManager.deviceModeDiscardedCounter = metrics.getLongCounter(DEVICE_MODE_DISCARD_COUNTER_TAG);

        cloudModeUploadSuccessCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_UPLOAD_SUCCESS_COUNTER_TAG);
        cloudModeUploadAbortCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_UPLOAD_ABORT_COUNTER_TAG);
        cloudModeUploadRetryCounter = metrics.getLongCounter(CLOUD_MODE_EVENT_UPLOAD_RETRY_COUNTER_TAG);

        sourceConfigDownloadRetryCounter = metrics.getLongCounter(SOURCE_CONFIG_DOWNLOAD_RETRY_COUNTER_TAG);
        sourceConfigDownloadSuccessCounter = metrics.getLongCounter(SOURCE_CONFIG_DOWNLOAD_SUCCESS_COUNTER_TAG);
        sourceConfigDownloadAbortCounter = metrics.getLongCounter(SOURCE_CONFIG_DOWNLOAD_ABORT_COUNTER_TAG);
        dbEncryptionCounter = metrics.getLongCounter(ENCRYPTED_DB_COUNTER_TAG);

        dmtEventSubmittedCounter = metrics.getLongCounter(DMT_SUBMITTED_COUNTER_TAG);
        dmtEventSuccessResponseCounter = metrics.getLongCounter(DMT_RESPONSE_COUNTER_TAG);
        dmtEventRetryCounter = metrics.getLongCounter(DMT_RETRY_COUNTER_TAG);
        dmtEventAbortCounter = metrics.getLongCounter(DMT_DISCARD_COUNTER_TAG);

        workManagerInitializationCounter = metrics.getLongCounter(FLUSH_WORKER_INIT_COUNTER_TAG);
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

    private static @Nullable RudderReporter rudderReporter;

    @SuppressWarnings("ConstantConditions")
    static void enableStatsCollection(Application application, String writeKey,
                                      @NonNull SourceConfiguration.StatsCollection statsCollection) {
        if (!isStatsReporterAvailable()) {
            if (!(statsCollection.getMetrics().isEnabled() || statsCollection.getErrors().isEnabled())) {
                RudderLogger.logDebug("EventRepository: Stats collection is not enabled");
                return;
            }
            RudderLogger.logDebug("EventRepository: Creating Stats Reporter");
            initiateRudderReporter(application, writeKey, statsCollection.getMetrics().isEnabled(),
                    statsCollection.getErrors().isEnabled());
            RudderLogger.logDebug("EventRepository: Metrics collection is not initialized");
            return;
        }
        if (!(statsCollection.getMetrics().isEnabled() || statsCollection.getErrors().isEnabled())) {
            RudderLogger.logDebug("EventRepository: Stats collection is not enabled: Shutting down Stats Reporter");
            rudderReporter.shutdown();
            return;
        }
        checkAndUpdateMetricsCollection(statsCollection.getMetrics().isEnabled());
        checkAndUpdateErrorsCollection(statsCollection.getErrors().isEnabled());
        RudderLogger.logDebug("EventRepository: Metrics Collection is enabled");

    }

    @SuppressWarnings("ConstantConditions")
    private static void checkAndUpdateMetricsCollection(boolean isMetricsEnabled) {
        if (!isStatsReporterAvailable())
            return;
        RudderLogger.logDebug("EventRepository: Enabling Metrics Collection: " + isMetricsEnabled);
        if (metrics == null) {
            if (!isMetricsEnabled)
                return;
            metrics = rudderReporter.getMetrics();
        }
        metrics.enable(isMetricsEnabled);
    }

    @SuppressWarnings("ConstantConditions")
    private static void checkAndUpdateErrorsCollection(boolean isErrorsEnabled) {
        if (!isStatsReporterAvailable())
            return;
        RudderLogger.logDebug("EventRepository: Enabling Errors Collection: " + isErrorsEnabled);
        if (errorStatsClient == null) {
            if (!isErrorsEnabled)
                return;
            errorStatsClient = rudderReporter.getErrorClient();
        }
        errorStatsClient.enable(isErrorsEnabled);
    }

    private static void initiateRudderReporter(Context context, @Nullable String writeKey,
                                               boolean isMetricsEnabled, boolean isErrorsEnabled) {
        RudderLogger.logDebug("EventRepository: Creating RudderReporter isMetricsEnabled: " + isMetricsEnabled + " isErrorsEnabled: " + isErrorsEnabled);
        String writeKeyOrBlank = writeKey == null ? "" : writeKey;
        if (rudderReporter == null) {
            rudderReporter = new DefaultRudderReporter(context, METRICS_URL_PROD,
                    new Configuration(new LibraryMetadata(
                            BuildConfig.LIBRARY_PACKAGE_NAME, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, writeKeyOrBlank
                    )), new GsonAdapter(), isMetricsEnabled, isErrorsEnabled);
            rudderReporter.getSyncer().startScheduledSyncs(METRICS_UPLOAD_INTERVAL,
                    true, METRICS_FLUSH_COUNT);
            //we default to null if metrics or errors are not enabled
            ReportManager.initiate(isMetricsEnabled ? rudderReporter.getMetrics() : null,
                    isErrorsEnabled ? rudderReporter.getErrorClient() : null);
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
    static void incrementDMTSubmittedCounter(int value, Map<String, String> attributes) {
        incrementCounter(dmtEventSubmittedCounter, value, attributes);
    }
    static void incrementDMTEventSuccessResponseCounter(int value, Map<String, String> attributes) {
        incrementCounter(dmtEventSuccessResponseCounter, value, attributes);
    }
    static void incrementDMTRetryCounter(int value) {
        incrementCounter(dmtEventRetryCounter, value);
    }

    static void incrementDMTErrorCounter(int value, Map<String, String> attributes) {
        incrementCounter(dmtEventAbortCounter, value, attributes);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void incrementDbEncryptionCounter(int value, Map<String, String> attributes) {
        incrementCounter(dbEncryptionCounter, value, attributes);
    }

    static void incrementWorkManagerInitializationCounter(int value) {
        incrementCounter(workManagerInitializationCounter, value);
    }

    public static void reportError(Throwable throwable) {
        if (errorStatsClient != null) {
            errorStatsClient.notify(throwable);
        }
    }

    public static void leaveBreadcrumb(String message, Map<String, Object> value) {
        if (errorStatsClient != null) {
            errorStatsClient.leaveBreadcrumb(message, value, BreadcrumbType.MANUAL);
        }
    }

    public static void leaveBreadcrumb(String message, String key, Object value) {
        if (errorStatsClient != null) {
            errorStatsClient.leaveBreadcrumb(message, Collections.singletonMap(key, value),
                    BreadcrumbType.MANUAL);
        }
    }

    static boolean isStatsReporterAvailable() {
        return rudderReporter != null;
    }
}
