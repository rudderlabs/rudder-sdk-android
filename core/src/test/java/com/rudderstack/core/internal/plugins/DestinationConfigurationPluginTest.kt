/*
 * Creator: Debanjan Chatterjee on 04/04/22, 1:35 PM Last modified: 04/04/22, 1:35 PM
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

import com.rudderstack.core.BaseDestinationPlugin
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.models.TrackMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Date

/**
 * For testing [DestinationConfigurationPlugin] we will create
 * destination plugins, update rudder server config and check the result
 * of plugin.intercept
 */
class DestinationConfigurationPluginTest {
    private val destinations = listOf<Plugin>(
        BaseDestinationPlugin<Any>("d-1") {
            return@BaseDestinationPlugin it.proceed(it.message())
        },
        BaseDestinationPlugin<Any>("d-2") {
            return@BaseDestinationPlugin it.proceed(it.message())
        },
        BaseDestinationPlugin<Any>("d-3") {
            return@BaseDestinationPlugin it.proceed(it.message())
        },
        BaseDestinationPlugin<Any>("d-4") {
            return@BaseDestinationPlugin it.proceed(it.message())
        }
    )
    private val message = TrackMessage.create("some_event", timestamp = RudderUtils.timeStamp)

    //test plugin
    private var destinationConfigurationPlugin: DestinationConfigurationPlugin? = null

    //chain to proceed with
    private var defaultPluginChain: CentralPluginChain? = null

    @Before
    fun setup() {
        destinationConfigurationPlugin = DestinationConfigurationPlugin()
        defaultPluginChain = CentralPluginChain(message,
            destinations/*.toMutableList().also {
                it.add(0, destinationConfigurationPlugin!!)
            }*/)
    }

    @After
    fun destroy() {
        destinationConfigurationPlugin = null
        defaultPluginChain = null
    }

    @Test
    fun `test destination filtering with config set`() {
        //setting config
        destinationConfigurationPlugin?.updateRudderServerConfig(
            RudderServerConfig(
                source =
                RudderServerConfig.RudderServerConfigSource(
                    destinations =
                    listOf(
                        RudderServerConfig.RudderServerDestination(
                            destinationId = "1",
                            destinationName = "d-1",
                            destinationConfig = mapOf(),
                            isDestinationEnabled = true
                        ),
                        RudderServerConfig.RudderServerDestination(
                            destinationId = "2",
                            destinationName = "d-2",
                            destinationConfig = mapOf(),
                            isDestinationEnabled = false
                        )
                    )
                )
            )
        )
        //adding a assertion plugin
        val centralPluginChain = defaultPluginChain!!.copy(
            plugins = defaultPluginChain!!.plugins.toMutableList().also {
                //after destination config plugin
                it.add(0, Plugin {
                    println("intercepted by assertion plugin")
                    //after processing the chain should be devoid of d-2
                    assertThat(
                        it.plugins, allOf(
                            Matchers.hasItems(*(destinations.toMutableList().also {
                                it.removeIf {
                                    it is DestinationPlugin<*> && it.name == "d-2"
                                }
                            }.toTypedArray())),
                            everyItem(not(destinations[1]/*isIn(shouldNotBeInList)*/))
                        )
                    )
                    it.proceed(it.message())
                })
            }
        )
        destinationConfigurationPlugin!!.intercept(centralPluginChain)


    }@Test
    fun `test destination filtering with config not set`() {

        //adding a assertion plugin
        val centralPluginChain = defaultPluginChain!!.copy(
            plugins = defaultPluginChain!!.plugins.toMutableList().also {
                //after destination config plugin
                it.add(0, Plugin {
                    println("intercepted by assertion plugin")
                    //after processing the chain should be devoid of d-2
                    assertThat(
                        it.plugins, allOf(iterableWithSize(1), //the test plugin
                        //check there should be no destination plugin
                            everyItem(not(isA(DestinationPlugin::class.java)))
                             )
                    )
                    it.proceed(it.message())
                })
            }
        )
        destinationConfigurationPlugin!!.intercept(centralPluginChain)
    }



}