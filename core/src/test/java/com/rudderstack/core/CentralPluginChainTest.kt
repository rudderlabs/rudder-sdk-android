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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Testing flow of control through plugins.
 * We will check if plugins order is maintained.
 * Avoiding spying of objects.
 *
 */
@RunWith(MockitoJUnitRunner::class)
class CentralPluginChainTest {

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
    @Mock
    lateinit var mockPlugin1: Plugin

    @Mock
    lateinit var mockPlugin2: Plugin

    @Mock
    lateinit var mockDestinationPlugin: DestinationPlugin<*>

    @Mock
    lateinit var mockMessage: Message

    private lateinit var centralPluginChain: CentralPluginChain

    /*private val dummyPlugin1  = BaseDestinationPlugin<Any>("dummy1") {
        val msg = it.message()
        assertThat(enteredPluginNo ++, `is`(MAIN_PLUGIN_1_SL_NO))
        //message and original message should be different for destination plugin
        assertThat(msg, not(it.originalMessage))
        it.proceed(it.originalMessage)
    }
    private val dummyPlugin2 = BaseDestinationPlugin<Any>("dummy2") {
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
    }*/
    @Before
    fun setup(){
        // Mock the behavior of plugins and destination plugin
        whenever(mockPlugin1.intercept(any())).thenAnswer{
            val chain = it.arguments[0] as CentralPluginChain
            chain.proceed(mockMessage)
        }
        whenever(mockPlugin2.intercept(any())).thenAnswer{
            val chain = it.arguments[0] as CentralPluginChain
            chain.proceed(mockMessage)
        }
        whenever(mockDestinationPlugin.intercept(any())).thenAnswer{
            val chain = it.arguments[0] as CentralPluginChain
            chain.proceed(mockMessage)
        }
        // Initialize the list of plugins for testing
        val plugins = listOf(mockPlugin1, mockPlugin2, mockDestinationPlugin)
        centralPluginChain = CentralPluginChain(mockMessage, plugins)
        whenever(mockMessage.copy()).thenReturn(mockMessage)
    }
    @Test
    fun testMessage() {
        assertThat(centralPluginChain.message(), equalTo(mockMessage))
    }

    @Test
    fun testProceed() {


        // Call the method under test
        val resultMessage = centralPluginChain.proceed(mockMessage)

        // Verify interactions
        val chainCaptor1 = argumentCaptor<CentralPluginChain>()
        verify(mockPlugin1).intercept(chainCaptor1.capture())
        val chain1 = chainCaptor1.lastValue
        assertThat(chain1.index, `is`(1))
        assertThat(chain1.plugins.size, `is`(3))
        assertThat(chain1.originalMessage, equalTo(mockMessage))

        val chainCaptor2 = argumentCaptor<CentralPluginChain>()
        verify(mockPlugin2).intercept(chainCaptor2.capture())
        val chain2 = chainCaptor2.lastValue
        assertThat(chain2.index, `is`(2))
        assertThat(chain2.plugins.size, `is`(3))
        assertThat(chain2.originalMessage, equalTo(mockMessage))

        val chainCaptor3 = argumentCaptor<CentralPluginChain>()
        verify(mockDestinationPlugin).intercept(chainCaptor3.capture())
        val chain3 = chainCaptor3.lastValue
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
    fun testCentralPluginChainWithSubPlugins(){
        val subPlugin1 = mock<DestinationPlugin.DestinationInterceptor>()
        whenever(subPlugin1.intercept(any())).thenAnswer{
            val chain = it.arguments[0] as CentralPluginChain
            chain.proceed(mockMessage)
        }
        val subPlugin2 = mock<DestinationPlugin.DestinationInterceptor>()
        // Mock the behavior of plugins and destination plugin
        whenever(subPlugin2.intercept(any())).thenAnswer{
            val chain = it.arguments[0] as CentralPluginChain
            chain.proceed(mockMessage)
        }
        whenever(mockDestinationPlugin.subPlugins).thenReturn(listOf(subPlugin1, subPlugin2))

        centralPluginChain.proceed(mockMessage)
        // Verify interactions
        val subChainCaptor1 = argumentCaptor<CentralPluginChain>()
        verify(subPlugin1).intercept(subChainCaptor1.capture())
        val chain1 = subChainCaptor1.lastValue
        assertThat(chain1.index, `is`(1)) // index should be 1 for sub plugins
        assertThat(chain1.plugins.size, `is`(2)) //number of plugins should be 2 for sub plugins
        assertThat(chain1.originalMessage, equalTo(mockMessage))

        val subChainCaptor2 = argumentCaptor<CentralPluginChain>()
        verify(subPlugin2).intercept(subChainCaptor2.capture())
        val chain2 = subChainCaptor2.lastValue
        assertThat(chain2.index, `is`(2)) // index should be 2 for sub plugins
        assertThat(chain2.plugins.size, `is`(2)) //number of plugins should be 2 for sub plugins
        assertThat(chain2.originalMessage, equalTo(mockMessage))
    }

    /*@Test
    fun testCentralPluginChain(){
        val dummyMsg = TrackMessage.create("message_id",
            anonymousId = "anon_id", timestamp = Date().toString())
        CentralPluginChain(dummyMsg, listOf(
            dummyPlugin1, dummyPlugin2, dummyPlugin3) )
            .proceed(dummyMsg)
        //check if all plugins and sub plugins are called
        assertThat(enteredPluginNo , `is`(TOTAL_NO_OF_CALLS))
    }

*/

}