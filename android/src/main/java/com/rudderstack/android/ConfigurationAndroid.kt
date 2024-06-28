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
import com.rudderstack.core.RudderLogger
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.RudderOption
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


interface ConfigurationAndroid : Configuration {
    /**
     * TODO write documentation
     *
     * @property application
     * @property anonymousId
     * @property userId
     * @property trackLifecycleEvents
     * @property recordScreenViews
     * @property isPeriodicFlushEnabled
     * @property autoCollectAdvertId
     * @property multiProcessEnabled
     * @property defaultProcessName
     * @property useContentProvider
     * @property advertisingId
     * @property deviceToken
     * @property advertisingIdFetchExecutor
     * @constructor
     * TODO
     *
     * @param options
     * @param flushQueueSize
     * @param maxFlushInterval
     * @param shouldVerifySdk
     * @param sdkVerifyRetryStrategy
     * @param dataPlaneUrl
     * @param controlPlaneUrl
     * @param logLevel
     * @param analyticsExecutor
     * @param networkExecutor
     * @param base64Generator
     */
    val application: Application
    val anonymousId: String?
    val userId: String?
    val trackLifecycleEvents: Boolean
    val recordScreenViews: Boolean
    val isPeriodicFlushEnabled: Boolean
    val autoCollectAdvertId: Boolean
    val multiProcessEnabled: Boolean
    val defaultProcessName: String?
    val advertisingId: String?
    val deviceToken: String?
    val collectDeviceId: Boolean
    val advertisingIdFetchExecutor: ExecutorService
    //session
    val trackAutoSession: Boolean
    val sessionTimeoutMillis: Long


    companion object {
        operator fun invoke(
            application: Application,
            anonymousId: String? = null,
            userId: String? = null,
            options: RudderOption = RudderOption(),
            flushQueueSize: Int = Defaults.DEFAULT_FLUSH_QUEUE_SIZE,
            maxFlushInterval: Long = Defaults.DEFAULT_MAX_FLUSH_INTERVAL,
            shouldVerifySdk: Boolean = Defaults.SHOULD_VERIFY_SDK,
            gzipEnabled: Boolean = Defaults.GZIP_ENABLED,
            sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
            dataPlaneUrl: String? = null, //defaults to https://hosted.rudderlabs.com
            controlPlaneUrl: String? = null, //defaults to https://api.rudderlabs.com/
            trackLifecycleEvents: Boolean = Defaults.TRACK_LIFECYCLE_EVENTS,
            recordScreenViews: Boolean = Defaults.RECORD_SCREEN_VIEWS,
            isPeriodicFlushEnabled: Boolean = Defaults.IS_PERIODIC_FLUSH_ENABLED,
            autoCollectAdvertId: Boolean = Defaults.AUTO_COLLECT_ADVERT_ID,
            multiProcessEnabled: Boolean = Defaults.MULTI_PROCESS_ENABLED,
            defaultProcessName: String? = Defaults.DEFAULT_PROCESS_NAME,
            advertisingId: String? = null,
            deviceToken: String? = null,
            logLevel: RudderLogger.LogLevel = RudderLogger.DEFAULT_LOG_LEVEL,
            analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
            networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
            collectDeviceId: Boolean = Defaults.COLLECT_DEVICE_ID,
            advertisingIdFetchExecutor: ExecutorService = Executors.newCachedThreadPool(),
            base64Generator: Base64Generator = AndroidUtils.defaultBase64Generator(),
            trackAutoSession: Boolean = Defaults.AUTO_SESSION_TRACKING,
            sessionTimeoutMillis: Long = Defaults.SESSION_TIMEOUT
        ) = invoke(
            application,
            anonymousId,
            userId,
            options,
            flushQueueSize,
            maxFlushInterval,
            shouldVerifySdk,
            gzipEnabled,
            sdkVerifyRetryStrategy,
            dataPlaneUrl,
            controlPlaneUrl,
            trackLifecycleEvents,
            recordScreenViews,
            isPeriodicFlushEnabled,
            autoCollectAdvertId,
            multiProcessEnabled,
            defaultProcessName,
            advertisingId,
            deviceToken,
            AndroidLogger(logLevel),
            analyticsExecutor,
            networkExecutor,
            collectDeviceId,
            advertisingIdFetchExecutor,
            base64Generator,
            trackAutoSession,
            sessionTimeoutMillis
        )

        internal operator fun invoke(
            application: Application,
            anonymousId: String? = null,
            userId: String? = null,
            options: RudderOption = RudderOption(),
            flushQueueSize: Int = Defaults.DEFAULT_FLUSH_QUEUE_SIZE,
            maxFlushInterval: Long = Defaults.DEFAULT_MAX_FLUSH_INTERVAL,
            shouldVerifySdk: Boolean = Defaults.SHOULD_VERIFY_SDK,
            gzipEnabled: Boolean = Defaults.GZIP_ENABLED,
            sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
            dataPlaneUrl: String? = null, //defaults to https://hosted.rudderlabs.com
            controlPlaneUrl: String? = null, //defaults to https://api.rudderlabs.com/
            trackLifecycleEvents: Boolean = Defaults.TRACK_LIFECYCLE_EVENTS,
            recordScreenViews: Boolean = Defaults.RECORD_SCREEN_VIEWS,
            isPeriodicFlushEnabled: Boolean = Defaults.IS_PERIODIC_FLUSH_ENABLED,
            autoCollectAdvertId: Boolean = Defaults.AUTO_COLLECT_ADVERT_ID,
            multiProcessEnabled: Boolean = Defaults.MULTI_PROCESS_ENABLED,
            defaultProcessName: String? = Defaults.DEFAULT_PROCESS_NAME,
            advertisingId: String? = null,
            deviceToken: String? = null,
            rudderLogger: RudderLogger = AndroidLogger(),
            analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
            networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
            collectDeviceId: Boolean = Defaults.COLLECT_DEVICE_ID,
            advertisingIdFetchExecutor: ExecutorService = Executors.newCachedThreadPool(),
            base64Generator: Base64Generator = AndroidUtils.defaultBase64Generator(),
            trackAutoSession: Boolean = Defaults.AUTO_SESSION_TRACKING,
            sessionTimeoutMillis: Long = Defaults.SESSION_TIMEOUT
//    val defaultTraits: IdentifyTraits? = null, // will be added by default to each message
//    val defaultExternalIds: List<Map<String, String>>? = null, // will be added by default to each message
//    val defaultContextMap: Map<String, Any>? = null, // will be added by default to each message
//    val contextAddOns: Map<String, Any>? = null // will be added by default to each message
        ): ConfigurationAndroid = object : ConfigurationAndroid {
            override val application: Application = application
            override val anonymousId: String? = anonymousId
            override val userId: String? = userId
            override val trackLifecycleEvents: Boolean = trackLifecycleEvents
            override val recordScreenViews: Boolean = recordScreenViews
            override val isPeriodicFlushEnabled: Boolean = isPeriodicFlushEnabled
            override val autoCollectAdvertId: Boolean = autoCollectAdvertId
            override val multiProcessEnabled: Boolean = multiProcessEnabled
            override val defaultProcessName: String? = defaultProcessName
            override val advertisingId: String? = advertisingId
            override val deviceToken: String? = deviceToken
            override val advertisingIdFetchExecutor: ExecutorService = advertisingIdFetchExecutor
            override val trackAutoSession: Boolean = trackAutoSession
            override val sessionTimeoutMillis: Long = sessionTimeoutMillis
            override val options: RudderOption = options
            override val flushQueueSize: Int = flushQueueSize
            override val maxFlushInterval: Long = maxFlushInterval
            override val shouldVerifySdk: Boolean = shouldVerifySdk
            override val gzipEnabled: Boolean = gzipEnabled
            override val sdkVerifyRetryStrategy: RetryStrategy = sdkVerifyRetryStrategy
            override val dataPlaneUrl: String =
                dataPlaneUrl ?: Defaults.DEFAULT_ANDROID_DATAPLANE_URL
            override val controlPlaneUrl: String =
                controlPlaneUrl ?: Defaults.DEFAULT_ANDROID_CONTROLPLANE_URL
            override val rudderLogger: RudderLogger = rudderLogger
            override val analyticsExecutor: ExecutorService = analyticsExecutor
            override val networkExecutor: ExecutorService = networkExecutor
            override val base64Generator: Base64Generator = base64Generator
            override val collectDeviceId: Boolean = collectDeviceId
        }

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        operator fun invoke(
            configuration: Configuration,
            application: Application,
            anonymousId: String? = AndroidUtils.generateAnonymousId(Defaults.COLLECT_DEVICE_ID, application),
            userId: String? = null,
            trackLifecycleEvents: Boolean = Defaults.TRACK_LIFECYCLE_EVENTS,
            recordScreenViews: Boolean = Defaults.RECORD_SCREEN_VIEWS,
            isPeriodicFlushEnabled: Boolean = Defaults.IS_PERIODIC_FLUSH_ENABLED,
            autoCollectAdvertId: Boolean = Defaults.AUTO_COLLECT_ADVERT_ID,
            multiProcessEnabled: Boolean = Defaults.MULTI_PROCESS_ENABLED,
            defaultProcessName: String? = Defaults.DEFAULT_PROCESS_NAME,
            advertisingId: String? = null,
            deviceToken: String? = null,
            rudderLogger: RudderLogger = AndroidLogger(),
            collectDeviceId: Boolean = Defaults.COLLECT_DEVICE_ID,
            advertisingIdFetchExecutor: ExecutorService = Executors.newCachedThreadPool(),
            trackAutoSession: Boolean = Defaults.AUTO_SESSION_TRACKING,
            sessionTimeoutMillis: Long = Defaults.SESSION_TIMEOUT
        ): ConfigurationAndroid =
            invoke(
                application,
                anonymousId,
                userId,
                configuration.options,
                configuration.flushQueueSize,
                configuration.maxFlushInterval,
                configuration.shouldVerifySdk,
                configuration.gzipEnabled,
                configuration.sdkVerifyRetryStrategy,
                configuration.dataPlaneUrl,
                configuration.controlPlaneUrl,
                trackLifecycleEvents,
                recordScreenViews,
                isPeriodicFlushEnabled,
                autoCollectAdvertId,
                multiProcessEnabled,
                defaultProcessName,
                advertisingId,
                deviceToken,
                rudderLogger,
                configuration.analyticsExecutor,
                configuration.networkExecutor,
                collectDeviceId,
                advertisingIdFetchExecutor,
                configuration.base64Generator,
                trackAutoSession,
                sessionTimeoutMillis
            )
    }

    override fun copy(
        options: RudderOption,
        flushQueueSize: Int,
        maxFlushInterval: Long,
        shouldVerifySdk: Boolean,
        gzipEnabled: Boolean,
        sdkVerifyRetryStrategy: RetryStrategy,
        dataPlaneUrl: String,
        controlPlaneUrl: String?,
        rudderLogger: RudderLogger,
        analyticsExecutor: ExecutorService,
        networkExecutor: ExecutorService,
        base64Generator: Base64Generator
    ): ConfigurationAndroid {
        return ConfigurationAndroid(
            application,
                    anonymousId,
                    userId,
                    options,
                    flushQueueSize,
                    maxFlushInterval,
                    shouldVerifySdk,
                    gzipEnabled,
                    sdkVerifyRetryStrategy,
                    dataPlaneUrl,
                    controlPlaneUrl,
                    trackLifecycleEvents,
                    recordScreenViews,
                    isPeriodicFlushEnabled,
                    autoCollectAdvertId,
                    multiProcessEnabled,
                    defaultProcessName,
                    advertisingId,
                    deviceToken,
                    this.rudderLogger.also { it.activate(rudderLogger.level) },
                    analyticsExecutor,
                    networkExecutor,
                    collectDeviceId,
                    advertisingIdFetchExecutor,
                    base64Generator,
                    trackAutoSession,
                    sessionTimeoutMillis,
        )
    }

    fun copy(
        options: RudderOption = this.options,
        flushQueueSize: Int = this.flushQueueSize,
        maxFlushInterval: Long = this.maxFlushInterval,
        gzipEnabled: Boolean = this.gzipEnabled,
        analyticsExecutor: ExecutorService = this.analyticsExecutor,
        networkExecutor: ExecutorService = this.networkExecutor,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        advertisingId: String? = this.advertisingId,
        deviceToken: String? = this.deviceToken,
        trackAutoSession: Boolean = this.trackAutoSession,
        sessionTimeoutMillis: Long = this.sessionTimeoutMillis,
    ) : ConfigurationAndroid{
        return ConfigurationAndroid(
            application,
            anonymousId,
            userId,
            options,
            flushQueueSize,
            maxFlushInterval,
            shouldVerifySdk,
            gzipEnabled,
            sdkVerifyRetryStrategy,
            dataPlaneUrl,
            controlPlaneUrl,
            trackLifecycleEvents,
            recordScreenViews,
            isPeriodicFlushEnabled,
            autoCollectAdvertId,
            multiProcessEnabled,
            defaultProcessName,
            advertisingId,
            deviceToken,
            rudderLogger,
            analyticsExecutor,
            networkExecutor,
            collectDeviceId,
            advertisingIdFetchExecutor,
            base64Generator,
            trackAutoSession,
            sessionTimeoutMillis
//        defaultTraits,
//        defaultExternalIds,
//        defaultContextMap,
//        contextAddOns
        )
    }

    object Defaults {
        const val COLLECT_DEVICE_ID: Boolean = true
        const val DEFAULT_ANDROID_DATAPLANE_URL = "https://hosted.rudderlabs.com"
        const val DEFAULT_ANDROID_CONTROLPLANE_URL = "https://api.rudderlabs.com"
        const val GZIP_ENABLED: Boolean = true
        const val SHOULD_VERIFY_SDK: Boolean = true
        const val TRACK_LIFECYCLE_EVENTS = true
        const val RECORD_SCREEN_VIEWS = true
        const val IS_PERIODIC_FLUSH_ENABLED = false
        const val AUTO_COLLECT_ADVERT_ID = false
        const val MULTI_PROCESS_ENABLED = false
        val DEFAULT_PROCESS_NAME: String? = null
        const val USE_CONTENT_PROVIDER = false
        const val DEFAULT_FLUSH_QUEUE_SIZE = 30
        const val DEFAULT_MAX_FLUSH_INTERVAL = 10 * 1000L
        const val SESSION_TIMEOUT: Long = 300000
        const val AUTO_SESSION_TRACKING = true
    }
}
