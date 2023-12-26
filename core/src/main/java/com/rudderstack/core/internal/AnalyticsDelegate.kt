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
import com.rudderstack.core.Controller
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.DestinationConfig
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.LifecycleController
import com.rudderstack.core.Logger
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOptions
import com.rudderstack.core.Configuration
import com.rudderstack.core.copy
import com.rudderstack.core.internal.plugins.DestinationConfigurationPlugin
import com.rudderstack.core.internal.plugins.EventFilteringPlugin
import com.rudderstack.core.internal.plugins.GDPRPlugin
import com.rudderstack.core.internal.plugins.RudderOptionPlugin
import com.rudderstack.core.internal.plugins.StoragePlugin
import com.rudderstack.core.internal.plugins.WakeupActionPlugin
import com.rudderstack.core.internal.states.DestinationConfigState
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpResponse
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal class AnalyticsDelegate(
    configuration: Configuration,
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


    private val _commonContext = mapOf<String, String>(
        "library" to (configuration.jsonAdapter.writeToJson(mapOf(
            "name" to configuration.storage.libraryName,
            "version" to configuration.storage.libraryVersion
        ),
            object : RudderTypeAdapter<Map<String, String>>() {}) ?: "")

    )

    override val isShutdown
        get() = _isShutDown.get()
    override val logger: Logger
        get() = currentConfiguration?.logger?: Logger.Noob

    override fun clearStorage() {
        currentConfiguration?.storage?.clearStorage()
    }

    override fun reset() {
        applyMessageClosure {
            this.reset()
        }
    }

    //message callbacks
    private var _callbacks = setOf<Callback>()
    private val flushScheduler = FlushScheduler(
        this::flush)

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
    private val wakeupActionPlugin = WakeupActionPlugin(
//        destConfigState = DestinationConfigState
    )
    private val destinationConfigurationPlugin = DestinationConfigurationPlugin()

    private var _serverConfig: RudderServerConfig? = null

    init {
        ConfigurationsState.update(configuration)
        ConfigurationsState.update(configuration)
        DestinationConfigState.update(DestinationConfig())
        initializePlugins()
        if (configuration.shouldVerifySdk) {
            updateSourceConfig()
        } else {
            initializationListener?.invoke(true, null)
        }
    }



    override fun applyConfiguration(configurationScope: Configuration.() -> Configuration) {
        currentConfiguration?.let{
            val newConfiguration = configurationScope(it)
            ConfigurationsState.update(newConfiguration)
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
        synchronized(PLUGIN_LOCK){
            _infrastructurePlugins.forEach {
                it.closure()
            }
        }
    }

    override fun optOut(optOut: Boolean) {
        currentConfiguration?.storage?.saveOptOut(optOut)
        applyConfiguration {
            copy(isOptOut = optOut)
        }
    }

    override val isOptedOut: Boolean
        get() = currentConfiguration?.isOptOut ?: currentConfiguration?.storage?.isOptedOut?:false
    override val currentConfiguration: Configuration?
        get() = ConfigurationsState.value


    override fun addPlugin(vararg plugins: Plugin) {
        synchronized(PLUGIN_LOCK) {
            if (plugins.isEmpty()) return
            plugins.forEach {
                if (it is DestinationPlugin<*>) {
                    _destinationPlugins = _destinationPlugins + it
                    val newDestinationConfig =
                        DestinationConfigState.value?.withIntegration(it.name, it.isReady)
                        ?: DestinationConfig(mapOf(it.name to it.isReady))
                    DestinationConfigState.update(newDestinationConfig)
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
        synchronized(PLUGIN_LOCK) {
            _infrastructurePlugins += plugins
        }
        applyInfrastructureClosure {
            _analytics?.let {
                setup(it)
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
                message,
                generatePluginsWithOptions(options)
            )
            lcc.process()

        }
    }

    private fun generatePluginsWithOptions(options: RudderOptions?): List<Plugin> {
        return synchronized(PLUGIN_LOCK) {
            _internalPreMessagePlugins + (options?:currentConfiguration?.options ?: RudderOptions
                .defaultOptions()).createPlugin() +
            _customPlugins +
            _internalPostCustomPlugins +
            _destinationPlugins
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

    internal fun flush() {
        logger.info(log = "Flush called")
        if (isShutdown) return
        currentConfiguration?.let {
            forceFlush(dataUploadService, _flushExecutor)
        }
    }
    // works even after shutdown

    internal fun forceFlush(
        alternateDataUploadService: DataUploadService,
        flushExecutor: ExecutorService,
        clearDb: Boolean = true,
        callback: ((Boolean) -> Unit)? = null
    ) {
        flushExecutor.submit {
            blockFlush(alternateDataUploadService, clearDb).let {
                callback?.invoke(it)
            }
        }
    }

    internal fun blockFlush(
        alternateDataUploadService: DataUploadService, clearDb: Boolean
    ): Boolean {
        if (_isShutDown.get()) return false
        //inform plugins
        applyMessageClosure {
            if (this is DestinationPlugin<*>) this.flush()
        }
        var offset = 0
        var latestData = currentConfiguration?.storage?.getDataSync()

        while (latestData?.isNotEmpty() == true) {
            val response = alternateDataUploadService.uploadSync(latestData, null)?:return false
            if (response.success) {
                latestData.successCallback()
                if (clearDb) {
                    currentConfiguration?.storage?.deleteMessages(latestData)
                } else {
                    offset += latestData.size
                }
                latestData = currentConfiguration?.storage?.getDataSync(offset)

            } else {
                latestData.failureCallback(response.errorThrowable)
                return false
            }
        }
        return true
    }

    override fun shutdown() {
        if (!_isShutDown.compareAndSet(false, true)) return
        logger.info(log = "shutdown")
        //inform plugins
        shutdownPlugins()

        //release memory
        flushScheduler.shutdown()
        currentConfiguration?.storage?.shutdown()
        dataUploadService.shutdown()
        currentConfiguration?.analyticsExecutor?.shutdown()
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

        val destConfig = DestinationConfigState.value ?: DestinationConfig()
        fun onDestinationReady(isReady: Boolean) {
            if (isReady) {
                currentConfiguration?.storage?.startupQueue?.forEach {
                    // will be sent only for the individual destination.
                    //options need not be considered, since RudderOptionPlugin has
                    //already done it's job.
                    processMessage(
                        message = it,
                        null,
                        lifecycleController = LifecycleControllerImpl(it, listOf(plugin))
                    )
                }
                val newDestinationConfig = (DestinationConfigState.value
                                            ?: DestinationConfig()).withIntegration(
                        plugin.name,
                        isReady
                    )
                DestinationConfigState.update(newDestinationConfig)
                if (newDestinationConfig.allIntegrationsReady) {
                    //all integrations are ready, time to clear startup queue
                    currentConfiguration?.storage?.clearStartupQueue()
                }
            } else {
                logger.warn(log = "plugin ${plugin.name} activation failed")
                //remove from destination config, else all integrations ready won't be true anytime

                val newDestinationConfig = (DestinationConfigState.value
                                            ?: DestinationConfig()).removeIntegration(plugin.name)
                DestinationConfigState.update(newDestinationConfig)

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
        if (configDownloadService == null) {
            initializationListener?.invoke(false, "Config download service not set")
            logger.error(log = "Config Download Service Not Set")
            shutdown()
            return
        }
        currentConfiguration?.let { configuration ->
            //configDownloadService is non-null
            configDownloadService.download(
                currentConfiguration?.storage?.libraryName?:"",
                currentConfiguration?.storage?.libraryVersion?:"",
                currentConfiguration?.storage?.libraryOsVersion?:"",
                configuration.sdkVerifyRetryStrategy
            ) { success, rudderServerConfig, lastErrorMsg ->
                configuration.analyticsExecutor.submit {
                    if (success && rudderServerConfig != null && rudderServerConfig.source?.isSourceEnabled != false) {
                        initializationListener?.invoke(true, null)
                        handleConfigData(rudderServerConfig)
                    } else {
                        val cachedConfig = currentConfiguration?.storage?.serverConfig
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
    }

    private fun handleConfigData(serverConfig: RudderServerConfig) {
        currentConfiguration?.storage?.saveServerConfig(serverConfig)
        _serverConfig = serverConfig
        applyMessageClosure {
            applyServerConfigClosure(this)
        }

    }

    /**
     * pre destination plugins can be initialized before-hand
     *
     */
    private fun initializePlugins() {
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
        _internalPostCustomPlugins = _internalPostCustomPlugins + EventFilteringPlugin
        _internalPostCustomPlugins = _internalPostCustomPlugins + storagePlugin

    }

    /**
     * Any initialization that requires [Analytics] is to be done here
     *
     * @param analytics
     */
    internal fun startup(analytics: Analytics) {
        //apply startup closure
        applyMessageClosure {
            setup(analytics)
        }
        //if plugin update related configs are ready
        applyMessageClosure {
            applyUpdateClosures(this)
        }
        _analytics = analytics
    }

    private fun applyUpdateClosures(plugin: Plugin) {
        //apply setupClosures

        //server config closure, if available
        applyServerConfigClosure(plugin)
        //settings closure
        applyConfigurationClosure(plugin)
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

    private fun applyServerConfigClosure(plugin: Plugin) {
        _serverConfig?.apply {
            plugin. updateRudderServerConfig(this@apply)
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