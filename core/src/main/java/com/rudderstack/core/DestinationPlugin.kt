package com.rudderstack.core

import com.rudderstack.core.DestinationPlugin.DestinationInterceptor
import com.rudderstack.core.models.Message

/**
 * Implement [BaseDestinationPlugin] instead to avoid writing boiler plate code
 * Destination Plugins are those plugin extensions that are used to send data to device mode
 * destinations.
 * They are typical [Plugin], with the following differences.
 * [Plugin.intercept] is called with a copy of the original message.
 * [DestinationInterceptor] works on this copied message.
 * But when [Plugin.Chain.proceed] is called, internally the copied message is replaced by the
 * original message thus discarding all the changes DestinationPlugin and it's sub-plugins did on the
 * message.
 * This is to safeguard against changes for one destination being propagated to other destination.
 * For propagating changes in Message, use general plugin.
 * @param T The Destination type associated to this Plugin
 */
interface DestinationPlugin<T> : Plugin {
    /**
     * Destination definition name as given in control plane
     */
    val name: String

    /**
     * Returns whether this plugin is ready to accept events.
     * Closely linked to [onReadyCallbacks]
     */
    val isReady: Boolean
    val subPlugins: List<DestinationInterceptor>
    val onReadyCallbacks: List<(T?, Boolean) -> Unit>
//    get() = ArrayList(field)

    /**
     * To add a sub plugin to a main plugin.
     * The order will be
     * internal-plugins --> custom-plugins -> ... ->cloud plugin->sub-plugins of device-mode-1 ->
     * device mode-plugin-1 --> sub-plugins of device-mode-2 --> device-mode-plugin-2 ....
     *
     * @param plugin A plugin object.
     * @see DestinationInterceptor
     */
    fun addSubPlugin(plugin: DestinationInterceptor)

    /**
     * Called when the device destination is ready to accept requests
     *
     * @param callbackOnReady called with the destination specific class object and
     * true or false depending on initialization success or failure
     */
    fun addIsReadyCallback(callbackOnReady: (T?, isUsable: Boolean) -> Unit)

    /**
     * Marker Interface for sub-plugins that can be added to each individual plugin.
     * This is to discourage developers to intentionally/unintentionally adding main plugins as sub
     * plugins.
     *
     * Sub-plugins are those which intercepts the data prior to it reaches the main plugin code.
     * These plugins only act on the main-plugin that it is added to.
     *
     */
    interface DestinationInterceptor : Plugin

    /**
     * Called when flush is triggered.
     *
     */
    fun flush() {}
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
abstract class BaseDestinationPlugin<T>(override val name: String) : DestinationPlugin<T> {
    private var _isReady = false
    private var _subPlugins = listOf<DestinationPlugin.DestinationInterceptor>()
    private var _onReadyCallbacks = listOf<(T?, Boolean) -> Unit>()

    override val subPlugins: List<DestinationPlugin.DestinationInterceptor>
        get() = _subPlugins

    override val onReadyCallbacks: List<(T?, Boolean) -> Unit>
        get() = _onReadyCallbacks

    override val isReady: Boolean
        get() = _isReady


    override fun addSubPlugin(plugin: DestinationPlugin.DestinationInterceptor) {
        _subPlugins = _subPlugins + plugin
    }

    override fun addIsReadyCallback(callbackOnReady: (T?, isUsable: Boolean) -> Unit) {
        _onReadyCallbacks = _onReadyCallbacks + callbackOnReady
    }

    fun setReady(isReady: Boolean, destinationInstance: T? = null) {
        _isReady = isReady
        _onReadyCallbacks.forEach {
            it.invoke(destinationInstance, isReady)
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
        inline operator fun <T> invoke(
            name: String,
            crossinline block: (chain: Plugin.Chain) -> Message
        ): BaseDestinationPlugin<T> =
            object : BaseDestinationPlugin<T>(name) {
                override lateinit var analytics: Analytics
                override fun intercept(chain: Plugin.Chain): Message {
                    return block(chain)
                }
            }
    }
}
