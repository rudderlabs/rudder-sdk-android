/*
 * Creator: Debanjan Chatterjee on 30/12/21, 1:26 PM Last modified: 29/12/21, 5:30 PM
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

package com.rudderstack.core

import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Settings to be applied to the sdk.
 * To change the settings, create a copy and apply.
 * ```
 * val newSettings = settings.copy(options = settings.options.newBuilder().withExternalIds(mapOf()).build(),
 *  trackLifecycleEvents = true)
 * ```
 * [defaultTraits] and [defaultExternalIds] are stored in [ContextState] but not in [Storage]
 *
 * @property options Global [RudderOptions] for all plugins
 * @property flushQueueSize Max elements to be stored before a flush. Once it passes this threshold,
 * flush is triggered
 * @property maxFlushInterval Max time (in millis) to wait for a flush, even if the queue size hasn't
 * passed the threshold
// * @property trackLifecycleEvents Will track activity lifecycle if set to true, else false
// * @property recordScreenViews Will record screen views if true.
 * @property isOptOut GDPR implementation. Data won't be sent if GDPR is true
 */
interface Configuration {
    val jsonAdapter: JsonAdapter
    val options: RudderOptions
    val flushQueueSize: Int
    val maxFlushInterval: Long
    val isOptOut: Boolean
    val shouldVerifySdk: Boolean
    val sdkVerifyRetryStrategy: RetryStrategy
    val dataPlaneUrl: String
    val controlPlaneUrl: String
    val logger: Logger
    val storage: Storage
    val analyticsExecutor: ExecutorService
    val networkExecutor: ExecutorService
    val base64Generator: Base64Generator


    companion object {
        // default flush queue size for the events to be flushed to server
        const val FLUSH_QUEUE_SIZE = 30

        // default timeout for event flush
        // if events are registered and flushQueueSize is not reached
        // events will be flushed to server after maxFlushInterval millis
        const val MAX_FLUSH_INTERVAL = 10 * 1000L //10 seconds
        operator fun invoke(
            jsonAdapter: JsonAdapter,
            options: RudderOptions = RudderOptions.defaultOptions(),
            flushQueueSize: Int = FLUSH_QUEUE_SIZE,
            maxFlushInterval: Long = MAX_FLUSH_INTERVAL,
            isOptOut: Boolean = false,
            shouldVerifySdk: Boolean = false,
            sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
            dataPlaneUrl: String? = null, //defaults to https://hosted.rudderlabs.com
            controlPlaneUrl: String? = null, //defaults to https://api.rudderlabs.com/
            logger: Logger = KotlinLogger,
            storage: Storage = BasicStorageImpl(logger = logger),
            analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
            networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
            base64Generator: Base64Generator = RudderUtils.defaultBase64Generator,
        ) = object : Configuration {
            override val jsonAdapter: JsonAdapter = jsonAdapter
            override val options: RudderOptions = options
            override val flushQueueSize: Int = flushQueueSize
            override val maxFlushInterval: Long = maxFlushInterval
            override val isOptOut: Boolean = isOptOut
            override val shouldVerifySdk: Boolean = shouldVerifySdk
            override val sdkVerifyRetryStrategy: RetryStrategy = sdkVerifyRetryStrategy
            override val dataPlaneUrl: String = dataPlaneUrl?:"https://hosted.rudderlabs.com"
            override val controlPlaneUrl: String = controlPlaneUrl?:"https://api.rudderstack.com/"
            override val logger: Logger = logger
            override val storage: Storage = storage
            override val analyticsExecutor: ExecutorService = analyticsExecutor
            override val networkExecutor: ExecutorService = networkExecutor
            override val base64Generator: Base64Generator = base64Generator
        }
    }

}
//A copy constructor for Configuration

fun Configuration.copy(
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
    storage: Storage = this.storage,
    analyticsExecutor: ExecutorService = this.analyticsExecutor,
    networkExecutor: ExecutorService = this.networkExecutor,
    base64Generator: Base64Generator = this.base64Generator,
) = Configuration(
    jsonAdapter = jsonAdapter,
    options = options,
    flushQueueSize = flushQueueSize,
    maxFlushInterval = maxFlushInterval,
    isOptOut = isOptOut,
    shouldVerifySdk = shouldVerifySdk,
    sdkVerifyRetryStrategy = sdkVerifyRetryStrategy,
    dataPlaneUrl = dataPlaneUrl,
    controlPlaneUrl = controlPlaneUrl,
    logger = logger,
    storage = storage,
    analyticsExecutor = analyticsExecutor,
    networkExecutor = networkExecutor,
    base64Generator = base64Generator,
)

//    private val String.formattedUrl
//        get() = if (this.endsWith('/')) this else "$this/"