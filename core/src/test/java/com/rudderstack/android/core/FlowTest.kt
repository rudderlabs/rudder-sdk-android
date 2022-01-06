/*
 * Creator: Debanjan Chatterjee on 31/12/21, 11:27 AM Last modified: 31/12/21, 11:27 AM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.android.core

import com.rudderstack.android.core.internal.CentralPluginChain
import com.rudderstack.android.models.TrackMessage
import com.rudderstack.android.models.android.RudderContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*

/**
 * Testing flow of control through plugins.
 * We will check if plugins order is maintained.
 * Avoiding spying of objects.
 *
 */
class FlowTest {
    companion object{
        //Plugins should be called in this order
        private const val SUB_PLUGIN_1_1_SL_NO = 1
        private const val SUB_PLUGIN_2_1_SL_NO = 2
        private const val MAIN_PLUGIN_1_SL_NO = 3
        private const val SUB_PLUGIN_1_2_SL_NO = 4
        private const val MAIN_PLUGIN_2_SL_NO = 5
        private const val MAIN_PLUGIN_3_SL_NO = 6
        private const val TOTAL_NO_OF_CALLS = 7
    }

    private var enteredPluginNo = 1
    private val dummyPlugin1  = DestinationPlugin<Any>("dummy1") {
        val msg = it.message()
        assertThat(enteredPluginNo ++, `is`(MAIN_PLUGIN_1_SL_NO))
        //message and original message should be different for destination plugin
        assertThat(msg, not(it.originalMessage))
        it.proceed(it.originalMessage)
    }
    private val dummyPlugin2 = DestinationPlugin<Any>("dummy2") {
        val msg = it.message()
        assertThat(enteredPluginNo ++, `is`(MAIN_PLUGIN_2_SL_NO))

        //message and original message should be different for destination plugin
        assertThat(msg, not(it.originalMessage))
        //destination plugins should always call proceed on original message to discard changes.
        it.proceed(it.originalMessage)
    }
    private val dummyPlugin3 = Plugin {
        val msg = it.message()
        assertThat(enteredPluginNo ++, `is`(MAIN_PLUGIN_3_SL_NO))
        it.proceed(msg)
    }
    @Before
    fun setup(){
        dummyPlugin1.addSubPlugin {
            assertThat(enteredPluginNo++, `is`(SUB_PLUGIN_1_1_SL_NO))
            it.proceed(it.message())
        }
        dummyPlugin2.addSubPlugin {
            assertThat(enteredPluginNo ++, `is`(SUB_PLUGIN_1_2_SL_NO))
            it.proceed(it.message())
        }
        dummyPlugin1.addSubPlugin {
            assertThat(enteredPluginNo ++, `is`(SUB_PLUGIN_2_1_SL_NO))
            it.proceed(it.message())
        }
    }

    @Test
    fun testCentralPluginChain(){
        val dummyMsg = TrackMessage("message_id",
            anonymousId = "anon_id", timestamp = Date().toString())
        CentralPluginChain(dummyMsg, listOf(
            dummyPlugin1, dummyPlugin2, dummyPlugin3) )
            .proceed(dummyMsg)
        //check if all plugins and sub plugins are called
        assertThat(enteredPluginNo , `is`(TOTAL_NO_OF_CALLS))
    }

}