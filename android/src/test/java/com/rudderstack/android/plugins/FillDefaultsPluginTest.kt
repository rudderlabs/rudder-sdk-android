/*
 * Creator: Debanjan Chatterjee on 08/12/23, 5:23 pm Last modified: 07/12/23, 7:12 pm
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

package com.rudderstack.android.plugins

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.android.internal.plugins.FillDefaultsPlugin
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.core.Analytics
import com.rudderstack.core.RudderLogger
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.models.*
import com.rudderstack.testcommon.Verification
import com.rudderstack.testcommon.assertArgument
import com.rudderstack.testcommon.generateTestAnalytics
import com.rudderstack.testcommon.testPlugin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(
    RobolectricTestRunner::class)
    @Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class FillDefaultsPluginTest {

    //    private val commonContext = mapOf(
//        "some_context1" to "some_value",
//        "some_context2" to "some_value_2"
//    )
    private lateinit var analytics: Analytics
    lateinit var mockConfig: ConfigurationAndroid

    private val fillDefaultsPlugin = FillDefaultsPlugin(
    )

    @Before
    fun setup() {
        mockConfig = ConfigurationAndroid(
            application = getApplicationContext(),
            anonymousId = "anon_id",
            userId = "user_id",
            shouldVerifySdk = false,
            analyticsExecutor = TestExecutor(),
            logLevel = RudderLogger.LogLevel.DEBUG,
        )
        analytics = generateTestAnalytics(mock(), mockConfig)
        analytics.associateState(ContextState())
        fillDefaultsPlugin.setup(analytics)
        fillDefaultsPlugin.updateConfiguration(mockConfig)
    }
    @After
    fun destroy() {
        analytics.shutdown()
    }

    /**
     * We intend to test if data is filled in properly
     *
     */
    @Test
    fun `test insertion of defaults`() {
        analytics.retrieveState<ContextState>()?.update(
            createContext(
                traits = mapOf(
                    "name" to "some_name", "age" to 24
                ), externalIds = listOf(
                    mapOf("braze_id" to "b_id"),
                    mapOf("amp_id" to "a_id"),
                ), customContextMap = mapOf(
                    "custom_name" to "c_name"
                )
            )
        )
        val message = TrackMessage.create(
            "ev-1", RudderUtils.timeStamp, traits = mapOf(
                "age" to 31, "office" to "Rudderstack"
            ), externalIds = listOf(
                mapOf("some_id" to "s_id"),
                mapOf("amp_id" to "amp_id"),
            ), customContextMap = null
        )

//        val chain = CentralPluginChain(message, listOf(fillDefaultsPlugin))
        analytics.testPlugin(fillDefaultsPlugin)
        analytics.track(message)
        analytics.assertArgument { input, output ->
            //check for expected values
            assertThat(output?.anonymousId, allOf(notNullValue(), `is`("anon_id")))
            assertThat(output?.userId, allOf(notNullValue(), `is`("user_id")))
            //message context to override
            assertThat(
                output?.context?.traits, allOf(
                    notNullValue(),
                    aMapWithSize(2),
                    hasEntry("age", 31),
                    hasEntry("office", "Rudderstack"),
//            hasEntry("name", "some_name"),
                )
            )
            assertThat(
                output?.context?.customContexts, allOf(
                    notNullValue(),
                    aMapWithSize(1),
                    hasEntry("custom_name", "c_name"),
                )
            )
            // track messages shouldn't contain external ids sent inside it.
            // but it should have the context values
            assertThat(
                output?.context?.externalIds,
                containsInAnyOrder(mapOf("braze_id" to "b_id"),
                    mapOf("amp_id" to "a_id"))

            )
        }

    }
}
