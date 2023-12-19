/*
 * Creator: Debanjan Chatterjee on 08/12/23, 6:59 pm Last modified: 08/12/23, 6:59 pm
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

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.rudderstack.core.Analytics;
import com.rudderstack.core.ConfigDownloadService;
import com.rudderstack.core.DataUploadService;
import com.rudderstack.jacksonrudderadapter.JacksonAdapter;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import org.junit.Test;

public class AnalyticsBuilderCompatTest {
    private JsonAdapter jsonAdapter = new JacksonAdapter();
    @Test
    public void constructorInitialization() {
        AnalyticsBuilderCompat analyticsBuilder = new AnalyticsBuilderCompat("writeKey",
                new ConfigurationBuilder(jsonAdapter).build());
        Analytics analytics = analyticsBuilder.build();
        assertNotNull(analytics.getCurrentConfiguration());
        assertNotNull(analytics.getDataUploadService());
        assertNotNull(analytics.getConfigDownloadService());
    }

    @Test
    public void withDataUploadService() {
        DataUploadService mockDataUploadService = mock(DataUploadService.class);
        AnalyticsBuilderCompat analyticsBuilder = new AnalyticsBuilderCompat("writeKey",
                new ConfigurationBuilder(jsonAdapter).build())
                .withDataUploadService(mockDataUploadService);

        assertEquals(mockDataUploadService, analyticsBuilder.build().getDataUploadService());
    }

    @Test
    public void buildWithCustomImplementations() {
        DataUploadService mockDataUploadService = mock(DataUploadService.class);
        ConfigDownloadService mockConfigDownloadService = mock(ConfigDownloadService.class);
        AnalyticsBuilderCompat.ShutdownHook mockShutdownHook = mock(AnalyticsBuilderCompat.ShutdownHook.class);
        AnalyticsBuilderCompat.InitializationListener mockInitializationListener =
                mock(AnalyticsBuilderCompat.InitializationListener.class);

        AnalyticsBuilderCompat analyticsBuilder = new AnalyticsBuilderCompat("writeKey",
                new ConfigurationBuilder(jsonAdapter).build())
                .withDataUploadService(mockDataUploadService)
                .withConfigDownloadService(mockConfigDownloadService)
                .withShutdownHook(mockShutdownHook)
                .withInitializationListener(mockInitializationListener);

        Analytics analytics = analyticsBuilder.build();

        assertNotNull(analytics);
        assertEquals(mockDataUploadService, analytics.getDataUploadService());
        assertEquals(mockConfigDownloadService, analytics.getConfigDownloadService());
    }

}