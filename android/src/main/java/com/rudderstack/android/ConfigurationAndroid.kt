/*
 * Creator: Debanjan Chatterjee on 28/11/23, 5:37 pm Last modified: 28/11/23, 10:00 am
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

package com.rudderstack.android

import android.app.Application
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.core.Base64Generator
import com.rudderstack.core.Configuration
import com.rudderstack.core.Logger
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.RudderOptions
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.models.externalIds
import com.rudderstack.models.traits
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ConfigurationAndroid(
    val application: Application,
    jsonAdapter: JsonAdapter,
    var anonymousId: String= AndroidUtils.getDeviceId(
        application
    ),
    var userId: String? = null,
    options: RudderOptions = RudderOptions.default(),
    flushQueueSize: Int = Defaults.DEFAULT_FLUSH_QUEUE_SIZE,
    maxFlushInterval: Long = Defaults.DEFAULT_MAX_FLUSH_INTERVAL,
    isOptOut: Boolean = false,
    shouldVerifySdk: Boolean = Defaults.SHOULD_VERIFY_SDK,
    sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
    dataPlaneUrl: String? = null, //defaults to https://hosted.rudderlabs.com
    controlPlaneUrl: String? = null, //defaults to https://api.rudderlabs.com/
    val trackLifecycleEvents: Boolean = Defaults.TRACK_LIFECYCLE_EVENTS,
    val recordScreenViews: Boolean = Defaults.RECORD_SCREEN_VIEWS,
    val isPeriodicFlushEnabled: Boolean = Defaults.IS_PERIODIC_FLUSH_ENABLED,
    val autoCollectAdvertId: Boolean = Defaults.AUTO_COLLECT_ADVERT_ID,
    val multiProcessEnabled: Boolean = Defaults.MULTI_PROCESS_ENABLED,
    val defaultProcessName: String?= Defaults.DEFAULT_PROCESS_NAME,
    val useContentProvider: Boolean = Defaults.USE_CONTENT_PROVIDER,
    var advertisingId: String? = null,
    var deviceToken: String? = null,
    logger: Logger = AndroidLogger,
    override val storage: AndroidStorage = AndroidStorageImpl(application, jsonAdapter,
        useContentProvider,
        logger),
    analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
    networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
    val advertisingIdFetchExecutor : ExecutorService? = null,
    base64Generator: Base64Generator = AndroidUtils.defaultBase64Generator(),
//    val defaultTraits: IdentifyTraits? = null, // will be added by default to each message
//    val defaultExternalIds: List<Map<String, String>>? = null, // will be added by default to each message
//    val defaultContextMap: Map<String, Any>? = null, // will be added by default to each message
//    val contextAddOns: Map<String, Any>? = null // will be added by default to each message
) : com.rudderstack.core.Configuration by Configuration(
    jsonAdapter,
    options,
    flushQueueSize,
    maxFlushInterval,
    isOptOut,
    shouldVerifySdk,
    sdkVerifyRetryStrategy,
    dataPlaneUrl,
    controlPlaneUrl,
    logger,
    storage,
    analyticsExecutor,
    networkExecutor,
    base64Generator,
){
    fun copy(
        jsonAdapter: JsonAdapter = this.jsonAdapter,
        options: RudderOptions = this.options,
        flushQueueSize: Int = this.flushQueueSize,
        maxFlushInterval: Long = this.maxFlushInterval,
        isOptOut: Boolean = this.isOptOut,
        shouldVerifySdk: Boolean = this.shouldVerifySdk,
        sdkVerifyRetryStrategy: RetryStrategy = this.sdkVerifyRetryStrategy,
        dataPlaneUrl: String = this.dataPlaneUrl,
        controlPlaneUrl: String?= this.controlPlaneUrl,
        logger: Logger = this.logger,
        storage: AndroidStorage = this.storage,
        analyticsExecutor: ExecutorService = this.analyticsExecutor,
        networkExecutor: ExecutorService = this.networkExecutor,
        advertisingIdFetchExecutor : ExecutorService? = this.advertisingIdFetchExecutor,
        base64Generator: Base64Generator = this.base64Generator,
//        defaultTraits: IdentifyTraits? = this.defaultTraits,
//        defaultExternalIds: List<Map<String, String>>? = this.defaultExternalIds,
//        defaultContextMap: Map<String, Any>? = this.defaultContextMap,
//        contextAddOns: Map<String, Any>? = this.contextAddOns,
        anonymousId: String = this.anonymousId,
        userId: String? = this.userId,
        trackLifecycleEvents: Boolean = this.trackLifecycleEvents,
        recordScreenViews: Boolean = this.recordScreenViews,
        isPeriodicFlushEnabled: Boolean = this.isPeriodicFlushEnabled,
        autoCollectAdvertId: Boolean = this.autoCollectAdvertId,
        multiProcessEnabled: Boolean = this.multiProcessEnabled,
        defaultProcessName: String?= this.defaultProcessName,
        useContentProvider: Boolean = this.useContentProvider,
        advertisingId: String? = this.advertisingId,
        deviceToken: String? = this.deviceToken
    ) = ConfigurationAndroid(
        application,
        jsonAdapter,
        anonymousId,
        userId,
        options,
        flushQueueSize,
        maxFlushInterval,
        isOptOut,
        shouldVerifySdk,
        sdkVerifyRetryStrategy,
        dataPlaneUrl,
        controlPlaneUrl,
        trackLifecycleEvents,
        recordScreenViews,
        isPeriodicFlushEnabled,
        autoCollectAdvertId,
        multiProcessEnabled,
        defaultProcessName,
        useContentProvider,
        advertisingId,
        deviceToken,
        logger,
        storage,
        analyticsExecutor,
        networkExecutor,
        advertisingIdFetchExecutor,
        base64Generator,
//        defaultTraits,
//        defaultExternalIds,
//        defaultContextMap,
//        contextAddOns
    )

    private object Defaults{
        const val SHOULD_VERIFY_SDK: Boolean = true
        const val TRACK_LIFECYCLE_EVENTS = true
        const val RECORD_SCREEN_VIEWS = true
        const val IS_PERIODIC_FLUSH_ENABLED = false
        const val AUTO_COLLECT_ADVERT_ID = true
        const val MULTI_PROCESS_ENABLED = false
        val DEFAULT_PROCESS_NAME: String? = null
        const val USE_CONTENT_PROVIDER = false
        const val DEFAULT_FLUSH_QUEUE_SIZE = 30
        const val DEFAULT_MAX_FLUSH_INTERVAL = 10 * 1000L
    }
}