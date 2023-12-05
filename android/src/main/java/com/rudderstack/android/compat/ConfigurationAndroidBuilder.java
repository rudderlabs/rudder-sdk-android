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
import com.rudderstack.android.storage.AndroidStorage;
import com.rudderstack.android.storage.AndroidStorageImpl;
import com.rudderstack.core.compat.ConfigurationBuilder;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

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
    private Boolean useContentProvider  = ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER;
    private String advertisingId = null;
    private String deviceToken = null;
    private AndroidStorage storage  = new AndroidStorageImpl();
    private ExecutorService advertisingIdFetchExecutor = null;
    public ConfigurationAndroidBuilder(Application application, JsonAdapter jsonAdapter) {
        super(jsonAdapter);
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
    public ConfigurationBuilder withUseContentProvider(Boolean useContentProvider) {
        this.useContentProvider = useContentProvider;
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
    public ConfigurationBuilder withStorage(AndroidStorage storage) {
        this.storage = storage;
        return this;
    }
    public ConfigurationBuilder withAdvertisingIdFetchExecutor(ExecutorService advertisingIdFetchExecutor) {
        this.advertisingIdFetchExecutor = advertisingIdFetchExecutor;
        return this;
    }
    @Override
    public ConfigurationAndroid build() {
        return new ConfigurationAndroid(super.build(),
                application,
                anonymousId,
                userId,
                trackLifecycleEvents,
                recordScreenViews,
                isPeriodicFlushEnabled,
                autoCollectAdvertId,
                multiProcessEnabled,
                defaultProcessName,
                useContentProvider,
                advertisingId,
                deviceToken,
                storage,
                advertisingIdFetchExecutor
                );
    }

}
