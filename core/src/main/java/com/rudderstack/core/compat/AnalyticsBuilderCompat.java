/*
 * Creator: Debanjan Chatterjee on 13/10/22, 12:29 PM Last modified: 13/10/22, 12:29 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

package com.rudderstack.core.compat;

import com.rudderstack.core.Analytics;
import com.rudderstack.core.BasicStorageImpl;
import com.rudderstack.core.ConfigDownloadService;
import com.rudderstack.core.Configuration;
import com.rudderstack.core.DataUploadService;
import com.rudderstack.core.Storage;
import com.rudderstack.core.internal.ConfigDownloadServiceImpl;
import com.rudderstack.core.internal.DataUploadServiceImpl;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class AnalyticsBuilderCompat {
    private final String writeKey;
    private final JsonAdapter jsonAdapter;

    private Configuration configuration = Configuration.getDEFAULT();
    private Storage storage = new BasicStorageImpl();
    private DataUploadService dataUploadService = null;
    private ConfigDownloadService configDownloadService = null;

    private Function1<Analytics, Unit> shutdownHook = null;
    private Function2<Boolean, String, Unit> initializationListener;

    public AnalyticsBuilderCompat(String writeKey, JsonAdapter jsonAdapter) {
        this.writeKey = writeKey;
        this.jsonAdapter = jsonAdapter;
    }

    public AnalyticsBuilderCompat withDataUploadService(DataUploadService dataUploadService) {
        this.dataUploadService = dataUploadService;
        return this;
    }

    public AnalyticsBuilderCompat withConfigDownloadService(ConfigDownloadService configDownloadService) {
        this.configDownloadService = configDownloadService;
        return this;
    }


    public AnalyticsBuilderCompat withShutdownHook(ShutdownHook shutdownHook) {
        this.shutdownHook = (analytics) -> {
            shutdownHook.onShutdown(analytics);
            return Unit.INSTANCE;
        };
        return this;
    }

    public AnalyticsBuilderCompat withInitializationListener(InitializationListener initializationListener) {
        this.initializationListener = (success, message) -> {
            initializationListener.onInitialized(success, message);
            return Unit.INSTANCE;
        };
        return this;
    }
    public AnalyticsBuilderCompat withStorage(Storage storage) {
        this.storage = storage;
        return this;
    }

    public Analytics build() {
        return new Analytics(writeKey, jsonAdapter, configuration,
                dataUploadService == null ? new DataUploadServiceImpl(
                        writeKey, jsonAdapter) : dataUploadService, configDownloadService == null ?
                new ConfigDownloadServiceImpl(writeKey, jsonAdapter) : configDownloadService,
                storage,
                initializationListener, shutdownHook
        );
    }

    @FunctionalInterface
    public interface ShutdownHook {
        void onShutdown(Analytics analytics);
    }

    @FunctionalInterface
    public interface InitializationListener {
        void onInitialized(boolean success, String message);
    }
}
