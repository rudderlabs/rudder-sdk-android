/*
 * Creator: Debanjan Chatterjee on 28/12/21, 11:53 PM Last modified: 28/12/21, 11:53 PM
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

package com.rudderstack.core.internal

import com.rudderstack.core.Analytics
import com.rudderstack.core.Callback
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.Controller
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.DestinationConfig
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.LifecycleController
import com.rudderstack.core.Logger
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOptions
import com.rudderstack.core.Storage
import com.rudderstack.core.flushpolicy.CountBasedFlushPolicy
import com.rudderstack.core.flushpolicy.IntervalBasedFlushPolicy
import com.rudderstack.core.flushpolicy.addFlushPolicies
import com.rudderstack.core.flushpolicy.applyFlushPoliciesClosure
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.removeState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.core.internal.plugins.DestinationConfigurationPlugin
import com.rudderstack.core.internal.plugins.EventFilteringPlugin
import com.rudderstack.core.internal.plugins.GDPRPlugin
import com.rudderstack.core.internal.plugins.RudderOptionPlugin
import com.rudderstack.core.internal.plugins.StoragePlugin
import com.rudderstack.core.internal.plugins.WakeupActionPlugin
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.core.internal.states.DestinationConfigState
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.web.HttpResponse
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal class AnalyticsDelegate(
    configuration: Configuration,
    override val storage: Storage,
    override val instanceName: String,
    override val dataUploadService: DataUploadService,
    override val configDownloadService: ConfigDownloadService?,
    //optional
    private val initializationListener: ((success: Boolean, message: String?) -> Unit)? = null,
    //optional called if shutdown is called
    private val shutdownHook: (() -> Unit)? = null
) : Controller {

    companion object {

        //the number of flush calls that can be queued
        //unit sized queue means only one flush call can wait for the ongoing to complete
        private const val NUMBER_OF_FLUSH_CALLS_IN_QUEUE = 2
        private val PLUGIN_LOCK = Any()
    }

    private val _isShutDown = AtomicBoolean(false)

    //keep track of Analytics object
    private var _analytics: Analytics? = null

    /**
     * A handler for rejected tasks that discards the oldest unhandled request and then retries
     * execute, unless the executor is shut down, in which case the task is discarded.
     */
    private val handler: RejectedExecutionHandler = ThreadPoolExecutor.DiscardOldestPolicy()

    //used for flushing
    // a single threaded executor by default for sequentially calling flush one after other
    private val _flushExecutor = ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        LinkedBlockingQueue<Runnable>(NUMBER_OF_FLUSH_CALLS_IN_QUEUE),
        handler
    )


//    private val _commonContext = mapOf<String, String>(
//        "library" to (configuration.jsonAdapter.writeToJson(mapOf(
//            "name" to configuration.storage.libraryName,
//            "version" to configuration.storage.libraryVersion
//        ),
//            object : RudderTypeAdapter<Map<String, String>>() {}) ?: "")
//
//    )

    override val isShutdown
        get() = _isShutDown.get()
    override val logger: Logger
        get() = currentConfiguration?.logger ?: Logger.Noob

    override fun clearStorage() {
        storage.clearStorage()
    }

    override fun reset() {
        applyInfrastructureClosure {
            this.reset()
        }
        applyMessageClosure {
            this.reset()
        }
    }

    //message callbacks
    private var _callbacks = setOf<Callback>()

    private var _destinationPlugins: List<DestinationPlugin<*>> = listOf()

    //added before local message plugins
    private var _internalPreMessagePlugins: List<Plugin> = listOf()


    private var _customPlugins: List<Plugin> = listOf()

    private var _infrastructurePlugins: List<InfrastructurePlugin> = mutableListOf()

    //added after custom plugins
    private var _internalPostCustomPlugins: List<Plugin> = listOf()

    //Timeline plugins are associated throughout the lifecycle of SDK.
    private val _allTimelinePlugins
        get() = _internalPreMessagePlugins + _customPlugins + _internalPostCustomPlugins + _destinationPlugins


    //plugins
    private val gdprPlugin = GDPRPlugin()
    private val storagePlugin = StoragePlugin()
    private val wakeupActionPlugin = WakeupActionPlugin()
    private val eventFilteringPlugin = EventFilteringPlugin()
//        destConfigState = DestinationConfigState

    private val destinationConfigurationPlugin = DestinationConfigurationPlugin()

    private var _serverConfig: RudderServerConfig? = null

    init {
        associateState(ConfigurationsState(configuration))
        associateState(DestinationConfigState())
        initializePlugins()
        initializeFlush()
        if (configuration.shouldVerifySdk) {
            updateSourceConfig()
        } else {
            initializationListener?.invoke(true, null)
        }
    }

    private fun initializeFlush() {
        //these are the defaults
        addFlushPolicies(CountBasedFlushPolicy(), IntervalBasedFlushPolicy())
    }

    private fun initializePlugins() {
        initializeMessagePlugins()
        initializeInfraPlugins()
    }

    private fun initializeInfraPlugins() {
        addInfrastructurePlugin(storage)
        configDownloadService?.let {
            addInfrastructurePlugin(it)
        } ?: logger.warn(log = "ConfigDownloadService not set")
        addInfrastructurePlugin(dataUploadService)
    }


    override fun applyConfiguration(configurationScope: Configuration.() -> Configuration) {
        currentConfiguration?.let {
            val newConfiguration = configurationScope(it)
            currentConfigurationState?.update(newConfiguration)
            logger.debug(log = "Configuration updated: $newConfiguration")
            applyInfrastructureClosure {
                applyConfigurationClosure(this)
            }
            applyMessageClosure {
                applyConfigurationClosure(this)
            }
        }

    }

    override fun applyMessageClosure(closure: Plugin.() -> Unit) {
        synchronized(PLUGIN_LOCK) {
            _allTimelinePlugins.forEach {
                it.closure()
            }
        }
    }

    override fun applyInfrastructureClosure(closure: InfrastructurePlugin.() -> Unit) {
        synchronized(PLUGIN_LOCK) {
            _infrastructurePlugins.forEach {
                it.closure()
            }
        }
    }

    override fun optOut(optOut: Boolean) {
        storage.saveOptOut(optOut)
        applyConfiguration {
            copy(isOptOut = optOut)
        }
    }

    override val isOptedOut: Boolean
        get() = currentConfiguration?.isOptOut ?: storage.isOptedOut ?: false
    private val currentConfigurationState: ConfigurationsState?
        get() = retrieveState<ConfigurationsState>()
    private val currentDestinationConfigurationState: DestinationConfigState?
        get() = retrieveState<DestinationConfigState>().also {
            if (it == null) logger.error(log = "DestinationConfigState state not found")
        }
    override val currentConfiguration: Configuration?
        get() = currentConfigurationState?.value


    override fun addPlugin(vararg plugins: Plugin) {
        synchronized(PLUGIN_LOCK) {
            if (plugins.isEmpty()) return
            plugins.forEach {
                if (it is DestinationPlugin<*>) {
                    _destinationPlugins = _destinationPlugins + it
                    val newDestinationConfig =
                        currentDestinationConfigurationState?.value?.withIntegration(
                            it.name, it.isReady
                        ) ?: DestinationConfig(mapOf(it.name to it.isReady))
                    currentDestinationConfigurationState?.update(newDestinationConfig)
                    initDestinationPlugin(it)
                } else _customPlugins = _customPlugins + it
                //startup
                _analytics?.apply {
                    it.setup(this)
                }
                applyUpdateClosures(it)
            }
        }

    }

    override fun removePlugin(plugin: Plugin): Boolean {
        if (plugin is DestinationPlugin<*>) synchronized(PLUGIN_LOCK) {
            val destinationPluginPrevSize = _destinationPlugins.size
            _destinationPlugins = _destinationPlugins - plugin
            return _destinationPlugins.size < destinationPluginPrevSize
        }
        synchronized(PLUGIN_LOCK) {
            val customPluginPrevSize = _customPlugins.size
            _customPlugins = _customPlugins - plugin
            return (_customPlugins.size < customPluginPrevSize)
        }

    }

    override fun addInfrastructurePlugin(vararg plugins: InfrastructurePlugin) {
        val filteredPlugins = filterAcceptablePlugins(plugins)
        synchronized(PLUGIN_LOCK) {
            _infrastructurePlugins += filteredPlugins
        }
        filteredPlugins.forEach { plugin ->
            _analytics?.let {
                plugin.setup(it)
                applyUpdateClosures(plugin)
            }
        }
    }

    private fun filterAcceptablePlugins(plugins: Array<out InfrastructurePlugin>): List<InfrastructurePlugin> {
        val isConfigDownloadServiceAlreadySet =
            _infrastructurePlugins.any { it is ConfigDownloadService }
        if (!isConfigDownloadServiceAlreadySet) return plugins.toList()
        return plugins.filter {
            when (it) {
                is ConfigDownloadService -> false.also {
                    currentConfiguration?.logger?.warn(
                        log = "ConfigDownloadService already set. Dropping plugin $it"
                    )
                }

                else -> true
            }
        }
    }

    override fun removeInfrastructurePlugin(plugin: InfrastructurePlugin): Boolean {
        val prevSize = _infrastructurePlugins.size
        synchronized(PLUGIN_LOCK) {
            _infrastructurePlugins -= plugin
        }
        return _infrastructurePlugins.size < prevSize
    }


    override fun processMessage(
        message: Message, options: RudderOptions?, lifecycleController: LifecycleController?
    ) {
        if (isShutdown) {
            logger.warn(log = "Analytics has shut down, ignoring message $message")
            return
        }
        currentConfiguration?.analyticsExecutor?.execute {
            val lcc = lifecycleController ?: LifecycleControllerImpl(
                message, generatePluginsWithOptions(options)
            )
            lcc.process()

        }
    }

    private fun generatePluginsWithOptions(options: RudderOptions?): List<Plugin> {
        return synchronized(PLUGIN_LOCK) {
            _internalPreMessagePlugins + (options ?: currentConfiguration?.options
                                          ?: RudderOptions.defaultOptions()).createPlugin() + _customPlugins + _internalPostCustomPlugins + _destinationPlugins
        }.toList()
    }

    private fun RudderOptions.createPlugin(): Plugin {
        return RudderOptionPlugin(
            this
        ).also {
            it.setup(analytics = _analytics ?: return@also)
        }
    }

    override fun addCallback(callback: Callback) {
        _callbacks = _callbacks + callback
    }

    override fun removeCallback(callback: Callback) {
        _callbacks = _callbacks - callback
    }

    override fun removeAllCallbacks() {
        _callbacks = setOf()
    }

    override fun flush() {
        logger.info(log = "Flush called ${Exception()
            .stackTraceToString()}")
        if (isShutdown) return
        currentConfiguration?.let {
            forceFlush(_flushExecutor)
        }
    }
    // works even after shutdown

    private fun forceFlush(
        flushExecutor: ExecutorService, callback: ((Boolean) -> Unit)? = null
    ) {
        flushExecutor.submit {
            blockingFlush().let {
                callback?.invoke(it)
            }
        }
    }
    private val _isFlushing = AtomicBoolean(false)
    override fun blockingFlush(
    ): Boolean {
        if (_isShutDown.get()) return false
        if(!_isFlushing.compareAndSet(false, true)) return false
        //inform plugins
        broadcastFlush()
        var latestData = storage.getDataSync()

        var isFlushSuccess = true

        while (latestData.isNotEmpty()) {
            applyInfrastructureClosure {
                if (this is DataUploadService) {
                    val response = uploadSync(latestData, null)
                    if (response == null) {
                        isFlushSuccess = false
                        return@applyInfrastructureClosure
                    }
                    if (response.success) {
                        latestData.successCallback()
                        storage.deleteMessages(latestData)
                        latestData = storage.getDataSync()

                    } else {
                        latestData.failureCallback(response.errorThrowable)
                        isFlushSuccess = false
                    }
                }
            }
        }
        _isFlushing.set(false)
        return isFlushSuccess
    }

    private fun broadcastFlush() {
        applyMessageClosure {
            if (this is DestinationPlugin<*>) this.flush()
        }
        applyFlushPoliciesClosure {
            this.reschedule()
        }
    }

    override fun shutdown() {
        if (!_isShutDown.compareAndSet(false, true)) return
        logger.info(log = "shutdown")
        //inform plugins
        shutdownPlugins()

        storage.shutdown()
        currentConfiguration?.analyticsExecutor?.shutdown()
        removeState<ConfigurationsState>()
        removeState<DestinationConfigState>()
        _flushExecutor.shutdown()
        shutdownHook?.invoke()
    }

    private fun shutdownPlugins() {
        applyMessageClosure {
            onShutDown()
        }
        applyInfrastructureClosure {
            shutdown()
        }
    }


    private fun initDestinationPlugin(plugin: DestinationPlugin<*>) {

        val destConfig = currentDestinationConfigurationState?.value ?: DestinationConfig()
        fun onDestinationReady(isReady: Boolean) {
            if (isReady) {
                storage.startupQueue?.forEach {
                    // will be sent only for the individual destination.
                    //options need not be considered, since RudderOptionPlugin has
                    //already done it's job.
                    processMessage(
                        message = it,
                        null,
                        lifecycleController = LifecycleControllerImpl(it, listOf(plugin))
                    )
                }
                val newDestinationConfig = (currentDestinationConfigurationState?.value
                                            ?: DestinationConfig()).withIntegration(
                    plugin.name, isReady
                )
                currentDestinationConfigurationState?.update(newDestinationConfig)
                if (newDestinationConfig.allIntegrationsReady) {
                    //all integrations are ready, time to clear startup queue
                    storage.clearStartupQueue()
                }
            } else {
                logger.warn(log = "plugin ${plugin.name} activation failed")
                //remove from destination config, else all integrations ready won't be true anytime

                val newDestinationConfig = (currentDestinationConfigurationState?.value
                                            ?: DestinationConfig()).removeIntegration(plugin.name)
                currentDestinationConfigurationState?.update(newDestinationConfig)

            }
        }

        if (!destConfig.isIntegrationReady(plugin.name)) {

            plugin.addIsReadyCallback { _, isReady ->
                onDestinationReady(isReady)
            }
        } else {
            //destination is ready for startup queue
            onDestinationReady(true)
        }
    }

    private fun updateSourceConfig() {
        var isServerConfigDownloadPossible = false
        applyInfrastructureClosure {
            if (this is ConfigDownloadService) {
                currentConfiguration?.apply {
                    downloadServerConfig(
                        this@applyInfrastructureClosure, this
                    )
                    isServerConfigDownloadPossible = true
                }
            }
        }
        if (!isServerConfigDownloadPossible) {
            initializationListener?.invoke(
                false, "Config download service not set or " + "configuration not available"
            )
            logger.error(log = "Config Download Service Not Set or Configuration not available")
            shutdown()
            return
        }
    }

    private fun downloadServerConfig(
        configDownloadService: ConfigDownloadService, configuration: Configuration
    ) {
        configDownloadService.download { success, rudderServerConfig, lastErrorMsg ->
            configuration.analyticsExecutor.submit {
                if (success && rudderServerConfig != null && rudderServerConfig.source?.isSourceEnabled != false) {
                    initializationListener?.invoke(true, null)
                    handleConfigData(rudderServerConfig)
                } else {
                    val cachedConfig = storage.serverConfig
                    if (cachedConfig != null) {
                        initializationListener?.invoke(
                            true, "Downloading failed, using cached context"
                        )
                        logger.warn(log = "Downloading failed, using cached context")
                        handleConfigData(cachedConfig)
                    } else {
                        logger.error(log = "SDK Initialization failed due to $lastErrorMsg")
                        initializationListener?.invoke(
                            false, "Downloading failed, Shutting down $lastErrorMsg"
                        )
                        //log lastErrorMsg or isSourceEnabled
                        shutdown()
                    }
                }
            }
        }
    }

    private fun handleConfigData(serverConfig: RudderServerConfig) {
        storage.saveServerConfig(serverConfig)
        _serverConfig = serverConfig
        applyMessageClosure {
            applyServerConfigClosure(this)
        }
        applyInfrastructureClosure {
            applyServerConfigClosure(this)
        }

    }

    /**
     * pre destination plugins can be initialized before-hand
     *
     */
    private fun initializeMessagePlugins() {
        // check if opted out
        _internalPreMessagePlugins = _internalPreMessagePlugins + gdprPlugin
        // rudder option plugin followed by extract state plugin should be added by lifecycle
        // add defaults to message
//        _internalPrePlugins = _internalPrePlugins + anonymousIdPlugin

        // store for cloud destinations
//        _internalPostMessagePlugins = _internalPostMessagePlugins + fillDefaultsPlugin
//        _internalPostMessagePlugins = _internalPostMessagePlugins + extractStatePlugin

        _internalPostCustomPlugins = _internalPostCustomPlugins + destinationConfigurationPlugin
        _internalPostCustomPlugins = _internalPostCustomPlugins + wakeupActionPlugin
        _internalPostCustomPlugins = _internalPostCustomPlugins + eventFilteringPlugin
        _internalPostCustomPlugins = _internalPostCustomPlugins + storagePlugin

    }

    /**
     * Any initialization that requires [Analytics] is to be done here
     *
     * @param analytics
     */
    internal fun startup(analytics: Analytics) {
        messagePluginStartupClosure(analytics)
        infraPluginStartupClosure(analytics)
        _analytics = analytics
    }

    private fun infraPluginStartupClosure(analytics: Analytics) {
        applyInfrastructureClosure {
            setup(analytics)
        }
        applyInfrastructureClosure {
            applyUpdateClosures(this)
        }
    }

    private fun messagePluginStartupClosure(analytics: Analytics) {
        //apply startup closure
        applyMessageClosure {
            setup(analytics)
        }
        //if plugin update related configs are ready
        applyMessageClosure {
            applyUpdateClosures(this)
        }
    }

    private fun applyUpdateClosures(plugin: Plugin) {
        //apply setupClosures
        //config closure
        applyConfigurationClosure(plugin)
        //server config closure, if available
        applyServerConfigClosure(plugin)

    }

    private fun applyUpdateClosures(plugin: InfrastructurePlugin) {
        //apply setupClosures
        //config closure
        applyConfigurationClosure(plugin)
        //server config closure, if available
        applyServerConfigClosure(plugin)

    }

    private fun applyConfigurationClosure(plugin: Plugin) {
        currentConfiguration?.apply {
            plugin.updateConfiguration(this)
        }
    }

    private fun applyConfigurationClosure(plugin: InfrastructurePlugin) {
        currentConfiguration?.apply {
            plugin.updateConfiguration(this)
        }
    }

    private fun applyServerConfigClosure(plugin: InfrastructurePlugin) {
        _serverConfig?.apply {
            plugin.updateRudderServerConfig(this@apply)
        }
    }

    private fun applyServerConfigClosure(plugin: Plugin) {
        _serverConfig?.apply {
            plugin.updateRudderServerConfig(this@apply)
        }
    }

    //extensions
    private fun Iterable<Message>.successCallback() {
        if (_callbacks.isEmpty()) return
        forEach {
            _callbacks.forEach { callback ->
                callback.success(it)
            }
        }
    }

    private fun Iterable<Message>.failureCallback(throwable: Throwable) {
        if (_callbacks.isEmpty()) return
        forEach {
            _callbacks.forEach { callback ->
                callback.failure(it, throwable)
            }
        }
    }

    private val HttpResponse<*>.success: Boolean
        get() = status in (200..209)

    private val HttpResponse<*>.errorThrowable: Throwable
        get() = error ?: errorBody?.let {
            Exception(it)
        } ?: Exception("Internal error")


}