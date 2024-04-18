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

package com.rudderstack.core

import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.models.Message
import com.rudderstack.models.TrackMessage
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

import io.mockk.mockk
import io.mockk.slot

/**
 * Testing flow of control through plugins.
 * We will check if plugins order is maintained.
 * Avoiding spying of objects.
 *
 */
class CentralPluginChainTest {

    @MockK
    lateinit var mockPlugin1: Plugin

    @MockK
    lateinit var mockPlugin2: Plugin

    @MockK
    lateinit var mockDestinationPlugin: DestinationPlugin<*>

    private val message: Message = TrackMessage.create(
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

    private lateinit var centralPluginChain: CentralPluginChain

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        // Mock the behavior of plugins and destination plugin
        every { mockPlugin1.intercept(any()) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }
        every { mockPlugin2.intercept(any()) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }
        every { mockDestinationPlugin.intercept(any()) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }

        // Initialize the list of plugins for testing
        val plugins = listOf(mockPlugin1, mockPlugin2, mockDestinationPlugin)
        centralPluginChain = CentralPluginChain(message, plugins, originalMessage = message)
    }

    @Test
    fun testMessage() {
        assertThat(centralPluginChain.message(), equalTo(message))
    }

    @Test
    fun testProceed() {
        // Verify interactions
        val chainCaptor1 = slot<CentralPluginChain>()
        every { mockPlugin1.intercept(capture(chainCaptor1)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }

        val chainCaptor2 = slot<CentralPluginChain>()
        every { mockPlugin2.intercept(capture(chainCaptor2)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }

        val chainCaptor3 = slot<CentralPluginChain>()
        every { mockDestinationPlugin.intercept(capture(chainCaptor3)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }

        // Call the method under test
        val resultMessage = centralPluginChain.proceed(message)

        val chain1 = chainCaptor1.captured
        assertThat(chain1.index, `is`(1))
        assertThat(chain1.plugins.size, `is`(3))
        assertThat(chain1.originalMessage, equalTo(message))

        val chain2 = chainCaptor2.captured
        assertThat(chain2.index, `is`(2))
        assertThat(chain2.plugins.size, `is`(3))
        assertThat(chain2.originalMessage, equalTo(message))

        val chain3 = chainCaptor3.captured
        assertThat(chain3.index, `is`(3))
        assertThat(chain3.plugins.size, `is`(3))
        assertThat(chain3.originalMessage, equalTo(message))

        // Assert the result
        assertThat(resultMessage, equalTo(message))
    }

    @Test(expected = IllegalStateException::class)
    fun testProceedTwice() {

        // Call the method under test twice
        centralPluginChain.proceed(message)
        centralPluginChain.proceed(message)
    }

    @Test
    fun testCentralPluginChainWithSubPlugins() {
        val subChainCaptor1 = slot<CentralPluginChain>()
        val subChainCaptor2 = slot<CentralPluginChain>()

        val subPlugin1 = mockk<DestinationPlugin.DestinationInterceptor>()
        every { subPlugin1.intercept(capture(subChainCaptor1)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }

        val subPlugin2 = mockk<DestinationPlugin.DestinationInterceptor>()
        // Mock the behavior of plugins and destination plugin
        every { subPlugin2.intercept(capture(subChainCaptor2)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(message)
        }

        every { mockDestinationPlugin.subPlugins } returns listOf(subPlugin1, subPlugin2)

        centralPluginChain.proceed(message)

        val chain1 = subChainCaptor1.captured
        assertThat(chain1.index, `is`(1)) // index should be 1 for sub plugins
        assertThat(chain1.plugins.size, `is`(2)) //number of plugins should be 2 for sub plugins
        assertThat(chain1.originalMessage, equalTo(message))

        val chain2 = subChainCaptor2.captured
        assertThat(chain2.index, `is`(2)) // index should be 2 for sub plugins
        assertThat(chain2.plugins.size, `is`(2)) //number of plugins should be 2 for sub plugins
        assertThat(chain2.originalMessage, equalTo(message))
    }
}
