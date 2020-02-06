package com.rudderstack.android.sdk.core;

import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/*
 * Config class for RudderClient
 * - endPointUri -> API endpoint for flushing events
 * - flushQueueSize -> maximum number of events to be batched
 * - dbCountThreshold -> maximum number of events to be persisted in local DB
 * - sleepTimeOut -> timeout for automatic flushing since last successful flush
 * - logLevel -> level of logging for debugging
 * - configRefreshInterval -> time in hours as interval of downloading config from config server
 * - trackLifecycleEvents -> whether track lifecycle events automatically
 * - recordScreenViews -> whether we should record screen views automatically
 *
 * default values are set at Constants file
 *
 * */
public class RudderConfig {
    private String endPointUri;
    private int flushQueueSize;
    private int dbCountThreshold;
    private int sleepTimeOut;
    private int logLevel;
    private int configRefreshInterval;
    private boolean trackLifecycleEvents;
    private boolean recordScreenViews;
    private List<RudderIntegration.Factory> factories;

    RudderConfig() {
        this(
                Constants.BASE_URL,
                Constants.FLUSH_QUEUE_SIZE,
                Constants.DB_COUNT_THRESHOLD,
                Constants.SLEEP_TIMEOUT,
                RudderLogger.RudderLogLevel.ERROR,
                Constants.CONFIG_REFRESH_INTERVAL,
                Constants.TRACK_LIFECYCLE_EVENTS,
                Constants.RECORD_SCREEN_VIEWS,
                null
        );
    }

    private RudderConfig(
            String endPointUri,
            int flushQueueSize,
            int dbCountThreshold,
            int sleepTimeOut,
            int logLevel,
            int configRefreshInterval,
            boolean trackLifecycleEvents,
            boolean recordScreenViews,
            List<RudderIntegration.Factory> factories
    ) {
        RudderLogger.init(logLevel);

        if (TextUtils.isEmpty(endPointUri)) {
            RudderLogger.logError("endPointUri can not be null or empty. Set to default.");
            this.endPointUri = Constants.BASE_URL;
        } else if (!URLUtil.isValidUrl(endPointUri)) {
            RudderLogger.logError("Malformed endPointUri. Set to default");
            this.endPointUri = Constants.BASE_URL;
        } else {
            if (!endPointUri.endsWith("/")) endPointUri += "/";
            this.endPointUri = endPointUri;
        }

        if (flushQueueSize < Utils.MIN_FLUSH_QUEUE_SIZE || flushQueueSize > Utils.MAX_FLUSH_QUEUE_SIZE) {
            RudderLogger.logError("flushQueueSize is out of range. Min: 1, Max: 100. Set to default");
            this.flushQueueSize = Constants.FLUSH_QUEUE_SIZE;
        } else {
            this.flushQueueSize = flushQueueSize;
        }

        this.logLevel = logLevel;

        if (dbCountThreshold < 0) {
            RudderLogger.logError("invalid dbCountThreshold. Set to default");
            this.dbCountThreshold = Constants.DB_COUNT_THRESHOLD;
        } else {
            this.dbCountThreshold = dbCountThreshold;
        }

        if (configRefreshInterval > Utils.MAX_CONFIG_REFRESH_INTERVAL) {
            this.configRefreshInterval = Utils.MAX_CONFIG_REFRESH_INTERVAL;
        } else if (configRefreshInterval < Utils.MIN_CONFIG_REFRESH_INTERVAL) {
            this.configRefreshInterval = Utils.MIN_CONFIG_REFRESH_INTERVAL;
        } else {
            this.configRefreshInterval = configRefreshInterval;
        }

        if (sleepTimeOut < Utils.MIN_SLEEP_TIMEOUT) {
            RudderLogger.logError("invalid sleepTimeOut. Set to default");
            this.sleepTimeOut = Constants.SLEEP_TIMEOUT;
        } else {
            this.sleepTimeOut = sleepTimeOut;
        }

        this.trackLifecycleEvents = trackLifecycleEvents;
        this.recordScreenViews = recordScreenViews;

        if (factories != null && !factories.isEmpty()) {
            this.factories = factories;
        }
    }

    /**
     * @return endPointUrl (your data-plane url)
     */
    @NonNull
    public String getEndPointUri() {
        return endPointUri;
    }

    /**
     * @return flushQueueSize (# of events in a payload for v1/batch request)
     */
    public int getFlushQueueSize() {
        return flushQueueSize;
    }

    /**
     * @return dbCountThreshold (# of events to be kept in DB before deleting older events)
     */
    public int getDbCountThreshold() {
        return dbCountThreshold;
    }

    /**
     * @return sleepTimeOut (# of seconds to wait before sending a batch)
     */
    public int getSleepTimeOut() {
        return sleepTimeOut;
    }

    /**
     * @return logLevel (how much log we generate from the SDK)
     */
    public int getLogLevel() {
        return logLevel;
    }

    /**
     * @return configRefreshInterval (how often the server config should be fetched from the server)
     */
    public int getConfigRefreshInterval() {
        return configRefreshInterval;
    }

    /**
     * @return trackLifecycleEvents (whether we are tracking the Application lifecycle events except
     * "Application Installed" and "Application Updated"
     */
    public boolean isTrackLifecycleEvents() {
        return trackLifecycleEvents;
    }

    /**
     * @return recordScreenViews (whether we are recording the screen views automatically)
     */
    public boolean isRecordScreenViews() {
        return recordScreenViews;
    }

    /**
     * @return factories (list of native SDK factories integrated in the application)
     */
    @Nullable
    public List<RudderIntegration.Factory> getFactories() {
        return factories;
    }

    void setEndPointUri(String endPointUri) {
        this.endPointUri = endPointUri;
    }

    void setFlushQueueSize(int flushQueueSize) {
        this.flushQueueSize = flushQueueSize;
    }

    void setDbCountThreshold(int dbCountThreshold) {
        this.dbCountThreshold = dbCountThreshold;
    }

    void setSleepTimeOut(int sleepTimeOut) {
        this.sleepTimeOut = sleepTimeOut;
    }

    void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    void setFactories(List<RudderIntegration.Factory> factories) {
        this.factories = factories;
    }

    void setConfigRefreshInterval(int configRefreshInterval) {
        this.configRefreshInterval = configRefreshInterval;
    }

    void setTrackLifecycleEvents(boolean trackLifecycleEvents) {
        this.trackLifecycleEvents = trackLifecycleEvents;
    }

    void setRecordScreenViews(boolean recordScreenViews) {
        this.recordScreenViews = recordScreenViews;
    }

    /**
     * @return custom toString implementation for RudderConfig
     */
    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.US, "RudderConfig: endPointUrl:%s | flushQueueSize: %d | dbCountThreshold: %d | sleepTimeOut: %d | logLevel: %d", endPointUri, flushQueueSize, dbCountThreshold, sleepTimeOut, logLevel);
    }


    /**
     * Builder class for RudderConfig
     */
    public static class Builder {
        private List<RudderIntegration.Factory> factories = new ArrayList<>();

        /**
         * @param factory : Instance of RudderIntegration.Factory (for more information visit https://docs.rudderstack.com)
         * @return RudderConfig.Builder
         */
        public Builder withFactory(@NonNull RudderIntegration.Factory factory) {
            this.factories.add(factory);
            return this;
        }

        /**
         * @param factories List of instances of RudderIntegration.Factory
         * @return RudderConfig.Builder
         */
        public Builder withFactories(@NonNull List<RudderIntegration.Factory> factories) {
            this.factories.addAll(factories);
            return this;
        }

        /**
         * @param factories List of instances of RudderIntegration.Factory
         * @return RudderConfig.Builder
         */
        public Builder withFactories(@NonNull RudderIntegration.Factory... factories) {
            Collections.addAll(this.factories, factories);
            return this;
        }

        private String endPointUri = Constants.BASE_URL;

        /**
         * @param endPointUri Your data-plane Url
         * @return RudderConfig.Builder
         */
        public Builder withEndPointUri(@NonNull String endPointUri) {
            if (TextUtils.isEmpty(endPointUri)) {
                RudderLogger.logError("endPointUri can not be null or empty. Set to default");
                return this;
            }
            if (!URLUtil.isValidUrl(endPointUri)) {
                RudderLogger.logError("Malformed endPointUri. Set to default");
                return this;
            }
            this.endPointUri = endPointUri;
            return this;
        }

        private int flushQueueSize = Constants.FLUSH_QUEUE_SIZE;

        /**
         * @param flushQueueSize No. of events you want to send in a batch (min = 1, max = 100)
         * @return RudderConfig.Builder
         */
        public Builder withFlushQueueSize(int flushQueueSize) {
            if (flushQueueSize < 1 || flushQueueSize > 100) {
                RudderLogger.logError("flushQueueSize is out of range. Min: 1, Max: 100. Set to default");
                return this;
            }
            this.flushQueueSize = flushQueueSize;
            return this;
        }

        private boolean isDebug = false;

        /**
         * @param isDebug Set it true to initialize SDK in debug mode
         * @return RudderConfig.Builder
         * @deprecated Use withLogLevel(int logLevel) instead
         */
        @Deprecated
        public Builder withDebug(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        private int logLevel = RudderLogger.RudderLogLevel.NONE;

        /**
         * @param logLevel Determine how much log you want to generate.
         *                 Use RudderLogger.RudderLogLevel.NONE for production
         * @return RudderConfig.Builder
         */
        public Builder withLogLevel(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        private int dbThresholdCount = Constants.DB_COUNT_THRESHOLD;

        /**
         * @param dbThresholdCount No of events to be persisted in DB
         * @return RudderConfig.Builder
         */
        public Builder withDbThresholdCount(int dbThresholdCount) {
            this.dbThresholdCount = dbThresholdCount;
            return this;
        }

        private int sleepTimeout = Constants.SLEEP_TIMEOUT;

        /**
         * @param sleepCount No of seconds to wait before sending any batch
         * @return RudderConfig.Builder
         */
        public Builder withSleepCount(int sleepCount) {
            this.sleepTimeout = sleepCount;
            return this;
        }

        private int configRefreshInterval = Constants.CONFIG_REFRESH_INTERVAL;

        /**
         * @param configRefreshInterval How often you want to fetch the config from the server.
         *                              Min : 1 hr
         *                              Max : 24 hrs
         * @return RudderConfig.Builder
         */
        public Builder withConfigRefreshInterval(int configRefreshInterval) {
            this.configRefreshInterval = configRefreshInterval;
            return this;
        }

        private boolean recordScreenViews = Constants.RECORD_SCREEN_VIEWS;

        /**
         * @param shouldRecordScreenViews Whether we should record screen views automatically
         * @return RudderConfig.Builder
         */
        public Builder withRecordScreenViews(boolean shouldRecordScreenViews) {
            this.recordScreenViews = shouldRecordScreenViews;
            return this;
        }

        private boolean trackLifecycleEvents = Constants.TRACK_LIFECYCLE_EVENTS;

        /**
         * @param shouldTrackLifecycleEvents Whether we should track Application lifecycle events automatically
         *                                   "Application Installed" and "Application Updated" will always be tracked
         * @return RudderConfig.Builder
         */
        public Builder withTrackLifecycleEvents(boolean shouldTrackLifecycleEvents) {
            this.trackLifecycleEvents = shouldTrackLifecycleEvents;
            return  this;
        }

        /**
         * Finalize your config building
         *
         * @return RudderConfig
         */
        public RudderConfig build() {
            return new RudderConfig(
                    this.endPointUri,
                    this.flushQueueSize,
                    this.dbThresholdCount,
                    this.sleepTimeout,
                    this.isDebug ? RudderLogger.RudderLogLevel.DEBUG : logLevel,
                    this.configRefreshInterval,
                    this.trackLifecycleEvents,
                    this.recordScreenViews,
                    this.factories);
        }
    }
}
