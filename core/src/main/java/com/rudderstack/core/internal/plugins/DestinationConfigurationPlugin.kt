package com.rudderstack.core.internal.plugins

import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.RudderServerConfig

/**
 * Enables or disables destination plugins based on server config.
 * There can be three cases.
 * In case there is no device mode plugins, there will be no checking for Destination configuration.
 *
 * In case device mode plugins are present but no destination configuration, destination plugins will
 * be removed (though this scenario is very hard to produce, since as device mode plugins are required to
 * setup when server config is available), else it is responsibility of other plugins to do the same.
 *
 * In case device mode destination plugins are present along with server config, all destination
 * plugins that are disabled in destination config are filtered out.
 */
internal class DestinationConfigurationPlugin : Plugin {
    private var _notAllowedDestinations : Set<String> = setOf()
    private var _isConfigUpdated = false
    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message()
        val validPlugins = chain.plugins.filter {
            //either not a destination plugin or is allowed
            it !is DestinationPlugin<*> || (_isConfigUpdated && it.name !in _notAllowedDestinations)
        }
        return  if (validPlugins.isNotEmpty()) {
            return chain.with(validPlugins).proceed(msg)
        } else
            chain.proceed(msg)
    }

    override fun updateRudderServerConfig(config: RudderServerConfig) {
        _isConfigUpdated = true
        _notAllowedDestinations = config.source?.destinations?.filter {
            !it.isDestinationEnabled
        }?.map {
            it.destinationDefinition?.definitionName?:it.destinationName?: ""
        }?.toHashSet()?: setOf()
    }
}
