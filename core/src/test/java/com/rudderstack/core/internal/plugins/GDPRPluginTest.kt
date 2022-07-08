/*
 * Creator: Debanjan Chatterjee on 18/01/22, 9:59 AM Last modified: 18/01/22, 9:59 AM
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

package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Plugin
import com.rudderstack.core.Settings
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.models.TrackMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test

class GDPRPluginTest {
    private val gdprPlugin = GDPRPlugin()
    private val message = TrackMessage.create(
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

    @Test
    fun `test gdpr with opt out`() {
        val testPluginForOptOut = Plugin {
            //should not be called
            assert(false)
            it.proceed(it.message())
        }
        val optOutTestChain = CentralPluginChain(message, listOf(gdprPlugin, testPluginForOptOut))
        //opted out
        gdprPlugin.updateSettings(Settings(isOptOut = true))
        //check for opt out
        val returnedMsg = optOutTestChain.proceed(message)
        assertThat(returnedMsg, Matchers.`is`(returnedMsg))
    }

    @Test
    fun `test gdpr with opt in`() {
        var isCalled = false
        val testPluginForOptIn = Plugin {
            //should be called
            isCalled = true
            it.proceed(it.message())
        }
        val optInTestChain = CentralPluginChain(message, listOf(gdprPlugin, testPluginForOptIn))
        //opted out
        gdprPlugin.updateSettings(Settings(isOptOut = false))
        //check for opt out
        val returnedMsg = optInTestChain.proceed(message)
        assertThat(returnedMsg, Matchers.`is`(returnedMsg))
        assertThat(isCalled, Matchers.`is`(true))
    }
}