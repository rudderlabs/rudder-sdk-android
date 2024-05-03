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

import static com.rudderstack.android.ConfigurationAndroid.getProcessName;
import static com.rudderstack.android.ConfigurationAndroidKt.AUTO_COLLECT_ADVERT_ID;
import static com.rudderstack.android.ConfigurationAndroidKt.AUTO_SESSION_TRACKING;
import static com.rudderstack.android.ConfigurationAndroidKt.IS_PERIODIC_FLUSH_ENABLED;
import static com.rudderstack.android.ConfigurationAndroidKt.MULTI_PROCESS_ENABLED;
import static com.rudderstack.android.ConfigurationAndroidKt.RECORD_SCREEN_VIEWS;
import static com.rudderstack.android.ConfigurationAndroidKt.SESSION_TIMEOUT;
import static com.rudderstack.android.ConfigurationAndroidKt.TRACK_LIFECYCLE_EVENTS;

import android.app.Application;

import com.rudderstack.android.AndroidUtils;
import com.rudderstack.android.ConfigurationAndroid;
import com.rudderstack.core.Configuration;
import com.rudderstack.core.compat.ConfigurationBuilder;

import java.util.concurrent.ExecutorService;

//Java compatible Builder for [ConfigurationAndroid]
public class ConfigurationAndroidBuilder extends ConfigurationBuilder {
    private final Application application;
    private String anonymousId;
    private String userId = null;
    private Boolean trackLifecycleEvents = TRACK_LIFECYCLE_EVENTS;
    private Boolean recordScreenViews = RECORD_SCREEN_VIEWS;
    private Boolean isPeriodicFlushEnabled = IS_PERIODIC_FLUSH_ENABLED;
    private Boolean autoCollectAdvertId = AUTO_COLLECT_ADVERT_ID;
    private Boolean multiProcessEnabled = MULTI_PROCESS_ENABLED;
    private String defaultProcessName = getProcessName();
    private String advertisingId = null;
    private String deviceToken = null;
    private ExecutorService advertisingIdFetchExecutor = null;
    private boolean trackAutoSession = AUTO_SESSION_TRACKING;
    private long sessionTimeoutMillis = SESSION_TIMEOUT;

    public ConfigurationAndroidBuilder(Application application) {
        super();
        this.application = application;
        anonymousId = AndroidUtils.INSTANCE.getDeviceId(application);
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

    @Override
    public Configuration build() {
        return new ConfigurationAndroid(application,
                anonymousId,
                userId,
                advertisingId,
                deviceToken,
                advertisingIdFetchExecutor,
                trackLifecycleEvents,
                recordScreenViews,
                isPeriodicFlushEnabled,
                autoCollectAdvertId,
                multiProcessEnabled,
                defaultProcessName,
                trackAutoSession,
                sessionTimeoutMillis
        );
    }

}
