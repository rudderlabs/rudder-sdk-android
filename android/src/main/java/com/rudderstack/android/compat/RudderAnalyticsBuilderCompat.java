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
import com.rudderstack.android.storage.AndroidStorage;
import com.rudderstack.android.storage.AndroidStorageImpl;
import com.rudderstack.core.Analytics;
import com.rudderstack.core.ConfigDownloadService;
import com.rudderstack.core.DataUploadService;

import java.util.concurrent.Executors;

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
    private AndroidStorage storage = new AndroidStorageImpl(configuration.getApplication(),
            ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER,
            writeKey,
            Executors.newSingleThreadExecutor());

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

        return RudderAnalytics.getInstance(
                writeKey,
                configuration,
                storage,
                dataUploadService,
                configDownloadService,
                (success, message) -> {
                    if (initializationListener != null) {
                        initializationListener.onInitialized(success, message);
                    }
                    return Unit.INSTANCE;
                }
        );
    }

    public interface InitializationListener {
        void onInitialized(boolean success, String message);
    }

    public RudderAnalyticsBuilderCompat withStorage(AndroidStorage storage) {
        this.storage = storage;
        return this;
    }

}
