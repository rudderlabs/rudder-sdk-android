package com.rudderstack.core.internal.plugins

import com.rudderstack.core.BaseDestinationPlugin
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.core.models.RudderServerConfig
import com.rudderstack.core.models.TrackMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.isA
import org.hamcrest.Matchers.iterableWithSize
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test

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
        defaultPluginChain = CentralPluginChain(
            message,
            destinations, originalMessage = message/*.toMutableList().also {
                it.add(0, destinationConfigurationPlugin!!)
            }*/
        )
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
    }

    @Test
    fun `test destination filtering with config not set`() {

        //adding a assertion plugin
        val centralPluginChain = defaultPluginChain!!.copy(
            plugins = defaultPluginChain!!.plugins.toMutableList().also {
                //after destination config plugin
                it.add(0, Plugin {
                    //after processing the chain should be devoid of d-2
                    assertThat(
                        it.plugins, allOf(
                            iterableWithSize(1), //the test plugin
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
