package com.rudderstack.core

import com.rudderstack.core.models.Message
import com.rudderstack.core.models.RudderServerConfig

/**
 * Observes, modifies, and potentially short-circuits requests going out and the corresponding
 * responses coming back in. Typically plugins transforms or logs the data sent over to destinations.
 *
 */
interface Plugin {

    var analytics: Analytics

    fun setup(analytics: Analytics) {
        this.analytics = analytics
    }

    /**
     * Joins ("chains") the plugins for a particular message
     *
     */
    interface Chain {
        /**
         * Returns the message the Chain is associated to.
         *
         * @return Message
         */
        fun message(): Message

        /**
         * This behaves differently for destination plugins. For message
         * plugins, this is the original message that was passed to the chain.
         * For, Destination plugins this is prior to being copied and
         * intercepted by Sub Plugins, but will reflect the changes
         * made by other plugins.
         * [Chain.proceed] should be called with this message so as to discard any
         * alteration to the message by [DestinationPlugin]
         */
        val originalMessage: Message

        /**
         * Indicates that processing of this plugin is over and now the message is ready to be taken forward.
         * If changes made to the message object is local to plugin and is not intended to be moved forward,
         * then the old message should be proceeded. In these cases, it is advised to create a deep copy
         * of message. Otherwise changes made in Maps and Lists might be propagated through, unintentionally.
         *
         * In case the change is intended to be forwarded, proceed with the changed message.
         *
         * @param message The message to be processed
         * @return The processed message that will be provided to later interceptors.
         */
        fun proceed(message: Message): Message

        /**
         * Get the list of plugins this Chain operates on
         *
         * @return the set of plugins that this Chain operates on
         */
        val plugins: List<Plugin>

        /**
         * Index of the plugin that is being operated.
         * Generally if called from inside a plugin, this denotes the index of the plugin
         */
        val index: Int

        /**
         * Create a copy of [Chain] with updated set of plugins.
         *
         * @param plugins
         */
        fun with(plugins: List<Plugin>): Chain

    }
    /*companion object {
        */
    /**
     * Constructs an interceptor for a lambda. This compact syntax is most useful for inline
     * interceptors.
     *
     * ```kotlin
     * val plugin = Plugin { chain: Plugin.Chain ->
     *     chain.proceed(chain.request())
     * }
     * ```
     *//*
        inline operator fun invoke(crossinline block: (chain: Chain) -> Message): Plugin =
            Plugin { block(it) }
    }*/
    fun intercept(chain: Chain): Message

    /**
     * Called when settings is updated
     *
     * @param configuration [Configuration] globally set for the sdk
     */
    fun updateConfiguration(configuration: Configuration) {}

    fun updateRudderServerConfig(config: RudderServerConfig) {}

    /**
     * Called when shutDOwn is triggered in [Analytics]
     * refactor to shutdown
     */
    fun onShutDown() {}

    /**
     * Called when reset is triggered in Analytics
     *
     */
    fun reset() {}
}
