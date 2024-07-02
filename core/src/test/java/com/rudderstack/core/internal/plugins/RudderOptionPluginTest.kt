package com.rudderstack.core.internal.plugins

import com.rudderstack.core.BaseDestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOption
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.core.models.TrackMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.`in`
import org.hamcrest.Matchers.iterableWithSize
import org.hamcrest.Matchers.not
import org.junit.Test

class RudderOptionPluginTest {

    //for test we create 3 destinations
    private val dest1 = BaseDestinationPlugin<Any>("dest-1") {
        return@BaseDestinationPlugin it.proceed(it.message())
    }

    private val dest2 = BaseDestinationPlugin<Any>("dest-2") {
        return@BaseDestinationPlugin it.proceed(it.message())
    }
    private val dest3 = BaseDestinationPlugin<Any>("dest-3") {
        return@BaseDestinationPlugin it.proceed(it.message())
    }
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
    fun `test all true for empty integrations`() {
        //assertion plugin
        val assertPlugin = Plugin {
            //must contain all plugins
            assertThat(
                it.plugins, allOf(
                    iterableWithSize(5),
                    hasItems(dest1, dest2, dest3)
                )
            )
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(
            message, listOf(
                RudderOptionPlugin(RudderOption()), assertPlugin, dest1, dest2, dest3
            ), originalMessage = message
        )
        chain.proceed(message)
    }

    @Test
    fun `test all false for integrations`() {
        //assertion plugin
        val assertPlugin = Plugin {
            //must contain all plugins
            assertThat(
                it.plugins, allOf(
                    iterableWithSize(2),
                    everyItem(not(`in`(arrayOf(dest1, dest2, dest3))))
                )
            )
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(
            message, listOf(
                RudderOptionPlugin(
                    RudderOption()
                        .putIntegration("All", false)
                ), assertPlugin, dest1, dest2, dest3
            ), originalMessage = message
        )
        chain.proceed(message)
    }

    @Test
    fun `test custom integrations with false`() {
        //assertion plugin
        val assertPlugin = Plugin {
            //must contain all plugins
            assertThat(
                it.plugins, allOf(
                    iterableWithSize(3),
                    everyItem(not(`in`(arrayOf(dest2, dest3)))),
                    hasItem(dest1)
                )
            )
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(
            message, listOf(
                RudderOptionPlugin(
                    RudderOption()
                        .putIntegration("dest-2", false)
                        .putIntegration("dest-3", false)
                ), assertPlugin, dest1, dest2, dest3
            ), originalMessage = message
        )
        chain.proceed(message)
    }

    @Test
    fun `test custom integrations with true`() {
        //assertion plugin
        val assertPlugin = Plugin {
            //must contain all plugins
            assertThat(
                it.plugins, allOf(
                    iterableWithSize(3),
                    everyItem(not(`in`(arrayOf(dest1, dest3)))),
                    hasItem(dest2)
                )
            )
            return@Plugin it.proceed(it.message())
        }
        val chain = CentralPluginChain(
            message, listOf(
                RudderOptionPlugin(
                    RudderOption()
                        .putIntegration("All", false)
                        .putIntegration("dest-2", true)
                        .putIntegration("dest-3", false)
                ), assertPlugin, dest1, dest2, dest3
            ), originalMessage = message
        )
        chain.proceed(message)
    }

}
