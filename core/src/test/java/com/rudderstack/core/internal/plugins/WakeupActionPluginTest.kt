/*
 * Creator: Debanjan Chatterjee on 18/01/22, 10:01 AM Last modified: 18/01/22, 10:01 AM
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

import com.rudderstack.core.Analytics
import com.rudderstack.core.BaseDestinationPlugin
import com.rudderstack.core.BasicStorageImpl
import com.rudderstack.core.Configuration
import com.rudderstack.core.DestinationConfig
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.core.internal.states.DestinationConfigState
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.TrackMessage
import com.rudderstack.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * Wake up action plugin forwards only those destination plugins, that have initialized.
 * In case each destination plugin is not initialized, it stores the message in startup queue.
 *
 * The testing purpose would be to check if startup queue is storing the messages for late
 * initialized destinations
 */
class WakeupActionPluginTest {
    private val dest1 = BaseDestinationPlugin<Any>("dest-1") {
        return@BaseDestinationPlugin it.proceed(it.message())
    }

    private val dest2 = BaseDestinationPlugin<Any>("dest-2") {
        return@BaseDestinationPlugin it.proceed(it.message())
    }
    private val dest3 = BaseDestinationPlugin<Any>("dest-3") {
        return@BaseDestinationPlugin it.proceed(it.message())
    }
    private val storage = BasicStorageImpl()
    private val wakeupActionPlugin = WakeupActionPlugin()
    private val testMessage = TrackMessage.create(
        "ev-1", RudderUtils.timeStamp, traits = mapOf(
            "age" to 31, "office" to "Rudderstack"
        ), externalIds = listOf(
            mapOf("some_id" to "s_id"),
            mapOf("amp_id" to "amp_id"),
        ), customContextMap = null
    )

    private lateinit var analytics: Analytics

    @Before
    fun setup() {
        analytics = generateTestAnalytics(
            mock(),
            Configuration(
                shouldVerifySdk = false), storage = storage
        )
        wakeupActionPlugin.setup(analytics)
    }
    private val destinationConfigState
        get() = analytics.retrieveState<DestinationConfigState>()

    @After
    fun breakDown() {
        analytics.shutdown()
        dest1.setReady(false)
        dest2.setReady(false)
        dest3.setReady(false)

        storage.clearStartupQueue()
        //clear destination config
        destinationConfigState?.update(DestinationConfig())
    }

    @Test
    fun `check startup queue for uninitialized destinations`() {
        dest1.setReady(false)
        dest2.setReady(true)
        dest3.setReady(true)
        destinationConfigState?.update(
            DestinationConfig(
                mapOf(
                    "dest-1" to dest1.isReady,
                    "dest-2" to dest2.isReady,
                    "dest-3" to dest3.isReady,
                )
            )
        )
        val plugins = listOf(wakeupActionPlugin)
        val centralPluginChain = CentralPluginChain(testMessage, plugins, originalMessage = testMessage)
        centralPluginChain.proceed(testMessage)
        //dest1 is not ready, hence message should be stored
        assertThat(
            storage.startupQueue, allOf(
                iterableWithSize(1), hasItem(testMessage)
            )
        )
    }

    @Test
    fun `check startup queue for initialized destinations`() {
        dest1.setReady(true)
        dest2.setReady(true)
        dest3.setReady(true)

        destinationConfigState?.update(
            DestinationConfig(
                mapOf(
                    "dest-1" to dest1.isReady,
                    "dest-2" to dest2.isReady,
                    "dest-3" to dest3.isReady,
                )
            )
        )
        val plugins = listOf(wakeupActionPlugin)
        val centralPluginChain = CentralPluginChain(testMessage, plugins, originalMessage = testMessage)
        centralPluginChain.proceed(testMessage)
        //dest1 is not ready, hence message should be stored
        assertThat(storage.startupQueue, iterableWithSize(0))
    }


}