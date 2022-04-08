/*
 * Creator: Debanjan Chatterjee on 04/04/22, 1:36 PM Last modified: 04/04/22, 1:36 PM
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

package com.rudderstack.android.core.internal.plugins

import com.rudderstack.android.core.Settings
import com.rudderstack.android.core.Utils
import com.rudderstack.android.core.internal.CentralPluginChain
import com.rudderstack.android.core.internal.states.ContextState
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test


class FillDefaultsPluginTest {

    private val commonContext = mapOf(
        "some_context1" to "some_value",
        "some_context2" to "some_value_2"
    )
    private val fillDefaultsPlugin = FillDefaultsPlugin(
        commonContext,
        SettingsState, ContextState
    )

    /**
     * We intend to test if data is filled in properly
     *
     */
    @Test
    fun `test insertion of defaults`() {
        SettingsState.update(
            Settings(
                "anon_id", "user_id"
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
            "ev-1", Utils.timeStamp,
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

        val chain = CentralPluginChain(message, listOf(fillDefaultsPlugin))
        val updatedMsg = chain.proceed(message)

        //check for expected values
        assertThat(updatedMsg.anonymousId, allOf(notNullValue(), `is`("anon_id")))
        assertThat(updatedMsg.userId, allOf(notNullValue(), `is`("user_id")))
        //message context to override
        assertThat(updatedMsg.context?.traits, allOf(
            notNullValue(),
            aMapWithSize(2),
            hasEntry("age", 31),
            hasEntry("office", "Rudderstack"),
//            hasEntry("name", "some_name"),
        ))
        assertThat(updatedMsg.context?.customContexts, allOf(
            notNullValue(),
            aMapWithSize(1),
            hasEntry("custom_name", "c_name"),
        ))
        assertThat(updatedMsg.context?.externalIds, allOf( notNullValue(),
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
        assertThat(updatedMsg.context, allOf(
            notNullValue(),
            hasEntry("some_context1","some_value"),
            hasEntry(
            "some_context2","some_value_2")
        ))
    }
}