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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.*
import com.rudderstack.android.core.internal.plugins.*
import com.rudderstack.android.core.internal.states.ContextState
import com.rudderstack.android.core.internal.states.DestinationConfigState
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.Message
import com.rudderstack.android.models.MessageContext
import com.rudderstack.android.models.RudderServerConfig
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService

internal class AnalyticsDelegate(
    settings: Settings,
    storage: Storage,
    private val defaultOptions: RudderOptions,
//    controlPlaneUrl: String,
    jsonAdapter: JsonAdapter,
    // implies if source config be downloaded. must for using device mode
    shouldVerifySdk: Boolean,
    //retry strategy to verify sdk in case shouldVerifySdk is true
    private val sdkVerifyRetryStrategy: RetryStrategy,
    private val dataUploadService: DataUploadService,
    //can be null only if shouldVerifySdk is false
    private val configDownloadService: ConfigDownloadService?,
    private val analyticsExecutor: ExecutorService,
    private val logger: Logger,
    context: MessageContext,
) : Controller {


    companion object {
        private const val PROPERTIES_FILE_NAME = "config.properties"
        private const val LIB_KEY_NAME = "libraryName"
        private const val LIB_KEY_VERSION = "rudderCoreSdkVersion"
        private const val LIB_KEY_PLATFORM = "platform"
        private const val LIB_KEY_OS_VERSION = "os_version"
    }

    private val libDetails: Map<String, String> by lazy {
        try {
            Properties().let {
                it.load(FileInputStream(PROPERTIES_FILE_NAME))
                mapOf(
                    LIB_KEY_NAME to it.getProperty(LIB_KEY_NAME),
                    LIB_KEY_VERSION to it.getProperty(LIB_KEY_VERSION),
                    LIB_KEY_PLATFORM to it.getProperty(LIB_KEY_PLATFORM),
                    LIB_KEY_OS_VERSION to it.getProperty(LIB_KEY_OS_VERSION)
                )
            }
        } catch (ex: IOException) {
            logger.error(log = "Config fetch error", throwable = ex)
            mapOf()
        }

    }
    private val _commonContext = mapOf<String, String>(
        "library" to (jsonAdapter.writeToJson(
            libDetails.filter {
                it.key in arrayOf(AnalyticsDelegate.LIB_KEY_NAME, AnalyticsDelegate.LIB_KEY_VERSION)
            }.map {
                when (it.key) {
                    AnalyticsDelegate.LIB_KEY_NAME -> "name" to it.value
                    AnalyticsDelegate.LIB_KEY_VERSION -> "version" to it.value
                    else -> it.toPair()
                }
            }.toMap(),
            object : RudderTypeAdapter<Map<String, String>>() {}) ?: "")

    )


    private val _storageDecorator =
        StorageDecorator(storage, SettingsState, this::onStorageDataChange)

    private var _customPlugins: List<Plugin> = listOf()
    private var _destinationPlugins: List<DestinationPlugin<*>> = listOf()

    // added prior to custom plugins
    private var _internalPrePlugins: List<Plugin> = listOf()

    //added after custom plugins
    private var _internalPostPlugins: List<Plugin> = listOf()

    private val _allPlugins
        get() = _internalPrePlugins + _customPlugins + _internalPostPlugins + _destinationPlugins


    //plugins
    private val gdprPlugin = GDPRPlugin()
    private val storagePlugin = StoragePlugin(_storageDecorator)
    private val fillDefaultsPlugin: FillDefaultsPlugin
    private val wakeupActionPlugin = WakeupActionPlugin(
        _storageDecorator,
        destConfigState = DestinationConfigState
    )
    private val destinationConfigurationPlugin = DestinationConfigurationPlugin()

    private var _serverConfig: RudderServerConfig? = null

    init {
        SettingsState.update(settings)
        DestinationConfigState.update(DestinationConfig())
        ContextState.update(context)
        initializePlugins()
        if (shouldVerifySdk) {
            updateSourceConfig()
        }
        fillDefaultsPlugin = FillDefaultsPlugin(
            _commonContext,
            SettingsState, ContextState
        )
    }


    override fun applySettings(settings: Settings) {
        SettingsState.update(settings)
        applyClosure {
            applySettingsClosure(this)
        }
    }

    override fun applyClosure(closure: Plugin.() -> Unit) {
        _allPlugins.forEach {
            it.closure()
        }
    }

    override fun setAnonymousId(anonymousId: String) {
        applySettings(
            SettingsState.value?.copy(anonymousId = anonymousId)
                ?: Settings(anonymousId = anonymousId)
        )

    }

    override fun optOut(optOut: Boolean) {
        applySettings(SettingsState.value?.copy(isOptOut = optOut) ?: Settings(isOptOut = optOut))
    }

    override val isOptedOut: Boolean
        get() = SettingsState.value?.isOptOut ?: Settings().isOptOut

    /*override fun putAdvertisingId(advertisingId: String) {
        _storageDecorator.cacheContext(_storageDecorator.context + ("advertisingId" to advertisingId))
    }

    override fun putDeviceToken(token: String) {
        TODO("Not yet implemented")
    }*/

    override fun addPlugin(vararg plugins: Plugin) {

        if (plugins.isEmpty()) return
        plugins.forEach {
            if (it is DestinationPlugin<*>) {
                _destinationPlugins = _destinationPlugins + it
                val newDestinationConfig =
                    DestinationConfigState.value?.withIntegration(it.name, it.isReady)
                        ?: DestinationConfig(mapOf(it.name to it.isReady))
                DestinationConfigState.update(newDestinationConfig)
                initDestinationPlugin(it)
            } else
                _customPlugins = _customPlugins + it

            applyUpdateClosures(it)
        }

    }


    override fun processMessage(
        message: Message,
        options: RudderOptions?,
        lifecycleController: LifecycleController?
    ) {
        val lcc = lifecycleController ?: LifecycleControllerImpl(message,
            _allPlugins.toMutableList().also {
                //after gdpr plugin
                it.add(1, RudderOptionPlugin(options ?: defaultOptions))
                //after option plugin
                it.add(
                    2, ExtractStatePlugin(
                        ContextState, SettingsState, options ?: defaultOptions,
                        _storageDecorator
                    )
                )
            })
        lcc.process()

    }

    override fun shutdown() {
        //release memory
        _storageDecorator.shutdown()
        dataUploadService.shutdown()
        analyticsExecutor.shutdown()
    }

//    @Throws(MissingPropertiesException::class)
//    override fun <T : Message> T.withDefaults(): T


    private fun initDestinationPlugin(plugin: DestinationPlugin<*>) {

        val destConfig = DestinationConfigState.value ?: DestinationConfig()
        fun onDestinationReady(isReady: Boolean) {
            if (isReady) {
                _storageDecorator.startupQueue.forEach {
                    // will be sent only for the individual destination.
                    //options need not be considered, since RudderOptionPlugin has
                    //already done it's job.
                    processMessage(
                        message = it, null, lifecycleController =
                        LifecycleControllerImpl(it, listOf(plugin))
                    )
                }
                val newDestinationConfig =
                    (DestinationConfigState.value ?: DestinationConfig()).let {
                        it.withIntegration(plugin.name, isReady)
                    }
                DestinationConfigState.update(newDestinationConfig)
                if (newDestinationConfig.allIntegrationsReady) {
                    //all integrations are ready, time to clear startup queue
                    _storageDecorator.clearStartupQueue()
                }
            } else {
                logger.warn(log = "plugin ${plugin.name} activation failed")
                //remove from destination config, else all integrations ready won't be true anytime

                val newDestinationConfig =
                    (DestinationConfigState.value ?: DestinationConfig()).let {
                        it.removeIntegration(plugin.name)
                    }
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
        check(configDownloadService != null) {
            "Config Download Service Not Set"
        }
        //configDownloadService is non-null

        configDownloadService.download(
            libDetails.getOrDefault(LIB_KEY_NAME, ""),
            libDetails.getOrDefault(LIB_KEY_VERSION, ""),
            libDetails.getOrDefault(LIB_KEY_OS_VERSION, ""),
            sdkVerifyRetryStrategy
        ) { success, rudderServerConfig, lastErrorMsg ->
            analyticsExecutor.submit {
                if (success && rudderServerConfig != null && rudderServerConfig.source?.isSourceEnabled != false) {
                    handleConfigData(rudderServerConfig)
                } else {
                    val cachedConfig = _storageDecorator.serverConfig
                    if (cachedConfig != null)
                        handleConfigData(cachedConfig)
                    else {
                        logger.error(log = "SDK Initialization failed due to $lastErrorMsg")
                        //log lastErrorMsg or isSourceEnabled
                        shutdown()
                    }
                }
            }
        }
    }

    private fun handleConfigData(serverConfig: RudderServerConfig) {
        _storageDecorator.saveServerConfig(serverConfig)
        _serverConfig = serverConfig
        applyClosure {
            applyServerConfigClosure(this)
        }

    }

    /**
     * pre destination plugins can be initialized before-hand
     *
     */
    private fun initializePlugins() {
        // check if opted out
        _internalPrePlugins = _internalPrePlugins + gdprPlugin
        // rudder option plugin followed by extract state plugin should be added by lifecycle

        // add defaults to message
//        _internalPrePlugins = _internalPrePlugins + anonymousIdPlugin
        // store for cloud destinations
        _internalPrePlugins = _internalPrePlugins + fillDefaultsPlugin
        _internalPrePlugins = _internalPrePlugins + storagePlugin

        _internalPostPlugins + _internalPostPlugins + destinationConfigurationPlugin
        _internalPostPlugins = _internalPostPlugins + wakeupActionPlugin
        //if plugin update related configs are ready
        applyClosure {
            applyUpdateClosures(this)
        }
    }

    private fun onStorageDataChange(data: List<Message>) {
        if (data.isEmpty() || _serverConfig == null) return // in case server config is
        // not yet downloaded

        //post the data
        dataUploadService.upload(data, _commonContext) {
            if (it) {
                _storageDecorator.deleteMessages(data)
            }
        }


    }

    private fun applyUpdateClosures(plugin: Plugin) {
        //server config closure, if available
        applyServerConfigClosure(plugin)
        //settings closure
        applySettingsClosure(plugin)
    }

    private fun applySettingsClosure(plugin: Plugin) {
        SettingsState.value?.apply {
            plugin.updateSettings(this)
        }
    }

    private fun applyServerConfigClosure(plugin: Plugin) {
        _serverConfig?.apply {
            plugin.updateRudderServerConfig(this@apply)
        }
    }
}