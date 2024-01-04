/*
 * Creator: Debanjan Chatterjee on 23/12/21, 4:26 PM Last modified: 23/12/21, 4:09 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.core

import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig

/**
 * Observes, modifies, and potentially short-circuits requests going out and the corresponding
 * responses coming back in. Typically plugins transforms or logs the data sent over to destinations.
 *
 */
fun interface Plugin {
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
         * Available only for DestinationPlugins
         * For other plugins, this is same as [message]
         * Destination chain contains the original message, that is prior to being copied and
         * intercepted by Sub Plugins.
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
     * Setup code for this plugin
     * Helps in changing settings, etc.
     * @param analytics The analytics object this plugin is added to.
     */
    fun setup(analytics: Analytics) {}

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
    fun onShutDown(){}

    /**
     * Called when reset is triggered in Analytics
     *
     */
    fun reset(){}


}