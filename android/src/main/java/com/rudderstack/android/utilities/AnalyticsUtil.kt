@file:JvmName("AnalyticsUtil") @file:Suppress("FunctionName")

package com.rudderstack.android.utilities

import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.internal.infrastructure.ActivityBroadcasterPlugin
import com.rudderstack.android.internal.infrastructure.AnonymousIdHeaderPlugin
import com.rudderstack.android.internal.infrastructure.AppInstallUpdateTrackerPlugin
import com.rudderstack.android.internal.infrastructure.LifecycleObserverPlugin
import com.rudderstack.android.internal.infrastructure.ReinstatePlugin
import com.rudderstack.android.internal.infrastructure.ResetImplementationPlugin
import com.rudderstack.android.internal.plugins.ExtractStatePlugin
import com.rudderstack.android.internal.plugins.FillDefaultsPlugin
import com.rudderstack.android.internal.plugins.PlatformInputsPlugin
import com.rudderstack.android.internal.plugins.SessionPlugin
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Analytics
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.models.MessageContext
import com.rudderstack.models.createContext
import com.rudderstack.models.traits
import com.rudderstack.models.updateWith

val Analytics.currentConfigurationAndroid: ConfigurationAndroid?
    get() = (currentConfiguration as? ConfigurationAndroid)

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

// By changing to `internal` we are restricting the user from calling this API
internal fun Analytics.setAnonymousId(anonymousId: String) {
    androidStorage.setAnonymousId(anonymousId)
    applyConfiguration {
        if (this is ConfigurationAndroid) copy(
            anonymousId = anonymousId
        )
        else this
    }
    val anonymousIdPair = ("anonymousId" to anonymousId)
    val newContext = contextState?.value?.let {
        it.updateWith(traits = (it.traits ?: mapOf()) + anonymousIdPair)
    } ?: createContext(traits = mapOf(anonymousIdPair))
    processNewContext(newContext)
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

private val infrastructurePlugins = arrayOf(
    ReinstatePlugin(),
    AnonymousIdHeaderPlugin(),
    AppInstallUpdateTrackerPlugin(),
    LifecycleObserverPlugin(),
    ActivityBroadcasterPlugin(),
    ResetImplementationPlugin()
)

private val messagePlugins = listOf(
    ExtractStatePlugin(),
    FillDefaultsPlugin(),
    PlatformInputsPlugin(),
    SessionPlugin()
)

internal fun Analytics.startup() {
    addPlugins()
    associateStates()
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

internal fun Analytics.processNewContext(
    newContext: MessageContext
) {
    androidStorage.cacheContext(newContext)
    contextState?.update(newContext)
}

internal fun Analytics.onShutdown() {
    shutdownSessionManagement()
}
private fun Analytics.addPlugins() {
    addInfrastructurePlugin(*infrastructurePlugins)
    addPlugin(*messagePlugins.toTypedArray())
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
