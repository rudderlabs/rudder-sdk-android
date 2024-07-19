package com.rudderstack.core

import com.rudderstack.core.models.Message

/**
 * Handles all messages, assorting the plugins, keeping track of cache, to name a few of it's
 * duties
 *
 */
interface Controller {
    /**
     * Update the [Configuration] object to be used for all subsequent calls
     *
     * @param configurationScope Update the current configuration with this scope to
     * return the updated configuration
     */
    fun applyConfiguration(configurationScope: Configuration.() -> Configuration)

    /**
     * Applies a closure method to all available Plugins
     * Can break the system if not properly constructed.
     * Check for the plugin type and apply only to plugins that seem necessary
     *
     * @param closure A method to be run on each plugin
     */
    fun applyMessageClosure(closure : Plugin.() -> Unit)
    /**
     * Applies a closure method to all available [InfrastructurePlugin]
     * Can break the system if not properly constructed.
     *
     * @param closure A method to be run on each [InfrastructurePlugin]
     */
    fun applyInfrastructureClosure(closure : InfrastructurePlugin.() -> Unit)


    /**
     * Opt out from analytics and usage monitoring. No further data will be sent once set true
     * @param optOut True to stop analytics data collection, false otherwise
     */
    fun optOut(optOut : Boolean)

    /**
     * Intended to be called by other Rudderstack Modules. Not meant for standard SDK usage.
     */
    fun updateSourceConfig()

    /**
     * Is opted out from analytics
     */
    val isOptedOut : Boolean

    val currentConfiguration : Configuration?
    val storage:Storage

    val dataUploadService:DataUploadService
    val configDownloadService:ConfigDownloadService?

    /**
     * The write key
     * In case of multiple instances, this key is used to differentiate between them
     */
    val writeKey: String

    fun addPlugin(vararg plugins: Plugin)
    /**
     * Custom plugins to be removed.
     *
     * @param plugin  [Plugin] object
     * @return true if successfully removed false otherwise
     */
    fun removePlugin(plugin: Plugin) : Boolean

    fun addInfrastructurePlugin(vararg plugins: InfrastructurePlugin)
    /**
     * Infrastructure plugins to be removed.
     *
     * @param plugin  [InfrastructurePlugin] object
     * @return true if successfully removed false otherwise
     */
    fun removeInfrastructurePlugin(plugin: InfrastructurePlugin) : Boolean
    /**
     * Submit a [Message] for processing.
     * The message is taken up by the controller and it passes through the set of timelines defined.
     * Refrain from using this unless you are sure about it. Use other utility methods for
     * [Analytics.track], [Analytics.screen], [Analytics.identify], [Analytics.alias], [Analytics.group]
     * In case of external ids, custom contexts and integrations passed in message as well as in options,
     * the ones in options will replace those of message.
     *
     * @param message A [Message] object up for submission
     * @param options Individual [RudderOption] for this message. Only applicable for this message
     * @param lifecycleController LifeCycleController related to this message, null for default implementation
     */
    fun processMessage(message: Message, options: RudderOption?, lifecycleController: LifecycleController? = null)

    /**
     * Add a [Callback] for getting notified when a message is processed
     *
     * @param callback An object of [Callback]
     */
    fun addCallback(callback: Callback)

    /**
     * Removes an added [Callback]
     * @see addCallback
     *
     * @param callback The callback to be removed
     */
    fun removeCallback(callback: Callback)

    /**
     * Removes all added [Callback]
     * @see addCallback
     *
     */
    fun removeAllCallbacks()
    /**
     * Flush the remaining data from storage.
     * However flush returns immediately if  analytics is shutdown
     */
    fun flush()
    /**
     * This blocks the thread till events are flushed.
     * Users should prefer [flush]
     *
     */
    fun blockingFlush() : Boolean
    //fun reset()
    /**
     * Shuts down the Analytics. Once shutdown, a new instance needs to be created.
     * All executors and plugins to be shutdown.
     * This isn't an instant operation. It might take some time to complete.
     * Executors will finish executing the jobs they have taken
     *
     */
    fun shutdown()

    /**
     * true if [shutdown] is called, false otherwise
     */
    val isShutdown : Boolean

    /**
     * The logger set upfront or default [Logger]
     */
    val logger : Logger

    /**
     * clears the storage of all data
     *
     */
    fun clearStorage()

    /**
     * Resets the device mode destinations. Resets any traits and external ids attached to context
     *
     */
    fun reset()

}
