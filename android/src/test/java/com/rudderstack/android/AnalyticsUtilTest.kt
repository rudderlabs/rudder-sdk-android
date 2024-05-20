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

package com.rudderstack.android

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.RudderAnalytics.Companion.getInstance
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class AnalyticsUtilTest {

    @Before
    fun setUp() {
        AnalyticsRegistry.clear()
    }

    @Test
    fun `given writeKey and configuration are passed, when anonymousId id is set, then assert that configuration has this anonymousId set as a property`() {
        val analytics = getInstance("testKey", ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter()
        ))

        analytics.setAnonymousId("anon_id")
        MatcherAssert.assertThat(
            analytics.currentConfigurationAndroid, allOf(Matchers.isA(ConfigurationAndroid::class.java),
                Matchers.hasProperty("anonymousId", Matchers.equalTo("anon_id"))
        ))
    }
}
