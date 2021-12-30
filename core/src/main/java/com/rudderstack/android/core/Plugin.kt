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

package com.rudderstack.android.core

import com.rudderstack.android.models.Message
import com.rudderstack.android.models.RudderServerConfig

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
    }
    /*companion object {
        */
    /**
     * Constructs an interceptor for a lambda. This compact syntax is most useful for inline
     * interceptors.
     *
     * ```kotlin
     * val interceptor = Interceptor { chain: Interceptor.Chain ->
     *     chain.proceed(chain.request())
     * }
     * ```
     *//*
        inline operator fun invoke(crossinline block: (chain: Chain) -> Message): Plugin =
            Plugin { block(it) }
    }*/
    fun intercept(chain: Chain): Message

    /**
     * To add a sub plugin to a main plugin.
     * The order will be
     * internal-plugins -->sub-plugin (main-plugin-1) -> main-plugin-1 -> ... ->cloud plugin->device mode-plugin
     *
     * @param plugin A plugin object.
     * @see PluginInterceptor
     */
    fun addSubPlugin(plugin: PluginInterceptor){}

    /**
     * Called when settings is updated
     *
     * @param settings [Settings] globally set for the sdk
     */
    fun updateSettings(settings: Settings){}

    fun updateRudderServerConfig(config: RudderServerConfig){}
    /**
     * Marker Interface for sub-plugins that can be added to each individual plugin.
     * This is to discourage developers to intentionally/unintentionally adding main plugins as sub
     * plugins.
     *
     * Sub-plugins are those which intercepts the data prior to it reaches the main plugin code.
     * These plugins only act on the main-plugin that it is added to.
     *
     */
    interface PluginInterceptor : Plugin
}