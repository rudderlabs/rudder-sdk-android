/*
 * Creator: Debanjan Chatterjee on 18/03/24, 3:39 pm Last modified: 11/03/24, 1:55 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.android

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.core.Analytics
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(
    RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])class RudderAnalyticsTest {
    private lateinit var analytics: Analytics

    @Before
    fun setup() {
        analytics = RudderAnalytics("testKey", ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter()
        ))
    }

    @Test
    fun `test put anonymous id`() {
        analytics.setAnonymousId("anon_id")
        MatcherAssert.assertThat(
            analytics.currentConfigurationAndroid, allOf(Matchers.isA(ConfigurationAndroid::class.java),
                Matchers.hasProperty("anonymousId", Matchers.equalTo("anon_id"))
        ))
    }
}