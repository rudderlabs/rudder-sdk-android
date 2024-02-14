/*
 * Creator: Debanjan Chatterjee on 08/12/23, 8:01 pm Last modified: 08/12/23, 8:01 pm
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.rudderstack.core.Base64Generator;
import com.rudderstack.core.Configuration;
import com.rudderstack.core.Logger;
import com.rudderstack.core.RetryStrategy;
import com.rudderstack.core.RudderOptions;
import com.rudderstack.core.Storage;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigurationBuilderTest {

    @Test
    public void buildConfigurationWithDefaultValues() {
        JsonAdapter mockJsonAdapter = mock(JsonAdapter.class);
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(mockJsonAdapter);

        Configuration configuration = configurationBuilder.build();

        assertNotNull(configuration);
        assertEquals(mockJsonAdapter, configuration.getJsonAdapter());
        assertEquals(RudderOptions.defaultOptions(), configuration.getOptions());
        assertEquals(Configuration.FLUSH_QUEUE_SIZE, configuration.getFlushQueueSize());
        assertEquals(Configuration.MAX_FLUSH_INTERVAL, configuration.getMaxFlushInterval());
        assertFalse(configuration.isOptOut());
        assertFalse(configuration.getShouldVerifySdk());
        assertThat(configuration.getSdkVerifyRetryStrategy(),
                Matchers.isA(RetryStrategy.ExponentialRetryStrategy.class));
        assertThat(configuration.getDataPlaneUrl(), equalTo("https://hosted.rudderlabs.com"));
        assertNotNull(configuration.getLogger());
        assertNotNull(configuration.getAnalyticsExecutor());
        assertNotNull(configuration.getNetworkExecutor());
        assertNotNull(configuration.getBase64Generator());
    }

    @Test
    public void buildConfigurationWithCustomValues() {
        JsonAdapter mockJsonAdapter = mock(JsonAdapter.class);
        RudderOptions customOptions = new RudderOptions.Builder().build();
        int customFlushQueueSize = 100;
        long customMaxFlushInterval = 5000;
        boolean customOptOut = true;
        boolean customShouldVerifySdk = true;
        RetryStrategy customRetryStrategy = RetryStrategy.exponential();
        String customDataPlaneUrl = "https://custom-data-plane-url.com";
        String customControlPlaneUrl = "https://custom-control-plane-url.com";
        Logger customLogger = mock(Logger.class);
        Storage customStorage = mock(Storage.class);
        ExecutorService customAnalyticsExecutor = Executors.newFixedThreadPool(2);
        ExecutorService customNetworkExecutor = Executors.newFixedThreadPool(3);
        Base64Generator customBase64Generator = mock(Base64Generator.class);

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(mockJsonAdapter)
                .withOptions(customOptions)
                .withFlushQueueSize(customFlushQueueSize)
                .withMaxFlushInterval(customMaxFlushInterval)
                .withOptOut(customOptOut)
                .withShouldVerifySdk(customShouldVerifySdk)
                .withSdkVerifyRetryStrategy(customRetryStrategy)
                .withDataPlaneUrl(customDataPlaneUrl)
                .withControlPlaneUrl(customControlPlaneUrl)
                .withLogger(customLogger)
                .withAnalyticsExecutor(customAnalyticsExecutor)
                .withNetworkExecutor(customNetworkExecutor)
                .withBase64Generator(customBase64Generator);

        Configuration configuration = configurationBuilder.build();

        assertNotNull(configuration);
        assertEquals(mockJsonAdapter, configuration.getJsonAdapter());
        assertEquals(customOptions, configuration.getOptions());
        assertEquals(customFlushQueueSize, configuration.getFlushQueueSize());
        assertEquals(customMaxFlushInterval, configuration.getMaxFlushInterval());
        assertEquals(customOptOut, configuration.isOptOut());
        assertEquals(customShouldVerifySdk, configuration.getShouldVerifySdk());
        assertEquals(customRetryStrategy, configuration.getSdkVerifyRetryStrategy());
        assertEquals(customDataPlaneUrl, configuration.getDataPlaneUrl());
        assertEquals(customControlPlaneUrl, configuration.getControlPlaneUrl());
        assertEquals(customLogger, configuration.getLogger());
        assertEquals(customAnalyticsExecutor, configuration.getAnalyticsExecutor());
        assertEquals(customNetworkExecutor, configuration.getNetworkExecutor());
        assertEquals(customBase64Generator, configuration.getBase64Generator());
    }

    // Add more test cases as needed for edge cases, validation, etc.
}
