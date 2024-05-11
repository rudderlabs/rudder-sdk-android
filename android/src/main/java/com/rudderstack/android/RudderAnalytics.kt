/*
 * Creator: Debanjan Chatterjee on 26/04/22, 3:08 PM Last modified: 26/04/22, 3:08 PM
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
@file:JvmName("RudderAnalytics") @file:Suppress("FunctionName")

package com.rudderstack.android

import com.rudderstack.android.internal.infrastructure.ActivityBroadcasterPlugin
import com.rudderstack.android.internal.infrastructure.AppInstallUpdateTrackerPlugin
import com.rudderstack.android.internal.infrastructure.CreateAnonymousIdPlugin
import com.rudderstack.android.internal.infrastructure.LifecycleObserverPlugin
import com.rudderstack.android.internal.infrastructure.ResetImplementationPlugin
import com.rudderstack.android.internal.plugins.ExtractStatePlugin
import com.rudderstack.android.internal.plugins.FillDefaultsPlugin
import com.rudderstack.android.internal.plugins.PlatformInputsPlugin
import com.rudderstack.android.internal.infrastructure.ReinstatePlugin
import com.rudderstack.android.internal.plugins.SessionPlugin
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.android.utilities.shutdownSessionManagement
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.models.MessageContext
import com.rudderstack.models.createContext
import com.rudderstack.models.traits
import com.rudderstack.models.updateWith

//device info and stuff
//multi process
//bt stuff
//tv,
//work manager
private fun RudderAnalytics(
    writeKey: String,
    configuration: ConfigurationAndroid,
    dataUploadService: DataUploadService? = null,
    configDownloadService: ConfigDownloadService? = null,
    storage: AndroidStorage = AndroidStorageImpl(
        configuration.application,
        writeKey = writeKey,
        useContentProvider = ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER
    ),
    initializationListener: ((success: Boolean, message: String?) -> Unit)? = null
): Analytics {
    return Analytics(writeKey,
        configuration,
        dataUploadService,
        configDownloadService,
        storage,
        initializationListener = initializationListener,
        shutdownHook = {
            onShutdown()
        }).apply {
        startup()
    }
}

@JvmOverloads
fun createInstance(
    writeKey: String,
    configuration: ConfigurationAndroid,
    dataUploadService: DataUploadService? = null,
    configDownloadService: ConfigDownloadService? = null,
    storage: AndroidStorage = AndroidStorageImpl(
        configuration.application,
        writeKey = writeKey,
        useContentProvider = ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER
    ),
    initializationListener: ((success: Boolean, message: String?) -> Unit)? = null
): Analytics {
    return AnalyticsRegistry.getInstance(writeKey)
        ?: RudderAnalytics(
            writeKey,
            configuration,
            dataUploadService,
            configDownloadService,
            storage,
            initializationListener
        ).also { analyticsInstance ->
            AnalyticsRegistry.register(writeKey, analyticsInstance)
        }
}

fun getInstance(writeKey: String): Analytics? {
    return AnalyticsRegistry.getInstance(writeKey)
}

internal val Analytics.contextState: ContextState?
    get() = retrieveState<ContextState>()
val Analytics.androidStorage: AndroidStorage
    get() = (storage as AndroidStorage)

/**
 * Set the AdvertisingId yourself. If set, SDK will not capture idfa automatically
 *
 * @param advertisingId IDFA for the device
 */
fun Analytics.putAdvertisingId(advertisingId: String) {

    applyConfiguration {
        if (this is ConfigurationAndroid) copy(
            advertisingId = advertisingId
        )
        else this
    }
}

/**
 * Set the push token for the device to be passed to the downstream destinations
 *
 * @param deviceToken Push Token from FCM
 */
fun Analytics.putDeviceToken(deviceToken: String) {
    applyConfiguration {
        if (this is ConfigurationAndroid) copy(
            deviceToken = deviceToken
        )
        else this
    }
}

/**
 * Setting the [ConfigurationAndroid.userId] explicitly.
 *
 * @param userId String to be used as userId
 */
fun Analytics.setUserId(userId: String) {
    androidStorage.setUserId(userId)
    applyConfiguration {
        if (this is ConfigurationAndroid) copy(
            userId = userId
        )
        else this
    }
}

private val infrastructurePlugins
    get() = arrayOf(
        CreateAnonymousIdPlugin(),
        ReinstatePlugin(),
        AppInstallUpdateTrackerPlugin(),
        LifecycleObserverPlugin(),
        ActivityBroadcasterPlugin(),
        ResetImplementationPlugin()
    )
private val messagePlugins
    get() = listOf(
        ExtractStatePlugin(), FillDefaultsPlugin(), PlatformInputsPlugin(),
        SessionPlugin()
    )

private fun Analytics.startup() {
    addPlugins()
    associateStates()
}


private fun Analytics.associateStates() {
    associateState(ContextState())
    attachSavedContextIfAvailable()
    associateState(UserSessionState())
}

private fun Analytics.attachSavedContextIfAvailable() {
    androidStorage.context?.let {
        processNewContext(it)
    }
}

private fun Analytics.addPlugins() {
    addInfrastructurePlugin(*infrastructurePlugins)
    addPlugin(*messagePlugins.toTypedArray())
}

internal fun Analytics.processNewContext(
    newContext: MessageContext
) {
    androidStorage.cacheContext(newContext)
    contextState?.update(newContext)
}

fun Analytics.applyConfigurationAndroid(
    androidConfigurationScope: ConfigurationAndroid.() ->
    ConfigurationAndroid
) {
    applyConfiguration {
        if (this is ConfigurationAndroid) androidConfigurationScope()
        else this
    }
}

val Analytics.currentConfigurationAndroid: ConfigurationAndroid?
    get() = (currentConfiguration as? ConfigurationAndroid)

private fun Analytics.onShutdown() {
    shutdownSessionManagement()
}


