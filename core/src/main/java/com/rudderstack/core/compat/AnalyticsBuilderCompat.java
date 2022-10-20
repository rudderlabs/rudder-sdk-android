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

import static com.rudderstack.core.Analytics.CONTROL_PLANE_URL;
import static com.rudderstack.core.Analytics.DATA_PLANE_URL;

import com.rudderstack.core.Analytics;
import com.rudderstack.core.Base64Generator;
import com.rudderstack.core.BasicStorageImpl;
import com.rudderstack.core.ConfigDownloadService;
import com.rudderstack.core.DataUploadService;
import com.rudderstack.core.Logger;
import com.rudderstack.core.RetryStrategy;
import com.rudderstack.core.Settings;
import com.rudderstack.core.Storage;
import com.rudderstack.core.internal.ConfigDownloadServiceImpl;
import com.rudderstack.core.internal.DataUploadServiceImpl;
import com.rudderstack.core.internal.KotlinLogger;
import com.rudderstack.core.internal.states.SettingsState;
import com.rudderstack.models.Message;
import com.rudderstack.models.MessageUtils;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;

public final class AnalyticsBuilderCompat {
    private final String writeKey;

    private final Settings settings;

    private final JsonAdapter jsonAdapter;
    private String dataPlaneUrl = null;
    private String controlPlaneUrl = null;
    private ExecutorService analyticsExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private Base64Generator base64Generator = string -> Base64.getEncoder().encodeToString(
            String.format(Locale.US, "%s:", string).getBytes(StandardCharsets.UTF_8)
    );
    private Storage storage = null;

    private boolean shouldVerifySdk = true;
    private RetryStrategy sdkVerifyRetryStrategy = RetryStrategy.exponential(5);
    private DataUploadService dataUploadService = null;
    private ConfigDownloadService configDownloadService = null;
    private Logger logger = KotlinLogger.INSTANCE;

    private Map<String, Object> defaultTraits = null;
    private List<Map<String, String>> defaultExternalIds = null;
    private Map<String, Object> defaultContextMap = null;
    private Map<String, Object> contextAddons = null;


    private Function0<Unit> shutdownHook = null;
    private Function2<Boolean, String, Unit> initializationListener;

    public AnalyticsBuilderCompat(String writeKey, Settings settings, JsonAdapter jsonAdapter) {
        this.writeKey = writeKey;
        this.settings = settings;
        this.jsonAdapter = jsonAdapter;
    }


    public AnalyticsBuilderCompat withDataPlaneUrl(String dataPlaneUrl) {
        this.dataPlaneUrl = dataPlaneUrl;
        return this;
    }

    public AnalyticsBuilderCompat withControlPlaneUrl(String controlPlaneUrl) {
        this.controlPlaneUrl = controlPlaneUrl;
        return this;
    }


    public AnalyticsBuilderCompat withBase64Generator(Base64Generator base64Generator) {
        this.base64Generator = base64Generator;
        return this;
    }

    public AnalyticsBuilderCompat withSdkVerifyRetryStrategy(RetryStrategy sdkVerifyRetryStrategy) {
        this.sdkVerifyRetryStrategy = sdkVerifyRetryStrategy;
        return this;
    }

    public AnalyticsBuilderCompat withDataUploadService(DataUploadService dataUploadService) {
        this.dataUploadService = dataUploadService;
        return this;
    }

    public AnalyticsBuilderCompat withConfigDownloadService(ConfigDownloadService configDownloadService) {
        this.configDownloadService = configDownloadService;
        return this;
    }

    public AnalyticsBuilderCompat withAnalyticsExecutor(ExecutorService analyticsExecutor) {
        this.analyticsExecutor = analyticsExecutor;
        return this;
    }

    public AnalyticsBuilderCompat withNetworkExecutor(ExecutorService networkExecutor) {
        this.networkExecutor = networkExecutor;
        return this;
    }

    public AnalyticsBuilderCompat withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }


    public AnalyticsBuilderCompat withShutdownHook(ShutdownHook shutdownHook) {
        this.shutdownHook = () -> {
            shutdownHook.onShutdown();
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

    public AnalyticsBuilderCompat shouldVerifySdk(boolean shouldVerifySdk) {
        this.shouldVerifySdk = shouldVerifySdk;
        return this;
    }

    public AnalyticsBuilderCompat withStorage(Storage storage) {
        this.storage = storage;
        return this;
    }

    public AnalyticsBuilderCompat withDefaultTraits(Map<String, Object> defaultTraits) {
        this.defaultTraits = defaultTraits;
        return this;
    }

    public AnalyticsBuilderCompat withDefaultExternalIds(List<Map<String, String>> defaultExternalIds) {
        this.defaultExternalIds = defaultExternalIds;
        return this;
    }

    public AnalyticsBuilderCompat withDefaultContextMap(Map<String, Object> defaultContextMap) {
        this.defaultContextMap = defaultContextMap;
        return this;
    }

    public AnalyticsBuilderCompat withContextAddons(Map<String, Object> contextAddons) {
        this.contextAddons = contextAddons;
        return this;
    }

    public Analytics build() {
        Storage usableStorage = storage == null ? new BasicStorageImpl(new LinkedBlockingQueue<Message>(), logger) :
                storage;
        String usableDataPlaneUrl = dataPlaneUrl == null ?
                DATA_PLANE_URL : dataPlaneUrl;
        String usableControlPlaneUrl = controlPlaneUrl == null ? CONTROL_PLANE_URL :
                controlPlaneUrl;
        return new Analytics(writeKey, settings, jsonAdapter, shouldVerifySdk,
                sdkVerifyRetryStrategy, dataPlaneUrl, controlPlaneUrl, logger, usableStorage
                , analyticsExecutor, networkExecutor, base64Generator,
                dataUploadService == null ? new DataUploadServiceImpl(
                        writeKey, jsonAdapter, base64Generator, SettingsState.INSTANCE, usableDataPlaneUrl, networkExecutor
                ) : dataUploadService, configDownloadService == null ? new ConfigDownloadServiceImpl(
                base64Generator.generateBase64(writeKey), usableControlPlaneUrl, jsonAdapter, analyticsExecutor
        ) : configDownloadService, getDefaultTraits(defaultTraits, usableStorage),
                getDefaultExternalIds(defaultExternalIds, storage), defaultContextMap, contextAddons,
                initializationListener, shutdownHook
        );
    }

    private @Nullable
    Map<String, Object> getDefaultTraits(Map<String, Object> traitsSubmitted, Storage storage) {
        if (traitsSubmitted != null)
            return traitsSubmitted;
        if (storage != null && storage.getContext() != null)
            return MessageUtils.getTraits(storage.getContext());
        return null;
    }

    private @Nullable List<Map<String, String>> getDefaultExternalIds(List<Map<String, String>> defaultExternalIdsSubmitted, Storage storage) {
        if (defaultExternalIdsSubmitted != null)
            return defaultExternalIdsSubmitted;
        if (storage != null && storage.getContext() != null)
            return MessageUtils.getExternalIds(storage.getContext());
        return null;
    }

    @FunctionalInterface
    public interface ShutdownHook {
        void onShutdown();
    }

    @FunctionalInterface
    interface InitializationListener {
        void onInitialized(boolean success, String message);
    }
}
