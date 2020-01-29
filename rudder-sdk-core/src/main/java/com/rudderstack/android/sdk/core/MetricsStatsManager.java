package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_CONFIG_PLANE_FIELD_ID;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_CONFIG_PLANE_FIELD_RESPONSE_TIME;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_CONFIG_PLANE_FIELD_SUCCESS;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_CONFIG_PLANE_TABLE_NAME;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_DATA_PLANE_FIELD_ID;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_DATA_PLANE_FIELD_RESPONSE_TIME;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_DATA_PLANE_FIELD_SIZE;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_DATA_PLANE_FIELD_SUCCESS;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_DATA_PLANE_TABLE_NAME;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_EVENT_FIELD_ID;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_EVENT_FIELD_SIZE;
import static com.rudderstack.android.sdk.core.DBPersistentManager.METRICS_EVENT_TABLE_NAME;

class MetricsStatsManager {
    private static MetricsStatsManager instance;
    private DBPersistentManager dbPersistentManager;
    private RudderPreferenceManager preferenceManager;
    private RudderHttpClient rudderHttpClient;
    private boolean isFirstRun = true;
    private ScheduledExecutorService executorService;
    private static volatile MetricsConfig metricsConfig;

    private MetricsStatsManager(Application application) {
        rudderHttpClient = RudderHttpClient.getInstance();
        RudderLogger.logVerbose("MetricsStatsManager: creating db persistent manager");
        this.dbPersistentManager = DBPersistentManager.getInstance(application);
        RudderLogger.logVerbose("MetricsStatsManager: creating preference manager");
        this.preferenceManager = RudderPreferenceManager.getInstance(application);
        // check and set the begin time if not set.
        this.preferenceManager.getStatsBeginTime();

        RudderLogger.logVerbose("MetricsStatsManager: creating scheduled executor service");
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(getWorkerRunnable(), 0, Utils.STATS_DELAY_COUNT, Utils.STATS_DELAY_TIME_UNIT);
        }
    }

    private Runnable getWorkerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // fetch config-json from the server
                // if server is unavailable use persisted config.
                // if persisted config is not present, disable stats collection
                // if config is present check and update enabled flag
                // if enabled:
                // ==> schedule the worker to iterate every hour to check the stats dumped and keep in the queue or requests.
                // ==> iterate over queue requests and flush them to the server.
                // if not enabled:
                // ==> stop scheduled worker if started
                RudderLogger.logVerbose("MetricsStatsManager: starting metrics operation");
                if (isFirstRun) {
                    RudderLogger.logVerbose("MetricsStatsManager: initial run. downloading config json");
                    downloadConfigJson();
                    if (metricsConfig == null) {
                        RudderLogger.logVerbose("MetricsStatsManager: initial run. downloaded config is null");
                        parsePersistedConfig();
                    }
                    if (metricsConfig == null) {
                        RudderLogger.logVerbose("MetricsStatsManager: initial run. persisted config is null. shutting down service");
                        if (executorService != null && !executorService.isShutdown()) {
                            executorService.shutdownNow();
                        }
                    }
                    isFirstRun = false;
                }
                if (!metricsConfig.isEnabled()) {
                    RudderLogger.logVerbose("MetricsStatsManager: logging is not enabled. shutting down service");
                    if (executorService != null && !executorService.isShutdown()) {
                        executorService.shutdownNow();
                    }
                }
                fetchLastMetrics();
                flushRequestsToServer();
            }
        };
    }

    private void fetchLastMetrics() {
        boolean logRequest = false;
        Map<String, String> params = new HashMap<>();
        for (MetricsStats stats : MetricsStats.values()) {
            if (stats.getSampleRate() > 0) {
                List<Integer> list = this.dbPersistentManager.getStats(stats.getQuerySql());
                if (list != null && !list.isEmpty()) {
                    logRequest = true;
                    params.put(stats.getParamPrefix() + "Max", String.valueOf(Utils.computeMax(list)));
                    params.put(stats.getParamPrefix() + "Min", String.valueOf(Utils.computeMin(list)));
                    params.put(stats.getParamPrefix() + "Med", String.valueOf(Utils.computeMedian(list)));
                    float mean = Utils.computeAverage(list);
                    params.put(stats.getParamPrefix() + "Avg", String.valueOf(mean));
                    params.put(stats.getParamPrefix() + "Sd", String.valueOf(Utils.computeDeviation(list, mean)));
                }
            }
        }
        int retryCountConfigPlane = dbPersistentManager.getRetryCountConfigPlane();
        if (retryCountConfigPlane > -1) {
            logRequest = true;
            params.put("retryCountConfigPlane", String.valueOf(retryCountConfigPlane));
        }
        int retryCountDataPlane = dbPersistentManager.getRetryCountDataPlane();
        if (retryCountDataPlane > -1) {
            logRequest = true;
            params.put("retryCountDataPlane", String.valueOf(retryCountDataPlane));
        }

        if (logRequest) {
            params.put("writeKey", metricsConfig.getWriteKey());
            RudderContext context = RudderElementCache.getCachedContext();
            params.put("os", context.getOsInfo().getName());
            params.put("version", context.getOsInfo().getVersion());
            params.put("sdk", context.getLibraryInfo().getVersion());
            params.put("begin", String.valueOf(preferenceManager.getStatsBeginTime()));
            long statsEndTime = System.currentTimeMillis();
            params.put("end", String.valueOf(statsEndTime));
            params.put("fingerprint", preferenceManager.getRudderStatsFingerPrint());

            StringBuilder queryBuilder = new StringBuilder();
            for (String key : params.keySet()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(String.format("%s=%s", key, params.get(key)));
            }
            dbPersistentManager.saveStatsRequest(String.format("%s?%s", metricsConfig.getDataPlaneUrl(), queryBuilder.toString()));

            dbPersistentManager.deleteAllMetrics();
            preferenceManager.updateRudderStatsBeginTime(System.currentTimeMillis());
        }
    }

    private void flushRequestsToServer() {
        SparseArray<String> requests = dbPersistentManager.getMetricsRequests();
        if (requests.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < requests.size(); index++) {
                String response = rudderHttpClient.get(String.format(Locale.US, "%s&timestamp=%d", requests.valueAt(index), System.currentTimeMillis()), null);
                if (response.equalsIgnoreCase("OK")) {
                    builder.append(requests.keyAt(index));
                }
                builder.append(",");
            }
            // remove last "," character
            builder.deleteCharAt(builder.length() - 1);
            dbPersistentManager.clearMetricsRequestFromDB(builder.toString());
        }
    }

    private void parsePersistedConfig() {
        this.parseConfigJson(preferenceManager.getStatsConfigJson());
    }

    private void downloadConfigJson() {
        String configUrl = String.format(Locale.US, Constants.STATS_CONFIG_URL_TEMPLATE, RudderClient.getWriteKey());
        RudderLogger.logDebug("MetricsStatsManager: downloadConfigJson: configUrl: " + configUrl);
        this.parseConfigJson(rudderHttpClient.get(configUrl, null));
    }

    private void parseConfigJson(String json) {
        if (!TextUtils.isEmpty(json)) {
            metricsConfig = new Gson().fromJson(json, MetricsConfig.class);
        }
    }

    static MetricsStatsManager getInstance(Application application) {
        if (instance == null) {
            instance = new MetricsStatsManager(application);
        }
        return instance;
    }

    boolean isEnabled() {
        return metricsConfig == null || metricsConfig.isEnabled();
    }

    private enum MetricsStats implements StatsInterface {
        EVENT_SIZE() {
            @Override
            @NonNull
            public String getQuerySql() {
                return String.format(Locale.US, "SELECT %s FROM %s WHERE %s IN (SELECT %s FROM %s ORDER BY RANDOM() LIMIT %d)", METRICS_EVENT_FIELD_SIZE, METRICS_EVENT_TABLE_NAME, METRICS_EVENT_FIELD_ID, METRICS_EVENT_FIELD_ID, METRICS_EVENT_TABLE_NAME, getSampleRate());
            }

            @NonNull
            @Override
            public String getParamPrefix() {
                return "eventSize";
            }

            @Override
            public int getSampleRate() {
                return MetricsStatsManager.metricsConfig.getEventSize();
            }
        },
        BATCH_SIZE {
            @NonNull
            @Override
            public String getQuerySql() {
                // only consider successful batch events
                return String.format(Locale.US, "SELECT %s FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=1 ORDER BY RANDOM() LIMIT %d)", METRICS_DATA_PLANE_FIELD_SIZE, METRICS_DATA_PLANE_TABLE_NAME, METRICS_DATA_PLANE_FIELD_ID, METRICS_DATA_PLANE_FIELD_ID, METRICS_DATA_PLANE_TABLE_NAME, METRICS_DATA_PLANE_FIELD_SUCCESS, getSampleRate());
            }

            @NonNull
            @Override
            public String getParamPrefix() {
                return "batchSize";
            }

            @Override
            public int getSampleRate() {
                return MetricsStatsManager.metricsConfig.getAverageBatchSize();
            }
        },
        DATA_PLANE_RESPONSE_TIME {
            @NonNull
            @Override
            public String getQuerySql() {
                return String.format(Locale.US, "SELECT %s FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=1 ORDER BY RANDOM() LIMIT %d)", METRICS_DATA_PLANE_FIELD_RESPONSE_TIME, METRICS_DATA_PLANE_TABLE_NAME, METRICS_DATA_PLANE_FIELD_ID, METRICS_DATA_PLANE_FIELD_ID, METRICS_DATA_PLANE_TABLE_NAME, METRICS_DATA_PLANE_FIELD_SUCCESS, getSampleRate());
            }

            @NonNull
            @Override
            public String getParamPrefix() {
                return "dataPlaneResponseTime";
            }

            @Override
            public int getSampleRate() {
                return MetricsStatsManager.metricsConfig.getDataPlaneResponseTime();
            }
        },
        CONFIG_RESPONSE_TIME {
            @NonNull
            @Override
            public String getQuerySql() {
                return String.format(Locale.US, "SELECT %s FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=1 ORDER BY RANDOM() LIMIT %d)", METRICS_CONFIG_PLANE_FIELD_RESPONSE_TIME, METRICS_CONFIG_PLANE_TABLE_NAME, METRICS_CONFIG_PLANE_FIELD_ID, METRICS_CONFIG_PLANE_FIELD_ID, METRICS_CONFIG_PLANE_TABLE_NAME, METRICS_CONFIG_PLANE_FIELD_SUCCESS, getSampleRate());
            }

            @NonNull
            @Override
            public String getParamPrefix() {
                return "configPlaneResponseTime";
            }

            @Override
            public int getSampleRate() {
                return MetricsStatsManager.metricsConfig.getConfigResponseTime();
            }
        }
    }

    private interface StatsInterface {
        @NonNull
        String getQuerySql();

        @NonNull
        String getParamPrefix();

        int getSampleRate();
    }
}
