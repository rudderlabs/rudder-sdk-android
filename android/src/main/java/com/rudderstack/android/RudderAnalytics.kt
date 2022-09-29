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
@file:Suppress("FunctionName")

package com.rudderstack.android

import android.app.Application
import android.util.Base64
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.internal.LifecycleObserver
import com.rudderstack.android.internal.RudderPreferenceManager
import com.rudderstack.android.internal.plugins.AndroidContextPlugin
import com.rudderstack.android.internal.sync.RudderWorkerConfig
import com.rudderstack.android.internal.sync.registerWorkManager
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.*
import com.rudderstack.models.*
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.*

//device info and stuff
//multi process
//bt stuff
//tv,
//work manager
fun RudderAnalytics(
    application: Application,
    writeKey: String,
    settings: Settings,
    jsonAdapter: JsonAdapter,
    shouldVerifySdk: Boolean = true,
    sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
    dataPlaneUrl: String? = null,
    controlPlaneUrl: String? = null,
    logger: Logger = AndroidLogger,
    defaultTraits: IdentifyTraits? = null,
    defaultExternalIds: List<Map<String, String>>? = null,
    defaultContextMap: Map<String, Any>? = null,
    useContentProvider: Boolean = false,
    isPeriodicFlushEnabled: Boolean = false,
    trackLifecycleEvents: Boolean = false,
    autoCollectAdvertId: Boolean = false,
    recordScreenViews: Boolean = false,
    multiProcessEnabled: Boolean = false,
    defaultProcessName: String? = null,
    initializationListener: ((success: Boolean, message: String?) -> Unit)? = null
): Analytics {
    initialize(application)
    val storage = AndroidStorage(application, jsonAdapter, useContentProvider, logger)
    return Analytics(
        writeKey,
        settings.takeIf { it.anonymousId != null }
            ?: settings.copy(anonymousId = AndroidUtils.getDeviceId(application)),
        jsonAdapter,
        shouldVerifySdk,
        sdkVerifyRetryStrategy,

        dataPlaneUrl,
        controlPlaneUrl,
        logger,
        storage,
        base64Generator = Base64Generator {
            Base64.encodeToString(
                String.format(Locale.US, "%s:", it).toByteArray(charset("UTF-8")),
                Base64.DEFAULT
            )
        },
        defaultTraits = defaultTraits,
        defaultExternalIds = defaultExternalIds,
        initializationListener = initializationListener,
        defaultContextMap = defaultContextMap,
        shutdownHook = ::shutdown
    ).apply {
        startup(
            application,
            jsonAdapter,
            isPeriodicFlushEnabled,
            trackLifecycleEvents,
            autoCollectAdvertId,
            recordScreenViews,
            writeKey, dataPlaneUrl, controlPlaneUrl, logger,
            if (multiProcessEnabled) defaultProcessName else null
        )
    }

}

//android specific properties
private var androidContextPlugin: AndroidContextPlugin? = null
private var lifecycleObserver: LifecycleObserver? = null

/**
 * Set the AdvertisingId yourself. If set, SDK will not capture idfa automatically
 *
 * @param advertisingId IDFA for the device
 */
fun Analytics.putAdvertisingId(advertisingId: String) {

    // sets into context plugin
    androidContextPlugin?.setAdvertisingId(advertisingId) ?: logger.warn(
        log = "Analytics not initialized. Setting advertising id failed"
    )
}

/**
 * Set the push token for the device to be passed to the downstream destinations
 *
 * @param deviceToken Push Token from FCM
 */
fun Analytics.putDeviceToken(deviceToken: String) {

    //set device token in context plugin
    androidContextPlugin?.putDeviceToken(deviceToken) ?: logger.warn(
        log = "Analytics not initialized. Setting device token failed"
    )
}

private fun initialize(application: Application) {
    RudderPreferenceManager.initialize(application)
}

private fun Analytics.startup(
    application: Application,
    jsonAdapter: JsonAdapter,
    isPeriodicFlushEnabled: Boolean,
    trackLifecycleEvents: Boolean,
    autoCollectAdvertId: Boolean,
    recordScreenViews: Boolean,
    writeKey: String,
    dataPlaneUrl: String?,
    controlPlaneUrl: String?,
    logger: Logger,
    defaultProcessName: String? = null
) {
    //lifecycle observer
    lifecycleObserver = LifecycleObserver(
        application,
        trackLifecycleEvents,
        recordScreenViews, ::send
    )

    //add android context
    androidContextPlugin =
        AndroidContextPlugin(
            application,
            autoCollectAdvertId,
            analyticsExecutor,
            jsonAdapter
        ).also {
            addPlugin(it)
        }
    if (isPeriodicFlushEnabled)
        application.registerWorkManager(
            this, RudderWorkerConfig(
                writeKey, defaultProcessName != null,
                dataPlaneUrl, controlPlaneUrl, jsonAdapter, logger, defaultProcessName
            )
        )
}

private fun Analytics.send(message: Message) {
    when (message) {
        is AliasMessage -> alias(message)
        is GroupMessage -> group(message)
        is IdentifyMessage -> identify(message)
        is PageMessage -> {
            /**not supported in mobile**/
            logger.warn(log = "Page Message is not supported")
        }
        is ScreenMessage -> screen(message)
        is TrackMessage -> track(message)
    }
}

private fun shutdown() {
    androidContextPlugin = null
    lifecycleObserver?.shutdown()
    lifecycleObserver = null
}


