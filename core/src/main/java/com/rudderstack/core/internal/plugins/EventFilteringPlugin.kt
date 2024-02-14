/*
 * Creator: Debanjan Chatterjee on 13/08/22, 12:04 AM Last modified: 13/08/22, 12:04 AM
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

import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.models.TrackMessage
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