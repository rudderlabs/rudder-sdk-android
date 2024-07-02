package com.rudderstack.core

import com.rudderstack.core.models.RudderServerConfig

/**
 * Download config for SDK.
 * Config aids in usage of device mode plugins.
 * Do not add this plugin to Analytics using [Analytics.addInfrastructurePlugin] method.
 * This [InfrastructurePlugin] should be sent as constructor params to [Analytics] instance.
 *
 */
interface ConfigDownloadService : InfrastructurePlugin {
    /**
     * Fetches the config from the server
     */
    fun download(
        callback: (success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit
    )

    /**
     * These listeners are attached with an optional replay argument.
     * replay specifies how many old events will be broadcasted to the listener.
     * Making it 0 will make the listener listen to only future downloads.
     *
     */
    fun addListener(listener: Listener, replay: Int)
    fun removeListener(listener: Listener)

    @FunctionalInterface
    fun interface Listener{
        fun onDownloaded(success: Boolean)
    }
}
