package com.rudderstack.android.compat;

import android.app.Application;

import com.rudderstack.android.AndroidUtils;
import com.rudderstack.android.ConfigurationAndroid;
import com.rudderstack.core.compat.ConfigurationBuilder;
import com.rudderstack.rudderjsonadapter.JsonAdapter;
import com.rudderstack.core.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Java compatible Builder for [ConfigurationAndroid]
public class ConfigurationAndroidBuilder extends ConfigurationBuilder {
    private final Application application ;
    private String anonymousId;
    private Boolean trackLifecycleEvents = ConfigurationAndroid.TRACK_LIFECYCLE_EVENTS;
    private Boolean recordScreenViews  = ConfigurationAndroid.RECORD_SCREEN_VIEWS;
    private Boolean isPeriodicFlushEnabled  = ConfigurationAndroid.IS_PERIODIC_FLUSH_ENABLED;
    private Boolean autoCollectAdvertId  = ConfigurationAndroid.AUTO_COLLECT_ADVERT_ID;
    private Boolean multiProcessEnabled  = ConfigurationAndroid.MULTI_PROCESS_ENABLED;
    private String defaultProcessName= ConfigurationAndroid.DEFAULT_PROCESS_NAME;
    private String advertisingId = null;
    private String deviceToken = null;
    private boolean collectDeviceId = ConfigurationAndroid.COLLECT_DEVICE_ID;
    private ExecutorService advertisingIdFetchExecutor = Executors.newCachedThreadPool();
    private boolean trackAutoSession = ConfigurationAndroid.AUTO_SESSION_TRACKING;
    private long sessionTimeoutMillis = ConfigurationAndroid.SESSION_TIMEOUT;
    private Logger.LogLevel logLevel = Logger.DEFAULT_LOG_LEVEL;

    public ConfigurationAndroidBuilder(Application application, JsonAdapter jsonAdapter) {
        super(jsonAdapter);
        this.application = application;
        anonymousId = AndroidUtils.INSTANCE.generateAnonymousId(collectDeviceId, application);
    }
    public ConfigurationBuilder withAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
        return this;
    }
    public ConfigurationBuilder withTrackLifecycleEvents(Boolean trackLifecycleEvents) {
        this.trackLifecycleEvents = trackLifecycleEvents;
        return this;
    }
    public ConfigurationBuilder withRecordScreenViews(Boolean recordScreenViews) {
        this.recordScreenViews = recordScreenViews;
        return this;
    }
    public ConfigurationBuilder withIsPeriodicFlushEnabled(Boolean isPeriodicFlushEnabled) {
        this.isPeriodicFlushEnabled = isPeriodicFlushEnabled;
        return this;
    }
    public ConfigurationBuilder withAutoCollectAdvertId(Boolean autoCollectAdvertId) {
        this.autoCollectAdvertId = autoCollectAdvertId;
        return this;
    }
    public ConfigurationBuilder withMultiProcessEnabled(Boolean multiProcessEnabled) {
        this.multiProcessEnabled = multiProcessEnabled;
        return this;
    }
    public ConfigurationBuilder withDefaultProcessName(String defaultProcessName) {
        this.defaultProcessName = defaultProcessName;
        return this;
    }
    public ConfigurationBuilder withAdvertisingId(String advertisingId) {
        this.advertisingId = advertisingId;
        return this;
    }
    public ConfigurationBuilder withDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }
    public ConfigurationBuilder withAdvertisingIdFetchExecutor(ExecutorService advertisingIdFetchExecutor) {
        this.advertisingIdFetchExecutor = advertisingIdFetchExecutor;
        return this;
    }
    public ConfigurationBuilder withTrackAutoSession(boolean trackAutoSession) {
        this.trackAutoSession = trackAutoSession;
        return this;
    }
    public ConfigurationBuilder withSessionTimeoutMillis(long sessionTimeoutMillis) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
        return this;
    }

    public ConfigurationBuilder withLogLevel(Logger.LogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public ConfigurationBuilder withCollectDeviceId(boolean collectDeviceId) {
        this.collectDeviceId = collectDeviceId;
        return this;
    }

    @Override
    public ConfigurationAndroid build() {
        return new ConfigurationAndroid(
                application,
                anonymousId,
                trackLifecycleEvents,
                recordScreenViews,
                isPeriodicFlushEnabled,
                autoCollectAdvertId,
                multiProcessEnabled,
                defaultProcessName,
                advertisingId,
                deviceToken,
                logLevel,
                collectDeviceId,
                advertisingIdFetchExecutor,
                trackAutoSession,
                sessionTimeoutMillis,
                jsonAdapter
        );
    }

}
