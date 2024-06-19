/*
 * Creator: Debanjan Chatterjee on 02/12/23, 5:57 pm Last modified: 02/12/23, 5:57 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.compat;

import android.app.Application;

import com.rudderstack.android.AndroidUtils;
import com.rudderstack.android.ConfigurationAndroid;
import com.rudderstack.android.internal.AndroidLogger;
import com.rudderstack.core.compat.ConfigurationBuilder;
import com.rudderstack.rudderjsonadapter.JsonAdapter;
import com.rudderstack.core.RudderLogger;

import java.util.concurrent.ExecutorService;

//Java compatible Builder for [ConfigurationAndroid]
public class ConfigurationAndroidBuilder extends ConfigurationBuilder {
    private final Application application ;
    private String anonymousId;
    private String userId = null;
    private Boolean trackLifecycleEvents = ConfigurationAndroid.Defaults.TRACK_LIFECYCLE_EVENTS;
    private Boolean recordScreenViews  = ConfigurationAndroid.Defaults.RECORD_SCREEN_VIEWS;
    private Boolean isPeriodicFlushEnabled  = ConfigurationAndroid.Defaults.IS_PERIODIC_FLUSH_ENABLED;
    private Boolean autoCollectAdvertId  = ConfigurationAndroid.Defaults.AUTO_COLLECT_ADVERT_ID;
    private Boolean multiProcessEnabled  = ConfigurationAndroid.Defaults.MULTI_PROCESS_ENABLED;
    private String defaultProcessName= ConfigurationAndroid.Defaults.INSTANCE.getDEFAULT_PROCESS_NAME();
    private String advertisingId = null;
    private String deviceToken = null;
    private boolean collectDeviceId = ConfigurationAndroid.Defaults.COLLECT_DEVICE_ID;
    private ExecutorService advertisingIdFetchExecutor = null;
    private boolean trackAutoSession = ConfigurationAndroid.Defaults.AUTO_SESSION_TRACKING;
    private long sessionTimeoutMillis = ConfigurationAndroid.Defaults.SESSION_TIMEOUT;
    private RudderLogger rudderLogger = new AndroidLogger();

    public ConfigurationAndroidBuilder(Application application, JsonAdapter jsonAdapter) {
        super(jsonAdapter);
        this.application = application;
        anonymousId = AndroidUtils.INSTANCE.generateAnonymousId(collectDeviceId, application);
    }
    public ConfigurationBuilder withAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
        return this;
    }
    public ConfigurationBuilder withUserId(String userId) {
        this.userId = userId;
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

    public ConfigurationBuilder withLogLevel(RudderLogger.LogLevel logLevel) {
        this.rudderLogger = new AndroidLogger(logLevel);
        return this;
    }

    public ConfigurationBuilder withCollectDeviceId(boolean collectDeviceId) {
        this.collectDeviceId = collectDeviceId;
        return this;
    }

    @Override
    public ConfigurationAndroid build() {
        return ConfigurationAndroid.Companion.invoke(super.build(),
                application,
                anonymousId,
                userId,
                trackLifecycleEvents,
                recordScreenViews,
                isPeriodicFlushEnabled,
                autoCollectAdvertId,
                multiProcessEnabled,
                defaultProcessName,
                advertisingId,
                deviceToken,
                rudderLogger,
                collectDeviceId,
                advertisingIdFetchExecutor,
                trackAutoSession,
                sessionTimeoutMillis
        );
    }

}
