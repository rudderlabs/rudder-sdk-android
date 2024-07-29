package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.core.internal.states.DestinationConfigState
import com.rudderstack.core.models.Message

/**
 * Must be added prior to destination plugins.
 * Will store messages till all factories are ready
 * After that reiterate the messages to the plugins
 */
internal class WakeupActionPlugin : Plugin {

    override lateinit var analytics: Analytics

    private val Analytics.destinationConfigState: DestinationConfigState?
        get() = retrieveState()

    override fun intercept(chain: Plugin.Chain): Message {
        val destinationConfig = analytics.destinationConfigState?.value

        val forwardChain =
            if (destinationConfig == null || !destinationConfig.allIntegrationsReady || analytics.storage.startupQueue.isNotEmpty()) {
                analytics.storage.saveStartupMessageInQueue(chain.message())
                //remove all destination plugins that are not ready, for others the message flow is normal
                val validPlugins = chain.plugins.toMutableList().filterNot {
                    it is DestinationPlugin<*> && destinationConfig?.isIntegrationReady(it.name) != true
                }
                chain.with(validPlugins)
            } else chain
        return forwardChain.proceed(chain.message())

    }
}
