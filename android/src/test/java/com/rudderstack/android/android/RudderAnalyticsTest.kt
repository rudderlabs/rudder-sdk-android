/*
 * Creator: Debanjan Chatterjee on 11/12/23, 11:14 am Last modified: 11/12/23, 11:14 am
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

package com.rudderstack.android.android

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.AnalyticsRegistry
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.createInstance
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.setAnonymousId
import com.rudderstack.core.Analytics
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(
    RobolectricTestRunner::class
)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class RudderAnalyticsTest {
    val writeKey = "writeKey"
    private lateinit var analytics: Analytics
    private lateinit var analytics2: Analytics
    val writeKey2 = "writeKey2"


    @Before
    fun setUp() {
        AnalyticsRegistry.clear()
        analytics = createInstance(
            writeKey, mock(),
            ApplicationProvider.getApplicationContext()
        )

        analytics2 = createInstance(writeKey2, mock(), ApplicationProvider.getApplicationContext())

    }

    @After
    fun destroy() {
        analytics.shutdown()
        analytics2.shutdown()
    }

    @Test
    fun `test put anonymous id`() {
        val analytics = createInstance("testKey", mock(), ApplicationProvider.getApplicationContext
            (), configurationInitializer = {
            copy(anonymousId = "anon_id")
        })

        analytics.setAnonymousId("anon_id")
        MatcherAssert.assertThat(
            analytics.currentConfigurationAndroid, allOf(
                Matchers.isA(ConfigurationAndroid::class.java),
                Matchers.hasProperty("anonymousId", Matchers.equalTo("anon_id"))
            )
        )
    }

    @Test
    fun `when writeKey and configuration is passed, then createInstance should return Analytics instance`() {

        MatcherAssert.assertThat(analytics, Matchers.isA(Analytics::class.java))
    }

    @Test
    fun `when multiple instances are created with different writeKeys, then the instances should be different`() {
        MatcherAssert.assertThat(analytics, Matchers.isA(Analytics::class.java))
        MatcherAssert.assertThat(analytics2, Matchers.isA(Analytics::class.java))
        assert(analytics != analytics2)
    }

    @Test
    fun `given instance is already created with the writeKey, when createInstance is called with same write key, then the previous instance should be returned`() {
        val dupeAnalytics = createInstance(
            writeKey, mock(),
            ApplicationProvider.getApplicationContext()
        )

        MatcherAssert.assertThat(analytics, Matchers.isA(Analytics::class.java))
        MatcherAssert.assertThat(dupeAnalytics, Matchers.isA(Analytics::class.java))
        assert(analytics === dupeAnalytics)
    }

    @Test
    fun `given instance is already created with the writeKey, when getInstance is called with that write key, then the Analytics instance should be returned`() {
        val result = AnalyticsRegistry.getInstance(writeKey)
        assert(result == analytics)

    }

    @Test
    fun `given multiple instances are already created with different writeKeys, when getInstance is called with those write keys, then the Analytics instances should be returned`() {

        val result1 = AnalyticsRegistry.getInstance(writeKey)
        val result2 = AnalyticsRegistry.getInstance(writeKey2)

        assert(result1 == analytics)
        assert(result2 == analytics2)
    }
}
