package com.rudderstack.android.sdk.core;

import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.consent.RudderConsentFilter;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
 * - controlPlaneUrl -> link to self-hosted sourceConfig
 * - autoSessionTracking -> whether we are tracking session automatically
 * - sessionDuration -> duration of a session in minute
 * default values are set at Constants file
 *
 * */
public class RudderConfig {
    @Nullable
    private String dataPlaneUrl;
    private int flushQueueSize;
    private int dbCountThreshold;
    private int sleepTimeOut;
    private int logLevel;
    private int configRefreshInterval;
    private boolean isPeriodicFlushEnabled;
    private long repeatInterval;
    private TimeUnit repeatIntervalTimeUnit;
    private boolean trackLifecycleEvents;
    private boolean autoCollectAdvertId;
    private boolean recordScreenViews;
    private boolean trackAutoSession;
    private boolean useNewLifeCycleEvents;
    private boolean trackDeepLinks;
    private boolean collectDeviceId;
    private long sessionTimeout;
    private String controlPlaneUrl;
    private List<RudderIntegration.Factory> factories;
    private List<RudderIntegration.Factory> customFactories;
    private RudderDataResidencyServer rudderDataResidencyServer;
    @Nullable
    private RudderConsentFilter consentFilter;
    private boolean isGzipEnabled = true;

    private DBEncryption dbEncryption = new DBEncryption(Constants.DEFAULT_DB_ENCRYPTION_ENABLED,
            null);
    private long eventDispatchSleepInterval;


    RudderConfig() {
        this(
                null,
                Constants.FLUSH_QUEUE_SIZE,
                Constants.DB_COUNT_THRESHOLD,
                Constants.SLEEP_TIMEOUT,
                RudderLogger.RudderLogLevel.ERROR,
                Constants.CONFIG_REFRESH_INTERVAL,
                Constants.PERIODIC_FLUSH_ENABLED,
                Constants.REPEAT_INTERVAL,
                Constants.REPEAT_INTERVAL_TIME_UNIT,
                Constants.TRACK_LIFECYCLE_EVENTS,
                Constants.NEW_LIFECYCLE_EVENTS,
                Constants.TRACK_DEEP_LINKS,
                Constants.AUTO_COLLECT_ADVERT_ID,
                Constants.RECORD_SCREEN_VIEWS,
                Constants.AUTO_SESSION_TRACKING,
                Constants.COLLECT_DEVICE_ID,
                Constants.DEFAULT_SESSION_TIMEOUT,
                Constants.CONTROL_PLANE_URL,
                null,
                null,
                Constants.DATA_RESIDENCY_SERVER,
                null,
                Constants.DEFAULT_GZIP_ENABLED,
                null,
                Constants.EVENT_DISPATCH_SLEEP_INTERVAL
        );
    }

    private RudderConfig(
            String dataPlaneUrl,
            int flushQueueSize,
            int dbCountThreshold,
            int sleepTimeOut,
            int logLevel,
            int configRefreshInterval,
            boolean isPeriodicFlushEnabled,
            long repeatInterval,
            TimeUnit repeatIntervalTimeUnit,
            boolean trackLifecycleEvents,
            boolean useNewLifeCycleEvents,
            boolean trackDeepLinks,
            boolean autoCollectAdvertId,
            boolean recordScreenViews,
            boolean trackAutoSession,
            boolean collectDeviceId,
            long sessionTimeout,
            String controlPlaneUrl,
            List<RudderIntegration.Factory> factories,
            List<RudderIntegration.Factory> customFactories,
            RudderDataResidencyServer rudderDataResidencyServer,
            @Nullable RudderConsentFilter consentFilter,
            boolean isGzipEnabled,
            @Nullable DBEncryption dbEncryption,
            long eventDispatchSleepInterval

    ) {
        RudderLogger.init(logLevel);

        if (!TextUtils.isEmpty(dataPlaneUrl) && URLUtil.isValidUrl(dataPlaneUrl)) {
            if (!dataPlaneUrl.endsWith("/")) dataPlaneUrl += "/";
            this.dataPlaneUrl = dataPlaneUrl;
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

        this.isPeriodicFlushEnabled = isPeriodicFlushEnabled;

        if (repeatIntervalTimeUnit == TimeUnit.MINUTES && repeatInterval < 15) {
            RudderLogger.logError("RudderConfig: the repeat Interval for Flushing Periodically should be atleast 15 minutes, falling back to default of 1 hour");
            this.repeatInterval = Constants.REPEAT_INTERVAL;
            this.repeatIntervalTimeUnit = Constants.REPEAT_INTERVAL_TIME_UNIT;
        } else {
            this.repeatInterval = repeatInterval;
            this.repeatIntervalTimeUnit = repeatIntervalTimeUnit;
        }

        this.trackLifecycleEvents = trackLifecycleEvents;
        this.useNewLifeCycleEvents = useNewLifeCycleEvents;
        this.trackDeepLinks = trackDeepLinks;
        this.autoCollectAdvertId = autoCollectAdvertId;
        this.recordScreenViews = recordScreenViews;

        if (factories != null && !factories.isEmpty()) {
            this.factories = factories;
        }

        if (customFactories != null && !customFactories.isEmpty()) {
            this.customFactories = customFactories;
        }

        if (TextUtils.isEmpty(controlPlaneUrl)) {
            RudderLogger.logError("configPlaneUrl can not be null or empty. Set to default.");
            this.controlPlaneUrl = Constants.CONTROL_PLANE_URL;
        } else if (!URLUtil.isValidUrl(controlPlaneUrl)) {
            RudderLogger.logError("Malformed configPlaneUrl. Set to default");
            this.controlPlaneUrl = Constants.CONTROL_PLANE_URL;
        } else {
            if (!controlPlaneUrl.endsWith("/")) controlPlaneUrl += "/";
            this.controlPlaneUrl = controlPlaneUrl;
        }

        if (sessionTimeout >= Constants.MIN_SESSION_TIMEOUT) {
            this.sessionTimeout = sessionTimeout;
        } else {
            this.sessionTimeout = Constants.DEFAULT_SESSION_TIMEOUT;
        }
        this.trackAutoSession = trackAutoSession;
        this.collectDeviceId = collectDeviceId;

        this.rudderDataResidencyServer = rudderDataResidencyServer;
        this.consentFilter = consentFilter;
        this.isGzipEnabled = isGzipEnabled;
        if (dbEncryption != null) {
            this.dbEncryption = dbEncryption;
        }

        if (eventDispatchSleepInterval <= this.sleepTimeOut) {
            this.eventDispatchSleepInterval = eventDispatchSleepInterval;
        } else {
            this.eventDispatchSleepInterval = Constants.EVENT_DISPATCH_SLEEP_INTERVAL;
        }
    }

    /**
     * @return endPointUrl (your data-plane url)
     * @deprecated use getDataPlaneUrl()
     */
    @NonNull
    public String getEndPointUri() {
        return dataPlaneUrl;
    }

    public @NonNull DBEncryption getDbEncryption() {
        return dbEncryption;
    }

    /**
     * @return dataPlaneUrl (your data-plane url)
     */
    @Nullable
    public String getDataPlaneUrl() {
        return dataPlaneUrl;
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
     * @return isPeriodicFlushEnabled if periodic flushing of events from db to server is enabled or not
     */
    public boolean isPeriodicFlushEnabled() {
        return isPeriodicFlushEnabled;
    }

    /**
     * @return repeatInterval the interval in which the SDK should flush away the events from db to server
     */
    public long getRepeatInterval() {
        return repeatInterval;
    }

    /**
     * @return repeatIntervalTimeUnit the time unit in which the flushing should be happening.
     */
    public TimeUnit getRepeatIntervalTimeUnit() {
        return repeatIntervalTimeUnit;
    }

    /**
     * @return trackLifecycleEvents (whether we are tracking the Application lifecycle events except
     * "Application Installed" and "Application Updated"
     */
    public boolean isTrackLifecycleEvents() {
        return trackLifecycleEvents;
    }


    /**
     * @return useNewLifeCycleEvents (whether we are using the new lifecycle events)
     */
    public boolean isNewLifeCycleEvents() {
        return useNewLifeCycleEvents;
    }


    /**
     * @return trackDeepLinks (whether we are tracking the deep link events or not
     */
    public boolean isTrackDeepLinks() {
        return trackDeepLinks;
    }

    /**
     * @return autoCollectAdvertId (whether we are automatically collecting the advertisingId if the
     * com.google.android.gms.ads.identifier.AdvertisingIdClient is found on the classpath.
     */
    public boolean isAutoCollectAdvertId() {
        return autoCollectAdvertId;
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

    /**
     * @return customFactories (list of custom factories integrated in the application)
     */
    @Nullable
    public List<RudderIntegration.Factory> getCustomFactories() {
        return customFactories;
    }

    public boolean isGzipEnabled() {
        return isGzipEnabled;
    }

    /**
     * @return configPlaneUrl (Link to your hosted version of source-config)
     * @deprecated use getControlPlaneUrl()
     */
    public String getConfigPlaneUrl() {
        return controlPlaneUrl;
    }

    /**
     * @return controlPlaneUrl (Link to your hosted version of source-config)
     */
    public String getControlPlaneUrl() {
        return controlPlaneUrl;
    }

    /**
     * @return trackAutoSession (whether we are tracking session automatically)
     */
    public boolean isTrackAutoSession() {
        return trackAutoSession;
    }

    /**
     * @return isCollectDeviceId (whether we want to collect the device ID)
     */
    public boolean isCollectDeviceId() {
        return collectDeviceId;
    }

    /**
     * @return sessionDuration (duration of a session in minute)
     */
    public long getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * Retrieves the event dispatch sleep interval in milliseconds.
     * Converts the stored interval value (in seconds) to milliseconds
     * and returns it.
     *
     * @return the event dispatch sleep interval in milliseconds
     */
    public long getEventDispatchSleepInterval() {
        return eventDispatchSleepInterval * 1000;
    }

    @Nullable
    public RudderConsentFilter getConsentFilter() {
        return consentFilter;
    }

    /**
     * @return dataResidencyServer (your data residency server url)
     */
    public RudderDataResidencyServer getDataResidencyServer() {
        return rudderDataResidencyServer;
    }

    void setDataPlaneUrl(String dataPlaneUrl) {
        this.dataPlaneUrl = dataPlaneUrl;
    }

    void setControlPlaneUrl(String controlPlaneUrl) {
        this.controlPlaneUrl = controlPlaneUrl;
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

    void setNewLifeCycleEvents(boolean useNewLifeCycleEvents) {
        this.useNewLifeCycleEvents = useNewLifeCycleEvents;
    }

    void setTrackDeepLinks(boolean trackDeepLinks) {
        this.trackDeepLinks = trackDeepLinks;
    }

    void setRecordScreenViews(boolean recordScreenViews) {
        this.recordScreenViews = recordScreenViews;
    }

    void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    void setTrackAutoSession(boolean trackAutoSession) {
        this.trackAutoSession = trackAutoSession;
    }

    void setDataResidencyServer(RudderDataResidencyServer rudderDataResidencyServer) {
        this.rudderDataResidencyServer = rudderDataResidencyServer;
    }

    void setEventDispatchSleepInterval(long eventDispatchSleepInterval) {
        this.eventDispatchSleepInterval = eventDispatchSleepInterval;
    }

    /**
     * @return custom toString implementation for RudderConfig
     */
    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.US, "RudderConfig: endPointUrl:%s | flushQueueSize: %d | dbCountThreshold: %d | sleepTimeOut: %d | logLevel: %d", dataPlaneUrl, flushQueueSize, dbCountThreshold, sleepTimeOut, logLevel);
    }


    /**
     * Builder class for RudderConfig
     */
    public static class Builder {
        private List<RudderIntegration.Factory> factories = new ArrayList<>();
        private List<RudderIntegration.Factory> customFactories = new ArrayList<>();
        private @Nullable RudderConsentFilter consentFilter = null;
        private @Nullable String dataPlaneUrl = null;
        private boolean isGzipEnabled = Constants.DEFAULT_GZIP_ENABLED;
        private @Nullable DBEncryption dbEncryption = null;
        private long eventDispatchSleepInterval = Constants.EVENT_DISPATCH_SLEEP_INTERVAL;

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

        /**
         * @param customFactory : Instance of RudderIntegration.Factory (for more information visit https://docs.rudderstack.com)
         * @return RudderConfig.Builder
         */
        public Builder withCustomFactory(@NonNull RudderIntegration.Factory customFactory) {
            this.customFactories.add(customFactory);
            return this;
        }

        /**
         * @param customFactories List of instances of RudderIntegration.Factory
         * @return RudderConfig.Builder
         */
        public Builder withCustomFactories(@NonNull List<RudderIntegration.Factory> customFactories) {
            this.customFactories.addAll(customFactories);
            return this;
        }

        /**
         * @param customFactories List of instances of RudderIntegration.Factory
         * @return RudderConfig.Builder
         */
        public Builder withCustomFactories(@NonNull RudderIntegration.Factory... customFactories) {
            Collections.addAll(this.customFactories, customFactories);
            return this;
        }

        /**
         * @param endPointUri Your data-plane Url
         * @return RudderConfig.Builder
         * @deprecated use withDataPlaneUrl(String dataPlaneUrl)
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
            this.dataPlaneUrl = endPointUri;
            return this;
        }

        /**
         * @param dataPlaneUrl Your data-plane Url
         * @return RudderConfig.Builder
         */
        public Builder withDataPlaneUrl(@NonNull String dataPlaneUrl) {
            if (TextUtils.isEmpty(dataPlaneUrl)) {
                RudderLogger.logError("endPointUri can not be null or empty.");
                return this;
            }
            if (!URLUtil.isValidUrl(dataPlaneUrl)) {
                RudderLogger.logError("Malformed endPointUri.");
                return this;
            }
            this.dataPlaneUrl = dataPlaneUrl;
            return this;
        }

        private RudderDataResidencyServer rudderDataResidencyServer = Constants.DATA_RESIDENCY_SERVER;

        /**
         * @param rudderDataResidencyServer Your dataResidencyServer url
         * @return RudderConfig.Builder
         */
        public Builder withDataResidencyServer(@NonNull RudderDataResidencyServer rudderDataResidencyServer) {
            this.rudderDataResidencyServer = rudderDataResidencyServer;
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

        /**
         * Enable/Disable Gzip.
         * Gzip is enabled by default
         *
         * @param isGzip true to enable and vice-versa
         * @return RudderConfig.Builder
         */
        public Builder withGzip(boolean isGzip) {
            this.isGzipEnabled = isGzip;
            return this;
        }

        public Builder withDbEncryption(DBEncryption dbEncryption) {
            this.dbEncryption = dbEncryption;
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

        private boolean isPeriodicFlushEnabled = Constants.PERIODIC_FLUSH_ENABLED;
        private long repeatInterval = Constants.REPEAT_INTERVAL;
        private TimeUnit repeatIntervalTimeUnit = Constants.REPEAT_INTERVAL_TIME_UNIT;

        /**
         * @param repeatInterval         the interval in which we should flush away the events in the db periodically
         * @param repeatIntervalTimeUnit the TimeUnit in which the repeatInterval is specified. It can be either minutes / hours.
         * @return RudderConfig.Builder
         */

        public Builder withFlushPeriodically(long repeatInterval, TimeUnit repeatIntervalTimeUnit) {
            this.isPeriodicFlushEnabled = true;
            if (repeatIntervalTimeUnit == TimeUnit.MINUTES && repeatInterval < 15) {
                RudderLogger.logError("RudderConfig: Builder: withFlushPeriodically: the repeat Interval for Flushing Periodically should be atleast 15 minutes, falling back to default of 1 hour");
                return this;
            }
            this.repeatInterval = repeatInterval;
            this.repeatIntervalTimeUnit = repeatIntervalTimeUnit;
            return this;
        }

        public Builder withConsentFilter(@NonNull RudderConsentFilter consentFilter) {
            this.consentFilter = consentFilter;
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
            return this;
        }

        private boolean useNewLifecycleEvents = Constants.NEW_LIFECYCLE_EVENTS;

        /**
         * @param shouldUseNewLifecycleEvents Whether we should use new lifecycle events
         * @return RudderConfig.Builder
         */

        public Builder withNewLifecycleEvents(boolean shouldUseNewLifecycleEvents) {
            this.useNewLifecycleEvents = shouldUseNewLifecycleEvents;
            return this;
        }

        private boolean trackDeepLinks = Constants.TRACK_DEEP_LINKS;

        /**
         * @param shouldTrackDeepLinks whether the sdk should track any deep links or not
         * @return
         */
        public Builder withTrackDeepLinks(boolean shouldTrackDeepLinks) {
            this.trackDeepLinks = shouldTrackDeepLinks;
            return this;
        }

        private boolean autoCollectAdvertId = Constants.AUTO_COLLECT_ADVERT_ID;

        /**
         * @param shouldAutoCollectAdvertId (whether we should automatically collecting the advertisingId if the
         *                                  com.google.android.gms.ads.identifier.AdvertisingIdClient is found on the classpath.
         * @return RudderConfig.Builder
         */
        public Builder withAutoCollectAdvertId(boolean shouldAutoCollectAdvertId) {
            this.autoCollectAdvertId = shouldAutoCollectAdvertId;
            return this;
        }

        private String controlPlaneUrl = Constants.CONTROL_PLANE_URL;

        /**
         * @param configPlaneUrl Your hosted version of sourceConfig
         * @return RudderConfig.Builder
         * @deprecated use withControlPlaneUrl(String controlPlaneUrl)
         */
        public Builder withConfigPlaneUrl(String configPlaneUrl) {
            this.controlPlaneUrl = configPlaneUrl;
            return this;
        }

        /**
         * @param controlPlaneUrl Your hosted version of sourceConfig
         * @return RudderConfig.Builder
         */
        public Builder withControlPlaneUrl(String controlPlaneUrl) {
            this.controlPlaneUrl = controlPlaneUrl;
            return this;
        }

        private long sessionTimeout = Constants.DEFAULT_SESSION_TIMEOUT;

        /**
         * @param sessionTimeout (duration of inactivity of session in milliseconds)
         * @return RudderConfig.Builder
         */
        public Builder withSessionTimeoutMillis(long sessionTimeout) {
            if (sessionTimeout < Constants.MIN_SESSION_TIMEOUT) {
                RudderLogger.logError(String.format("Minimum sessionTimeout is %s millisecond.", Constants.MIN_SESSION_TIMEOUT));
                return this;
            }
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        private boolean autoSessionTracking = Constants.AUTO_SESSION_TRACKING;

        /**
         * @param autoSessionTracking (whether we are tracking session automatically)
         * @return RudderConfig.Builder
         */
        public Builder withAutoSessionTracking(boolean autoSessionTracking) {
            this.autoSessionTracking = autoSessionTracking;
            return this;
        }

        private boolean collectDeviceId = Constants.COLLECT_DEVICE_ID;

        /**
         * @param collectDeviceId (whether we are collecting the device ID)
         * @return RudderConfig.Builder
         */
        public Builder withCollectDeviceId(boolean collectDeviceId) {
            this.collectDeviceId = collectDeviceId;
            return this;
        }

        /**
         * Sets the sleep interval for the event dispatch thread.
         * This interval (in seconds) determines how long the thread
         * will sleep when there are no events to send to the server.
         * The interval must be less than or equal to the value set by withSleepCount(int sleepCount).
         *
         * @param eventDispatchSleepInterval the sleep interval in seconds
         * @return the updated Builder instance for chaining
         */
        public Builder withEventDispatchSleepInterval(int eventDispatchSleepInterval) {
            this.eventDispatchSleepInterval = eventDispatchSleepInterval;
            return this;
        }

        /**
         * Finalize your config building
         *
         * @return RudderConfig
         */
        public RudderConfig build() {
            return new RudderConfig(
                    this.dataPlaneUrl,
                    this.flushQueueSize,
                    this.dbThresholdCount,
                    this.sleepTimeout,
                    this.isDebug ? RudderLogger.RudderLogLevel.DEBUG : logLevel,
                    this.configRefreshInterval,
                    this.isPeriodicFlushEnabled,
                    this.repeatInterval,
                    this.repeatIntervalTimeUnit,
                    this.trackLifecycleEvents,
                    this.useNewLifecycleEvents,
                    this.trackDeepLinks,
                    this.autoCollectAdvertId,
                    this.recordScreenViews,
                    this.autoSessionTracking,
                    this.collectDeviceId,
                    this.sessionTimeout,
                    this.controlPlaneUrl,
                    this.factories,
                    this.customFactories,
                    this.rudderDataResidencyServer,
                    consentFilter,
                    this.isGzipEnabled,
                    this.dbEncryption,
                    this.eventDispatchSleepInterval
            );
        }
    }

    public static class DBEncryption {
        public final boolean enable;
        public final @Nullable String key;
        /**
         * Not documented. This is to set the custom persistence provider factory.
         * The SDK creates an instance of this factory and uses it to get the PersistenceProvider.
         * This is experimental and not encouraged to be used by external users
         */
        private @Nullable String persistenceProviderFactoryClassName = null;

        public DBEncryption(boolean enable, @Nullable String key) {
            this(enable, key, null);
        }

        public DBEncryption(boolean enable, @Nullable String key,
                            @Nullable String persistenceProviderFactoryClassName) {
            this.enable = enable;
            this.key = key;
            this.persistenceProviderFactoryClassName = persistenceProviderFactoryClassName;
        }

        public void setPersistenceProviderFactoryClassName(String persistenceProviderFactoryClassName) {
            this.persistenceProviderFactoryClassName = persistenceProviderFactoryClassName;
        }

        @Nullable
        String getPersistenceProviderFactoryClassName() {
            return persistenceProviderFactoryClassName;
        }
    }
}
