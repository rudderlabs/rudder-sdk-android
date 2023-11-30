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

import android.app.Application;

import com.rudderstack.android.AnalyticsFactory;
import com.rudderstack.android.internal.AndroidLogger;
import com.rudderstack.core.Analytics;
import com.rudderstack.core.Logger;
import com.rudderstack.core.RetryStrategy;
import com.rudderstack.core.Configuration;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import java.util.List;
import java.util.Map;

import kotlin.Unit;

/**
 * To be used by java projects
 */
public class RudderAnalyticsBuilderCompat {
    private final Application application;
    private final String writeKey;
    private final Configuration configuration;
    private final JsonAdapter jsonAdapter;
    private String dataPlaneUrl = null;
    private boolean shouldVerifySdk = true;
    private String controlPlaneUrl = null;
    private boolean trackLifecycleEvents = false;
    private boolean recordScreenViews = false;
    private boolean isPeriodicFlushEnabled = false;
    private boolean autoCollectAdvertId = false;
    private boolean multiProcessEnabled = false;
    private String defaultProcessName = null;
    private boolean useContentProvider = multiProcessEnabled;
    private Map<String, Object> defaultTraits = null;
    private List<Map<String, String>> defaultExternalIds = null;
    private Map<String, Object> defaultContextMap = null;
    private InitializationListener initializationListener = null;
    private Logger logger = AndroidLogger.INSTANCE;
    private RetryStrategy sdkVerifyRetryStrategy = RetryStrategy.exponential(5);

    @FunctionalInterface
    interface InitializationListener {
        void onInitialized(boolean success, String message);
    }

    public RudderAnalyticsBuilderCompat(Application application, String writeKey,
                                        Configuration configuration,
                                        JsonAdapter jsonAdapter) {
        this.application = application;
        this.writeKey = writeKey;
        this.configuration = configuration;
        this.jsonAdapter = jsonAdapter;
    }

        public RudderAnalyticsBuilderCompat withDataPlaneUrl(String val) {
            dataPlaneUrl = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withShouldVerifySdk(boolean val) {
            shouldVerifySdk = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withControlPlaneUrl(String val) {
            controlPlaneUrl = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withTrackLifecycleEvents(boolean val) {
            trackLifecycleEvents = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withRecordScreenViews(boolean val) {
            recordScreenViews = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withIsPeriodicFlushEnabled(boolean val) {
            isPeriodicFlushEnabled = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withAutoCollectAdvertId(boolean val) {
            autoCollectAdvertId = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withMultiProcessEnabled(boolean val) {
            multiProcessEnabled = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withDefaultProcessName(String val) {
            defaultProcessName = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withUseContentProvider(boolean val) {
            useContentProvider = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withDefaultTraits(Map<String, Object> val) {
            defaultTraits = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withDefaultExternalIds(List<Map<String, String>> val) {
            defaultExternalIds = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withDefaultContextMap(Map<String, Object> val) {
            defaultContextMap = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withInitializationListener(InitializationListener val) {
            initializationListener = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withLogger(Logger val) {
            logger = val;
            return this;
        }

        public RudderAnalyticsBuilderCompat withSdkVerifyRetryStrategy(RetryStrategy val) {
            sdkVerifyRetryStrategy = val;
            return this;
        }

        public Analytics build() {
            return AnalyticsFactory.RudderAnalytics(application, writeKey,
                    configuration, jsonAdapter, dataPlaneUrl, shouldVerifySdk, controlPlaneUrl, trackLifecycleEvents,
                    recordScreenViews, isPeriodicFlushEnabled, autoCollectAdvertId, multiProcessEnabled, defaultProcessName,
                    useContentProvider, defaultTraits, defaultExternalIds,
                    defaultContextMap, logger, sdkVerifyRetryStrategy, (success, message) -> {
                        initializationListener.onInitialized(success, message);
                        return Unit.INSTANCE;
                    });
        }

}
