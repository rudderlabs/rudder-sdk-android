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
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.internal.RudderPreferenceManager
import com.rudderstack.android.internal.plugins.AndroidContextPlugin
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Analytics
import com.rudderstack.core.Logger
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.Settings
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.rudderjsonadapter.JsonAdapter

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
    initializationListener: ((success: Boolean, message: String?) -> Unit)? = null
): Analytics {
    initialize(application)
    val storage = AndroidStorage(application, jsonAdapter, useContentProvider, logger)
    return Analytics(
        writeKey,
        settings,
        jsonAdapter,
        shouldVerifySdk,
        sdkVerifyRetryStrategy,
        dataPlaneUrl,
        controlPlaneUrl,
        logger,
        storage,
        defaultTraits = defaultTraits,
        defaultExternalIds = defaultExternalIds,
        initializationListener = initializationListener,
        defaultContextMap = defaultContextMap
    )

}

//android specific properties
private var androidContextPlugin: AndroidContextPlugin? = null

/**
 * Set the AdvertisingId yourself. If set, SDK will not capture idfa automatically
 *
 * @param advertisingId IDFA for the device
 */
fun Analytics.putAdvertisingId(advertisingId: String) {
    // sets into context plugin
    androidContextPlugin?.setAdvertisingId(advertisingId)?:AndroidLogger.warn(
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
    androidContextPlugin?.putDeviceToken(deviceToken)?:AndroidLogger.warn(
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
    recordScreenViews: Boolean
) {
    androidContextPlugin =
        AndroidContextPlugin(application, autoCollectAdvertId, analyticsExecutor, jsonAdapter).also {
            addPlugin(it)
        }


}

