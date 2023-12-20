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

package com.rudderstack.android.android.internal.plugins

import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.internal.plugins.FillDefaultsPlugin
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.*
import com.vagabond.testcommon.Verification
import com.vagabond.testcommon.assertArgument
import com.vagabond.testcommon.generateTestAnalytics
import com.vagabond.testcommon.testPlugin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test


class FillDefaultsPluginTest {

    private val commonContext = mapOf(
        "some_context1" to "some_value",
        "some_context2" to "some_value_2"
    )
    private val fillDefaultsPlugin = FillDefaultsPlugin(

    )

    /**
     * We intend to test if data is filled in properly
     *
     */
    @Test
    fun `test insertion of defaults`() {
        ConfigurationsState.update(
            ConfigurationAndroid( ApplicationProvider.getApplicationContext(),
                JacksonAdapter(),
                anonymousId = "anon_id", userId =  "user_id"
            )
        )
        ContextState.update(
            createContext(
                mapOf(
                    "name" to "some_name",
                    "age" to 24
                ),
                externalIds = listOf(
                    mapOf("braze_id" to "b_id"),
                    mapOf("amp_id" to "a_id"),
                ),
                customContextMap = mapOf(
                    "custom_name" to "c_name"
                )
            )
        )
        val message = TrackMessage.create(
            "ev-1", RudderUtils.timeStamp,
            traits = mapOf(
                "age" to 31,
                "office" to "Rudderstack"
            ),
            externalIds = listOf(
                mapOf("some_id" to "s_id"),
                mapOf("amp_id" to "amp_id"),
            ),
            customContextMap = null
        )

//        val chain = CentralPluginChain(message, listOf(fillDefaultsPlugin))
        val analytics = generateTestAnalytics(JacksonAdapter())
        analytics.testPlugin(fillDefaultsPlugin)
        analytics.assertArgument(Verification<Message?, Message?> { input, output ->
            //check for expected values
            assertThat(output?.anonymousId, allOf(notNullValue(), `is`("anon_id")))
            assertThat(output?.userId, allOf(notNullValue(), `is`("user_id")))
            //message context to override
            assertThat(output?.context?.traits, allOf(
                notNullValue(),
                aMapWithSize(2),
                hasEntry("age", 31),
                hasEntry("office", "Rudderstack"),
//            hasEntry("name", "some_name"),
            ))
            assertThat(output?.context?.customContexts, allOf(
                notNullValue(),
                aMapWithSize(1),
                hasEntry("custom_name", "c_name"),
            ))
            assertThat(output?.context?.externalIds, allOf( notNullValue(),
                iterableWithSize(2),
                everyItem(
                    aMapWithSize(1)
                ), containsInAnyOrder(
                    mapOf(
                        "amp_id" to "amp_id"
                    ),
                    mapOf(
                        "some_id" to "s_id"
                    )
                )
            ))
            assertThat(output?.context, allOf(
                notNullValue(),
                hasEntry("some_context1","some_value"),
                hasEntry(
                    "some_context2","some_value_2")
            ))
        })
//        val updatedMsg =


    }
}