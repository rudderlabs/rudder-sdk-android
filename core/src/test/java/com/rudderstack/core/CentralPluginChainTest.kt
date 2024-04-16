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

    @MockK
    lateinit var mockMessage: Message

    private lateinit var centralPluginChain: CentralPluginChain

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        // Mock the behavior of plugins and destination plugin
        every { mockPlugin1.intercept(any()) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }
        every { mockPlugin2.intercept(any()) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }
        every { mockDestinationPlugin.intercept(any()) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }

        // Initialize the list of plugins for testing
        val plugins = listOf(mockPlugin1, mockPlugin2, mockDestinationPlugin)
        centralPluginChain = CentralPluginChain(mockMessage, plugins, originalMessage = mockMessage)
        every { mockMessage.copy() } returns mockMessage
    }

    @Test
    fun testMessage() {
        assertThat(centralPluginChain.message(), equalTo(mockMessage))
    }

    @Test
    fun testProceed() {
        // Verify interactions
        val chainCaptor1 = slot<CentralPluginChain>()
        every { mockPlugin1.intercept(capture(chainCaptor1)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }

        val chainCaptor2 = slot<CentralPluginChain>()
        every { mockPlugin2.intercept(capture(chainCaptor2)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }


        val chainCaptor3 = slot<CentralPluginChain>()
        every { mockDestinationPlugin.intercept(capture(chainCaptor3)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }

        // Call the method under test
        val resultMessage = centralPluginChain.proceed(mockMessage)

        val chain1 = chainCaptor1.captured
        assertThat(chain1.index, `is`(1))
        assertThat(chain1.plugins.size, `is`(3))
        assertThat(chain1.originalMessage, equalTo(mockMessage))

        val chain2 = chainCaptor2.captured
        assertThat(chain2.index, `is`(2))
        assertThat(chain2.plugins.size, `is`(3))
        assertThat(chain2.originalMessage, equalTo(mockMessage))


        val chain3 = chainCaptor3.captured
        assertThat(chain3.index, `is`(3))
        assertThat(chain3.plugins.size, `is`(3))
        assertThat(chain3.originalMessage, equalTo(mockMessage))

        // Assert the result
        assertThat(resultMessage, equalTo(mockMessage))
    }

    @Test(expected = IllegalStateException::class)
    fun testProceedTwice() {

        // Call the method under test twice
        centralPluginChain.proceed(mockMessage)
        centralPluginChain.proceed(mockMessage)
    }

    @Test
    fun testCentralPluginChainWithSubPlugins() {
        val subChainCaptor1 = slot<CentralPluginChain>()
        val subChainCaptor2 = slot<CentralPluginChain>()

        val subPlugin1 = mockk<DestinationPlugin.DestinationInterceptor>()
        every { subPlugin1.intercept(capture(subChainCaptor1)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }

        val subPlugin2 = mockk<DestinationPlugin.DestinationInterceptor>()
        // Mock the behavior of plugins and destination plugin
        every { subPlugin2.intercept(capture(subChainCaptor2)) } answers {
            val chain = arg<CentralPluginChain>(0)
            chain.proceed(mockMessage)
        }

        every { mockDestinationPlugin.subPlugins } returns listOf(subPlugin1, subPlugin2)

        centralPluginChain.proceed(mockMessage)

        val chain1 = subChainCaptor1.captured
        assertThat(chain1.index, `is`(1)) // index should be 1 for sub plugins
        assertThat(chain1.plugins.size, `is`(2)) //number of plugins should be 2 for sub plugins
        assertThat(chain1.originalMessage, equalTo(mockMessage))

        val chain2 = subChainCaptor2.captured
        assertThat(chain2.index, `is`(2)) // index should be 2 for sub plugins
        assertThat(chain2.plugins.size, `is`(2)) //number of plugins should be 2 for sub plugins
        assertThat(chain2.originalMessage, equalTo(mockMessage))
    }
}
