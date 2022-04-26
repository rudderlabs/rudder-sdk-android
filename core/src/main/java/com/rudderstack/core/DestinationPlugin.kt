/*
 * Creator: Debanjan Chatterjee on 02/01/22, 11:12 AM Last modified: 02/01/22, 11:12 AM
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

package com.rudderstack.core

import com.rudderstack.models.Message

/**
 * Implement [BaseDestinationPlugin] instead to avoid writing boiler plate code
 * Destination Plugins are those plugin extensions that are used to send data to device mode
 * destinations.
 * They are typical [Plugin], with the following differences.
 * [Plugin.intercept] is called with a copy of the original message.
 * [PluginInterceptor] works on this copied message.
 * But when [Plugin.Chain.proceed] is called, internally the copied message is replaced by the
 * original message thus discarding all the changes DestinationPlugin and it's sub-plugins did on the
 * message.
 * This is to safeguard against changes for one destination being propagated to other destination.
 * For propagating changes in Message, use general plugin.
 * @param T The Destination type associated to this Plugin
 */
interface DestinationPlugin<T> : Plugin {
    /**
     * An unique name for Destination plugin
     */
    val name : String

    /**
     * Returns whether this plugin is ready to accept events.
     * Closely linked to [onReadyCallbacks]
     */
    val isReady : Boolean
    val subPlugins: List<PluginInterceptor>
    val onReadyCallbacks: List<(T?, Boolean) -> Unit>
//    get() = ArrayList(field)

    /**
     * To add a sub plugin to a main plugin.
     * The order will be
     * internal-plugins --> custom-plugins -> ... ->cloud plugin->sub-plugins of device-mode-1 ->
     * device mode-plugin-1 --> sub-plugins of device-mode-2 --> device-mode-plugin-2 ....
     *
     * @param plugin A plugin object.
     * @see PluginInterceptor
     */
    fun addSubPlugin(plugin: PluginInterceptor)

    /**
     * Called when the device destination is ready to accept requests
     *
     * @param callbackOnReady called with the destination specific class object and
     * true or false depending on initialization success or failure
     */
    fun addIsReadyCallback(callbackOnReady : (T?, isUsable: Boolean) -> Unit)
    /**
     * Marker Interface for sub-plugins that can be added to each individual plugin.
     * This is to discourage developers to intentionally/unintentionally adding main plugins as sub
     * plugins.
     *
     * Sub-plugins are those which intercepts the data prior to it reaches the main plugin code.
     * These plugins only act on the main-plugin that it is added to.
     *
     */
    fun interface PluginInterceptor : Plugin
}

/**
 * [DestinationPlugin] with filled in boiler plate code.
 * Preferred way of constructing [DestinationPlugin] is by sub-classing it.
 * By default [isReady] is false
 * Call [setReady] when initialised
 *
 * @param T type of destination in [Plugin]
 * @property name Name of plugin, used to filter plugins
 */
abstract class BaseDestinationPlugin<T>(override val name: String) : DestinationPlugin<T>{
    private var _isReady = false
    private var _subPlugins = listOf<DestinationPlugin.PluginInterceptor>()
    private var _onReadyCallbacks = listOf<(T?, Boolean) -> Unit>()

    override val subPlugins: List<DestinationPlugin.PluginInterceptor>
        get() = _subPlugins

    override val onReadyCallbacks: List<(T?, Boolean) -> Unit>
        get() = _onReadyCallbacks

    override val isReady: Boolean
        get() = _isReady


    override fun addSubPlugin(plugin: DestinationPlugin.PluginInterceptor) {
        _subPlugins = _subPlugins + plugin
    }

    override fun addIsReadyCallback(callbackOnReady: (T?, isUsable: Boolean) -> Unit) {
        _onReadyCallbacks = _onReadyCallbacks+ callbackOnReady
    }

    fun setReady(isReady : Boolean, destinationInstance : T? = null){
        _isReady = isReady
        _onReadyCallbacks.forEach {
            it.invoke( destinationInstance, isReady)
        }
    }

    companion object {

        /**
         * Constructs an destination for a lambda. This compact syntax is most useful for inline
         * interceptors.
         * By default isReady is true if creating instance this way
         * Used for simple destinations.
         * By default [isReady] is false
         * Call [setReady] when initialised
         * ```kotlin
         * val plugin = DestinationPlugin("name") { chain: Plugin.Chain ->
         *     chain.proceed(chain.request())
         * }
         * ```
         */
        inline operator fun<T> invoke(name: String,  crossinline block: (chain: Plugin.Chain) -> Message): BaseDestinationPlugin<T> =
            object : BaseDestinationPlugin<T>(name) {
                override fun intercept(chain: Plugin.Chain): Message {
                    return block(chain)
                }
            }
    }
}