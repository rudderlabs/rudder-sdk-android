package com.rudderstack.core.internal.plugins

import com.rudderstack.core.BaseDestinationPlugin
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.core.models.RudderServerConfig
import com.rudderstack.core.models.TrackMessage
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class EventFilteringPluginTest {
    // Test subject
    private lateinit var eventFilteringPlugin: EventFilteringPlugin

    @Before
    fun setUp() {
        eventFilteringPlugin = EventFilteringPlugin()
        eventFilteringPlugin.onShutDown()
        eventFilteringPlugin.setup(generateTestAnalytics(JacksonAdapter()))
    }

    @Test
    fun testIntercept_AllowAllEventsWhenNoServerConfig() {
        // Given the plugin is not updated with server config,
        // any message reaching it will be forwarded without a check
        val chain = mock<Plugin.Chain>()
        val demoMessage = TrackMessage.create(
            "testEvent", anonymousId = "anon-1", timestamp =
            Date().toString()
        )
        whenever(chain.message()).thenReturn(demoMessage)
        //when
        eventFilteringPlugin.intercept(chain)
        // same chain is used
        verify(chain, times(1)).proceed(demoMessage)
    }

    @Test
    fun testIntercept_AllowAllEventsWhenServerConfigWithoutDestinations() {
        // Given
        val chain = mock<Plugin.Chain>()
        val serverConfig = RudderServerConfig(isHosted = true)
        val demoMessage = TrackMessage.create(
            "testEvent", anonymousId = "anon-1", timestamp =
            Date().toString()
        )
        whenever(chain.message()).thenReturn(demoMessage)
//        val dummyPlugins = listOf(Plugin { it.proceed(it.message()) },
//            Plugin { it.proceed(it.message()) })
//        // When
//        whenever(chain.plugins).thenReturn(dummyPlugins)
        eventFilteringPlugin.updateRudderServerConfig(serverConfig)
        eventFilteringPlugin.intercept(chain)

        //Then
        //no copy of chain is made since filteredEventsMap is empty
        verify(chain, times(1)).proceed(demoMessage)

    }

    @Test
    fun testIntercept_AllowAllEventsWhenServerConfigWithEmptyDestinations() {
        // Given
        val chain = mock<Plugin.Chain>()
        val serverConfig = RudderServerConfig(
            isHosted = true,
            source = RudderServerConfig.RudderServerConfigSource(destinations = emptyList())
        )

        // When
        val demoMessage = TrackMessage.create(
            "testEvent", anonymousId = "anon-1", timestamp =
            Date().toString()
        )
        whenever(chain.message()).thenReturn(demoMessage)
        eventFilteringPlugin.updateRudderServerConfig(serverConfig)
        eventFilteringPlugin.intercept(chain)
        // Then
        //no copy of chain is made since filteredEventsMap is empty
        verify(chain, times(1)).proceed(demoMessage)
    }

    @Test
    fun `allow all destination plugins when event name not in black list`() {
        // Given
        val chain = mock<Plugin.Chain>()
        val serverConfig = RudderServerConfig(
            isHosted = true,
            source = RudderServerConfig.RudderServerConfigSource(
                destinations = listOf(
                    RudderServerConfig.RudderServerDestination(
                        destinationId = "test",
                        destinationName = "TestDestination",
                        destinationDefinition = RudderServerConfig.RudderServerDestinationDefinition(
                            definitionName = "Firebase",
                        ),
                        isDestinationEnabled = true,
                        updatedAt = "2022-01-01",
                        destinationConfig = mapOf(
                            "eventFilteringOption" to "blacklistedEvents",
                            "blacklistedEvents" to setOf(
                                mapOf("eventName" to "test1"), mapOf(
                                    "eventName" to
                                            "test2",
                                ), mapOf("eventName" to "test3")
                            )
                        )
                    )
                )
            )
        )
        val demoMessage = TrackMessage.create(
            "testEvent", anonymousId = "anon-1", timestamp =
            Date().toString()
        )
        whenever(chain.message()).thenReturn(demoMessage)
        //destination plugin list
        whenever(chain.plugins).thenReturn(
            listOf(
                createDummyDestinationPluginWithName("Firebase"),
                createDummyDestinationPluginWithName("Mixpanel"),
            )
        )
        var chainCopy: Plugin.Chain? = null
        whenever(chain.with(anyList())).then {
            val list = it.arguments[0] as List<Plugin>
            CentralPluginChain(demoMessage, list, originalMessage = demoMessage).also { chainCopy = it }
        }
        // When
        eventFilteringPlugin.updateRudderServerConfig(serverConfig)

        eventFilteringPlugin.intercept(chain)

        // Then
        verify(chain, times(1)).with(anyOrNull())
        MatcherAssert.assertThat(
            chainCopy, allOf(
                org.hamcrest.Matchers.notNullValue(),
                org.hamcrest.Matchers.instanceOf(CentralPluginChain::class.java),
                org.hamcrest.Matchers.hasProperty(
                    "plugins",
                    allOf(
                        Matchers.iterableWithSize<Plugin>(2), Matchers.hasItem(
                            allOf(
                                Matchers.instanceOf<BaseDestinationPlugin<*>>
                                    (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Firebase"))
                            )
                        ), Matchers.hasItem(
                            allOf(
                                Matchers.instanceOf<BaseDestinationPlugin<*>>
                                    (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Mixpanel"))
                            )
                        )
                    )
                )
            )
        )
        //a copy of chain is made since filteredEventsMap is not empty
        verify(chain, never()).proceed(demoMessage)
    }

    @Test
    fun `disallow destination plugins when event name in black list`() {
        // Given
        val chain = mock<Plugin.Chain>()
        val serverConfig = RudderServerConfig(
            isHosted = true,
            source = RudderServerConfig.RudderServerConfigSource(
                destinations = listOf(
                    RudderServerConfig.RudderServerDestination(
                        destinationId = "test",
                        destinationName = "TestDestination",
                        destinationDefinition = RudderServerConfig.RudderServerDestinationDefinition(
                            definitionName = "Firebase",
                        ),
                        isDestinationEnabled = true,
                        updatedAt = "2022-01-01",
                        destinationConfig = mapOf(
                            "eventFilteringOption" to "blacklistedEvents",
                            "blacklistedEvents" to setOf(
                                mapOf("eventName" to "test1"), mapOf(
                                    "eventName" to
                                            "test2",
                                ), mapOf("eventName" to "test3")
                            )
                        )
                    )
                )
            )
        )
        val demoMessage = TrackMessage.create(
            "test1", anonymousId = "anon-1", timestamp =
            Date().toString()
        )
        whenever(chain.message()).thenReturn(demoMessage)
        //destination plugin list
        whenever(chain.plugins).thenReturn(
            listOf(
                createDummyDestinationPluginWithName("Firebase"),
                createDummyDestinationPluginWithName("Mixpanel"),
            )
        )
        var chainCopy: Plugin.Chain? = null
        whenever(chain.with(anyList())).then {
            val list = it.arguments[0] as List<Plugin>
            CentralPluginChain(demoMessage, list, originalMessage = demoMessage).also { chainCopy = it }
        }
        // When
        eventFilteringPlugin.updateRudderServerConfig(serverConfig)
        eventFilteringPlugin.intercept(chain)

        // Then
        verify(chain, times(1)).with(anyOrNull())
        MatcherAssert.assertThat(
            chainCopy, allOf(
                org.hamcrest.Matchers.notNullValue(),
                org.hamcrest.Matchers.instanceOf(CentralPluginChain::class.java),
                org.hamcrest.Matchers.hasProperty(
                    "plugins",
                    allOf(
                        Matchers.iterableWithSize<Plugin>(1), not(
                            Matchers.hasItem(
                                allOf(
                                    Matchers.instanceOf<BaseDestinationPlugin<*>>
                                        (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Firebase"))
                                )
                            )
                        ), Matchers.hasItem(
                            allOf(
                                Matchers.instanceOf<BaseDestinationPlugin<*>>
                                    (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Mixpanel"))
                            )
                        )
                    )
                )
            )
        )
        //a copy of chain is made since filteredEventsMap is not empty
        verify(chain, never()).proceed(demoMessage)
    }

    @Test
    fun `disallow destination plugins when event name not in white list`() {
        // Given
        val chain = mock<Plugin.Chain>()
        val serverConfig = RudderServerConfig(
            isHosted = true,
            source = RudderServerConfig.RudderServerConfigSource(
                destinations = listOf(
                    RudderServerConfig.RudderServerDestination(
                        destinationId = "test",
                        destinationName = "TestDestination",
                        destinationDefinition = RudderServerConfig.RudderServerDestinationDefinition(
                            definitionName = "Firebase",
                        ),
                        isDestinationEnabled = true,
                        updatedAt = "2022-01-01",
                        destinationConfig = mapOf(
                            "eventFilteringOption" to "whitelistedEvents",
                            "whitelistedEvents" to setOf(
                                mapOf("eventName" to "test1"), mapOf
                                    (
                                    "eventName" to
                                            "test2",
                                ), mapOf("eventName" to "test3")
                            )
                        )
                    ), RudderServerConfig.RudderServerDestination(
                        destinationId = "test2",
                        destinationName = "TestDestination2",
                        destinationDefinition = RudderServerConfig.RudderServerDestinationDefinition(
                            definitionName = "Braze",
                        ),
                        isDestinationEnabled = true,
                        updatedAt = "2022-01-01",
                        destinationConfig = mapOf(
                            "eventFilteringOption" to "blacklistedEvents",
                            "blacklistedEvents" to setOf(
                                mapOf("eventName" to "test2"), mapOf
                                    (
                                    "eventName" to
                                            "test2",
                                ), mapOf("eventName" to "test3")
                            )
                        )
                    )
                )
            )
        )
        val demoMessage = TrackMessage.create(
            "testEvent", anonymousId = "anon-1", timestamp =
            Date().toString()
        )
        whenever(chain.message()).thenReturn(demoMessage)
        //destination plugin list
        whenever(chain.plugins).thenReturn(
            listOf(
                createDummyDestinationPluginWithName("Firebase"),
                createDummyDestinationPluginWithName("Mixpanel"),
            )
        )
        var chainCopy: Plugin.Chain? = null
        whenever(chain.with(anyList())).then {
            val list = it.arguments[0] as List<Plugin>
            CentralPluginChain(demoMessage, list, originalMessage = demoMessage).also { chainCopy = it }
        }
        // When
        eventFilteringPlugin.updateRudderServerConfig(serverConfig)
        eventFilteringPlugin.intercept(chain)

        // Then
        verify(chain, times(1)).with(anyOrNull())
        MatcherAssert.assertThat(
            chainCopy, allOf(
                org.hamcrest.Matchers.notNullValue(),
                org.hamcrest.Matchers.instanceOf(CentralPluginChain::class.java),
                org.hamcrest.Matchers.hasProperty(
                    "plugins",
                    allOf(
                        Matchers.iterableWithSize<Plugin>(1), not(
                            Matchers.hasItem(
                                allOf(
                                    Matchers.instanceOf<BaseDestinationPlugin<*>>
                                        (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Firebase"))
                                )
                            )
                        ), Matchers.hasItem(
                            allOf(
                                Matchers.instanceOf<BaseDestinationPlugin<*>>
                                    (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Mixpanel"))
                            )
                        )
                    )
                )
            )
        )
        //a copy of chain is made since filteredEventsMap is not empty
        verify(chain, never()).proceed(demoMessage)
    }

    @Test
    fun `allow destination plugins when event name in white list`() {
        // Given
        val chain = mock<Plugin.Chain>()
        val serverConfig = RudderServerConfig(
            isHosted = true,
            source = RudderServerConfig.RudderServerConfigSource(
                destinations = listOf(
                    RudderServerConfig.RudderServerDestination(
                        destinationId = "test",
                        destinationName = "TestDestination",
                        destinationDefinition = RudderServerConfig.RudderServerDestinationDefinition(
                            definitionName = "Firebase",
                        ),
                        isDestinationEnabled = true,
                        updatedAt = "2022-01-01",
                        destinationConfig = mapOf(
                            "eventFilteringOption" to "whitelistedEvents",
                            "whitelistedEvents" to setOf(
                                mapOf("eventName" to "test1"), mapOf(
                                    "eventName" to
                                            "test2",
                                ), mapOf("eventName" to "test3")
                            )
                        )
                    )
                )
            )
        )
        val demoMessage = TrackMessage.create(
            "test1", anonymousId = "anon-1", timestamp =
            Date().toString()
        )
        whenever(chain.message()).thenReturn(demoMessage)
        //destination plugin list
        whenever(chain.plugins).thenReturn(
            listOf(
                createDummyDestinationPluginWithName("Firebase"),
                createDummyDestinationPluginWithName("Mixpanel"),
            )
        )
        var chainCopy: Plugin.Chain? = null
        whenever(chain.with(anyList())).then {
            val list = it.arguments[0] as List<Plugin>
            CentralPluginChain(demoMessage, list, originalMessage = demoMessage).also { chainCopy = it }
        }
        // When
        eventFilteringPlugin.updateRudderServerConfig(serverConfig)

        eventFilteringPlugin.intercept(chain)

        // Then
        verify(chain, times(1)).with(anyOrNull())
        MatcherAssert.assertThat(
            chainCopy, allOf(
                org.hamcrest.Matchers.notNullValue(),
                org.hamcrest.Matchers.instanceOf(CentralPluginChain::class.java),
                org.hamcrest.Matchers.hasProperty(
                    "plugins",
                    allOf(
                        Matchers.iterableWithSize<Plugin>(2), Matchers.hasItem(
                            allOf(
                                Matchers.instanceOf<BaseDestinationPlugin<*>>
                                    (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Firebase"))
                            )
                        ), Matchers.hasItem(
                            allOf(
                                Matchers.instanceOf<BaseDestinationPlugin<*>>
                                    (BaseDestinationPlugin::class.java), hasProperty("name", equalTo("Mixpanel"))
                            )
                        )
                    )
                )
            )
        )
        //a copy of chain is made since filteredEventsMap is not empty
        verify(chain, never()).proceed(demoMessage)
    }

    private fun createDummyDestinationPluginWithName(name: String): DestinationPlugin<*> {
        return BaseDestinationPlugin<Any>(name) {
            it.proceed(it.message())
        }
    }
}
