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
import androidx.annotation.RestrictTo
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.core.Base64Generator
import com.rudderstack.core.Configuration
import com.rudderstack.core.Logger
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.RudderOption
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Data class representing the Android-specific configuration for the RudderStack analytics SDK.
 *
 */
data class ConfigurationAndroid @JvmOverloads constructor(
    val application: Application,
    val anonymousId: String? = null,
    val trackLifecycleEvents: Boolean = TRACK_LIFECYCLE_EVENTS,
    val recordScreenViews: Boolean = RECORD_SCREEN_VIEWS,
    val isPeriodicFlushEnabled: Boolean = IS_PERIODIC_FLUSH_ENABLED,
    val autoCollectAdvertId: Boolean = AUTO_COLLECT_ADVERT_ID,
    val multiProcessEnabled: Boolean = MULTI_PROCESS_ENABLED,
    val defaultProcessName: String? = DEFAULT_PROCESS_NAME,
    val advertisingId: String? = null,
    val deviceToken: String? = null,
    val logLevel: Logger.LogLevel = Logger.DEFAULT_LOG_LEVEL,
    val collectDeviceId: Boolean = COLLECT_DEVICE_ID,
    val advertisingIdFetchExecutor: ExecutorService = Executors.newCachedThreadPool(),
    val trackAutoSession: Boolean = AUTO_SESSION_TRACKING,
    val sessionTimeoutMillis: Long = SESSION_TIMEOUT,
    override val jsonAdapter: JsonAdapter,
    override val options: RudderOption = RudderOption(),
    override val flushQueueSize: Int = DEFAULT_FLUSH_QUEUE_SIZE,
    override val maxFlushInterval: Long = DEFAULT_MAX_FLUSH_INTERVAL,
    override val shouldVerifySdk: Boolean = SHOULD_VERIFY_SDK,
    override val gzipEnabled: Boolean = GZIP_ENABLED,
    override val sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
    override val dataPlaneUrl: String = DEFAULT_ANDROID_DATAPLANE_URL,
    override val controlPlaneUrl: String = DEFAULT_ANDROID_CONTROLPLANE_URL,
    override val analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
    override val networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
    override val base64Generator: Base64Generator = AndroidUtils.defaultBase64Generator(),
) : Configuration (
    jsonAdapter = jsonAdapter,
    options = options,
    flushQueueSize = flushQueueSize,
    maxFlushInterval = maxFlushInterval,
    shouldVerifySdk = shouldVerifySdk,
    gzipEnabled = gzipEnabled,
    sdkVerifyRetryStrategy = sdkVerifyRetryStrategy,
    dataPlaneUrl = dataPlaneUrl,
    controlPlaneUrl = controlPlaneUrl,
    logger = AndroidLogger(logLevel),
    analyticsExecutor = analyticsExecutor,
    networkExecutor = networkExecutor,
    base64Generator = base64Generator,
) {
    companion object {
        const val COLLECT_DEVICE_ID: Boolean = true
        const val DEFAULT_ANDROID_DATAPLANE_URL = "https://hosted.rudderlabs.com"
        const val DEFAULT_ANDROID_CONTROLPLANE_URL = "https://api.rudderlabs.com"
        const val GZIP_ENABLED: Boolean = true
        const val SHOULD_VERIFY_SDK: Boolean = true
        const val TRACK_LIFECYCLE_EVENTS = true
        const val RECORD_SCREEN_VIEWS = false
        const val IS_PERIODIC_FLUSH_ENABLED = false
        const val AUTO_COLLECT_ADVERT_ID = false
        const val MULTI_PROCESS_ENABLED = false
        @JvmField
        var DEFAULT_PROCESS_NAME: String? = null
        const val USE_CONTENT_PROVIDER = false
        const val DEFAULT_FLUSH_QUEUE_SIZE = 30
        const val DEFAULT_MAX_FLUSH_INTERVAL = 10 * 1000L
        const val SESSION_TIMEOUT: Long = 300000
        const val AUTO_SESSION_TRACKING = true
    }
}
