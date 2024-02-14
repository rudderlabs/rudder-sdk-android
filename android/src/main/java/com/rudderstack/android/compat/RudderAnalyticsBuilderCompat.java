/*
 * Creator Debanjan Chatterjee on 10/10/22, 528 PM Last modified 10/10/22, 528 PM
 * Copyright All rights reserved Ⓒ 2022 http//rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http//www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.compat;

import androidx.annotation.NonNull;

import com.rudderstack.android.ConfigurationAndroid;
import com.rudderstack.android.RudderAnalytics;
import com.rudderstack.core.Analytics;
import com.rudderstack.core.ConfigDownloadService;
import com.rudderstack.core.DataUploadService;

import kotlin.Unit;

/**
 * To be used by java projects
 */
public final class RudderAnalyticsBuilderCompat  {

    private @NonNull String writeKey;
    private @NonNull ConfigurationAndroid configuration;
    private DataUploadService dataUploadService = null;
    private ConfigDownloadService configDownloadService = null;
    private InitializationListener initializationListener = null;

    public RudderAnalyticsBuilderCompat(@NonNull String writeKey, @NonNull ConfigurationAndroid configuration) {
        this.writeKey = writeKey;
        this.configuration = configuration;
    }
    public RudderAnalyticsBuilderCompat withDataUploadService(DataUploadService dataUploadService) {
        this.dataUploadService = dataUploadService;
        return this;
    }
    public RudderAnalyticsBuilderCompat withConfigDownloadService(ConfigDownloadService configDownloadService) {
        this.configDownloadService = configDownloadService;
        return this;
    }
    public RudderAnalyticsBuilderCompat withInitializationListener(InitializationListener initializationListener) {
        this.initializationListener = initializationListener;
        return this;
    }
    public Analytics build() {

        return RudderAnalytics.RudderAnalytics(
                writeKey,
                configuration,
                dataUploadService,
                configDownloadService,
                (success, message) -> {
                    if(initializationListener != null) {
                        initializationListener.onInitialized(success, message);
                    }
                    return Unit.INSTANCE;
                }
        );
    }
    public interface InitializationListener {
        void onInitialized(boolean success, String message);
    }
}
