/*
 * Creator: Debanjan Chatterjee on 02/12/23, 5:00 pm Last modified: 02/12/23, 5:00 pm
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

package com.rudderstack.core.compat;

import static com.rudderstack.core.Configuration.FLUSH_QUEUE_SIZE;
import static com.rudderstack.core.Configuration.MAX_FLUSH_INTERVAL;

import com.rudderstack.core.Base64Generator;
import com.rudderstack.core.Configuration;
import com.rudderstack.core.Logger;
import com.rudderstack.core.RetryStrategy;
import com.rudderstack.core.RudderOptions;
import com.rudderstack.core.RudderUtils;
import com.rudderstack.core.internal.KotlinLogger;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigurationBuilder {
    private JsonAdapter jsonAdapter;
    private RudderOptions options = RudderOptions.defaultOptions();
    private int flushQueueSize = FLUSH_QUEUE_SIZE;
    private long maxFlushInterval = MAX_FLUSH_INTERVAL;
    private boolean shouldVerifySdk = false;
    private boolean gzipEnabled = true;
    private RetryStrategy sdkVerifyRetryStrategy = RetryStrategy.exponential();
    private String dataPlaneUrl = null; //defaults to https://hosted.rudderlabs.com
    private String controlPlaneUrl = null; //defaults to https://api.rudderlabs.com
    private Logger logger = new KotlinLogger(Logger.LogLevel.NONE);
    private ExecutorService analyticsExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService networkExecutor = Executors.newCachedThreadPool();
    private Base64Generator base64Generator = RudderUtils.INSTANCE.getDefaultBase64Generator();

    public ConfigurationBuilder(JsonAdapter jsonAdapter) {
        this.jsonAdapter = jsonAdapter;
    }

    public ConfigurationBuilder withOptions(RudderOptions options) {
        this.options = options;
        return this;
    }
    //builder for Configuration

    public ConfigurationBuilder withFlushQueueSize(int flushQueueSize) {
        this.flushQueueSize = flushQueueSize;
        return this;
    }

    public ConfigurationBuilder withMaxFlushInterval(long maxFlushInterval) {
        this.maxFlushInterval = maxFlushInterval;
        return this;
    }

    public ConfigurationBuilder withShouldVerifySdk(boolean shouldVerifySdk) {
        this.shouldVerifySdk = shouldVerifySdk;
        return this;
    }

    public ConfigurationBuilder withGzipEnabled(boolean gzipEnabled) {
        this.gzipEnabled = gzipEnabled;
        return this;
    }

    public ConfigurationBuilder withSdkVerifyRetryStrategy(RetryStrategy sdkVerifyRetryStrategy) {
        this.sdkVerifyRetryStrategy = sdkVerifyRetryStrategy;
        return this;
    }

    public ConfigurationBuilder withDataPlaneUrl(String dataPlaneUrl) {
        this.dataPlaneUrl = dataPlaneUrl;
        return this;
    }

    public ConfigurationBuilder withControlPlaneUrl(String controlPlaneUrl) {
        this.controlPlaneUrl = controlPlaneUrl;
        return this;
    }

    public ConfigurationBuilder withLogLevel(Logger.LogLevel logLevel) {
        this.logger = new KotlinLogger(logLevel);
        return this;
    }

    public ConfigurationBuilder withAnalyticsExecutor(ExecutorService analyticsExecutor) {
        this.analyticsExecutor = analyticsExecutor;
        return this;
    }

    public ConfigurationBuilder withNetworkExecutor(ExecutorService networkExecutor) {
        this.networkExecutor = networkExecutor;
        return this;
    }

    public ConfigurationBuilder withBase64Generator(Base64Generator base64Generator) {
        this.base64Generator = base64Generator;
        return this;
    }

    public Configuration build() {
        return Configuration.Companion.invoke(jsonAdapter, options, flushQueueSize, maxFlushInterval,
                shouldVerifySdk, gzipEnabled, sdkVerifyRetryStrategy, dataPlaneUrl,
                controlPlaneUrl, logger,
                analyticsExecutor, networkExecutor, base64Generator);
    }
}
