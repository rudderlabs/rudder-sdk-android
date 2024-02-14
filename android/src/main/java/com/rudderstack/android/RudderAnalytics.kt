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
import com.rudderstack.android.internal.infrastructure.AnonymousIdHeaderPlugin
import com.rudderstack.android.internal.infrastructure.LifecycleObserverPlugin
import com.rudderstack.android.internal.infrastructure.ResetImplementationPlugin
import com.rudderstack.android.internal.plugins.PlatformInputsPlugin
import com.rudderstack.android.internal.plugins.ExtractStatePlugin
import com.rudderstack.android.internal.plugins.FillDefaultsPlugin
import com.rudderstack.android.internal.plugins.SessionPlugin
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.android.utilities.initializeSessionManagement
import com.rudderstack.android.utilities.shutdownSessionManagement
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.DEFAULTS_ANALYTICS_INSTANCE_NAME
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.models.MessageContext

//device info and stuff
//multi process
//bt stuff
//tv,
//work manager
fun RudderAnalytics(
    writeKey: String,
    configuration: ConfigurationAndroid,
    instanceName: String = DEFAULTS_ANALYTICS_INSTANCE_NAME,
    dataUploadService: DataUploadService? = null,
    configDownloadService: ConfigDownloadService? = null,
    storage: AndroidStorage = AndroidStorageImpl(
        configuration.application,
        instanceName = instanceName,
        useContentProvider = ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER
    ),
    initializationListener: ((success: Boolean, message: String?) -> Unit)? = null
): Analytics {
    return Analytics(
        writeKey,
        configuration,
        instanceName,
        dataUploadService,
        configDownloadService,
        storage,
        initializationListener = initializationListener,
        shutdownHook = {
            onShutdown()
        }
    ).apply {
        startup()
    }
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
 * Anonymous id to be used for all consecutive calls.
 * Anonymous id is mostly used for messages sent prior to user identification or in case of
 * anonymous usage.
 *
 * @param anonymousId String to be used as anonymousId
 */
fun Analytics.setAnonymousId(anonymousId: String) {
    androidStorage.setAnonymousId(anonymousId)
    applyConfiguration {
        if (this is ConfigurationAndroid) copy(
            anonymousId = anonymousId
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
        AnonymousIdHeaderPlugin(),
        LifecycleObserverPlugin(),
        ActivityBroadcasterPlugin(),
        ResetImplementationPlugin()
    )
private val messagePlugins
    get() = listOf(
        PlatformInputsPlugin(),
        ExtractStatePlugin(),
        FillDefaultsPlugin(),
        SessionPlugin()
    )

private fun Analytics.startup() {
    addPlugins()
    associateStates()
    androidStorage.let {
        contextState?.update(it.context)
    }

    initializeSessionManagement()
}

private fun Analytics.associateStates() {
    associateState(ContextState())
    associateState(UserSessionState())
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


val Analytics.currentConfigurationAndroid: ConfigurationAndroid?
    get() = (currentConfiguration as? ConfigurationAndroid)
private fun Analytics.onShutdown() {
    shutdownSessionManagement()
}


