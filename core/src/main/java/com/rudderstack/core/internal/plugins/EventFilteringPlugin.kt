package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.RudderServerConfig
import com.rudderstack.core.models.TrackMessage
import java.util.concurrent.ConcurrentHashMap

/**
 * Event filtering for device mode destinations. Filters out blacklisted or whitelisted events in
 * source config
 *
 */
private const val DISABLE = "disable"
private const val WHITELISTED_EVENTS = "whitelistedEvents"
private const val BLACKLISTED_EVENTS = "blacklistedEvents"
private const val EVENT_FILTERING_OPTION = "eventFilteringOption"
private const val EVENT_NAME = "eventName"

class EventFilteringPlugin : Plugin {

    override lateinit var analytics: Analytics

    //map of (destination definition name, DestinationEventFilteringConfig)
    private var filteredEventsMap = ConcurrentHashMap<String, DestinationEventFilteringConfig>()

    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message()
        return if (filteredEventsMap.isEmpty()) {
            chain.proceed(msg)
        } else {
            chain.with(chain.plugins.filterNot {
                it is DestinationPlugin<*> && msg is TrackMessage &&
                        filteredEventsMap[it.name]?.let { config ->
                            when (config.status) {
                                WHITELISTED_EVENTS -> !config.listedEvents.contains(msg.eventName)
                                BLACKLISTED_EVENTS -> config.listedEvents.contains(msg.eventName)
                                else -> false
                            }
                        } ?: false
            }).proceed(msg)
        }
    }

    override fun updateRudderServerConfig(config: RudderServerConfig) {
        super.updateRudderServerConfig(config)
        config.source?.destinations?.apply {
            cacheFilteredEvents(this)
        }
    }

    private fun cacheFilteredEvents(destinations: List<RudderServerConfig.RudderServerDestination?>) {
        if (destinations.isNotEmpty()) {
            // Iterate all destinations
            for (destination in destinations) {
                val destinationDefinitionName =
                    destination?.destinationDefinition?.definitionName ?: continue
                val eventFilteringStatus =
                    destination.destinationConfig[EVENT_FILTERING_OPTION] as? String ?: DISABLE
                val destinationFilterConfig = DestinationEventFilteringConfig(eventFilteringStatus,
                    eventFilteringStatus.takeIf { it != DISABLE }
                        ?.let { destination.destinationConfig[it] as? Collection<Map<String, String>>? }
                        ?.mapNotNull {
                            it[EVENT_NAME]
                        }?.toHashSet() ?: setOf()
                )
                filteredEventsMap[destinationDefinitionName] = destinationFilterConfig
            }
        }
    }

    override fun onShutDown() {
        super.onShutDown()
        filteredEventsMap.clear()
    }

    private data class DestinationEventFilteringConfig(
        val status: String,
        val listedEvents: Set<String>
    )
}
