package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.core.internal.states.DestinationConfigState
import com.rudderstack.core.models.Message
import java.lang.ref.WeakReference

/**
 * Must be added prior to destination plugins.
 * Will store messages till all factories are ready
 * After that reiterate the messages to the plugins
 */
internal class WakeupActionPlugin(
//    private val destConfigState: State<DestinationConfig> = DestinationConfigState
) : Plugin {

    private var _analytics: WeakReference<Analytics?> = WeakReference(null)
    override fun setup(analytics: Analytics) {
        _analytics = WeakReference(analytics)
    }

    private val storage
        get() = _analytics.get()?.storage
    private val Analytics.destinationConfigState: DestinationConfigState?
        get() = retrieveState()

    override fun intercept(chain: Plugin.Chain): Message {
        val destinationConfig = _analytics.get()?.destinationConfigState?.value

        val forwardChain =
            if (destinationConfig == null || !destinationConfig.allIntegrationsReady || storage?.startupQueue?.isNotEmpty() == true) {
                storage?.saveStartupMessageInQueue(chain.message())
                //remove all destination plugins that are not ready, for others the message flow is normal
                val validPlugins = chain.plugins.toMutableList().filterNot {
                    it is DestinationPlugin<*> && destinationConfig?.isIntegrationReady(it.name) != true
                }
                chain.with(validPlugins)
            } else chain
        return forwardChain.proceed(chain.message())

    }

    override fun onShutDown() {
        _analytics = WeakReference(null)
    }
}
