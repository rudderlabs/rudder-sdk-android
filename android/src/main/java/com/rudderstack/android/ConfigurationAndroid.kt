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
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.core.Base64Generator
import com.rudderstack.core.Configuration
import com.rudderstack.core.Logger
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.RudderOptions
import com.rudderstack.core.Storage
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
     * @property storage
     * @property advertisingIdFetchExecutor
     * @constructor
     * TODO
     *
     * @param jsonAdapter
     * @param options
     * @param flushQueueSize
     * @param maxFlushInterval
     * @param isOptOut
     * @param shouldVerifySdk
     * @param sdkVerifyRetryStrategy
     * @param dataPlaneUrl
     * @param controlPlaneUrl
     * @param logger
     * @param analyticsExecutor
     * @param networkExecutor
     * @param base64Generator
     */
    val application: Application
    val anonymousId: String
    val userId: String?
    val trackLifecycleEvents: Boolean
    val recordScreenViews: Boolean
    val isPeriodicFlushEnabled: Boolean
    val autoCollectAdvertId: Boolean
    val multiProcessEnabled: Boolean
    val defaultProcessName: String?
    val advertisingId: String?
    val deviceToken: String?
    override val storage: AndroidStorage
    val advertisingIdFetchExecutor : ExecutorService?
    //session
    val trackAutoSession: Boolean
    val sessionTimeoutMillis: Long


    companion object {
        operator fun invoke(
            application: Application,
            jsonAdapter: JsonAdapter,
            anonymousId: String= AndroidUtils.getDeviceId(
                application
            ),
            userId: String? = null,
            options: RudderOptions = RudderOptions.defaultOptions(),
            flushQueueSize: Int = Defaults.DEFAULT_FLUSH_QUEUE_SIZE,
            maxFlushInterval: Long = Defaults.DEFAULT_MAX_FLUSH_INTERVAL,
            isOptOut: Boolean = false,
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
            defaultProcessName: String?= Defaults.DEFAULT_PROCESS_NAME,
            advertisingId: String? = null,
            deviceToken: String? = null,
            logger: Logger = AndroidLogger,
            storage: AndroidStorage = AndroidStorageImpl(application,
                useContentProvider = Defaults.USE_CONTENT_PROVIDER,
                instanceName = "some_def"//TODO()
                         ),
            analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
            networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
            advertisingIdFetchExecutor : ExecutorService? = null,
            base64Generator: Base64Generator = AndroidUtils.defaultBase64Generator(),
            trackAutoSession: Boolean = Defaults.AUTO_SESSION_TRACKING,
            sessionTimeoutMillis: Long = Defaults.SESSION_TIMEOUT
//    val defaultTraits: IdentifyTraits? = null, // will be added by default to each message
//    val defaultExternalIds: List<Map<String, String>>? = null, // will be added by default to each message
//    val defaultContextMap: Map<String, Any>? = null, // will be added by default to each message
//    val contextAddOns: Map<String, Any>? = null // will be added by default to each message
        ) : ConfigurationAndroid = object : ConfigurationAndroid{
            override val application: Application = application
            override val anonymousId: String = anonymousId
            override val userId: String? = userId
            override val trackLifecycleEvents: Boolean = trackLifecycleEvents
            override val recordScreenViews: Boolean = recordScreenViews
            override val isPeriodicFlushEnabled: Boolean = isPeriodicFlushEnabled
            override val autoCollectAdvertId: Boolean = autoCollectAdvertId
            override val multiProcessEnabled: Boolean = multiProcessEnabled
            override val defaultProcessName: String? = defaultProcessName
            override val advertisingId: String? = advertisingId
            override val deviceToken: String? = deviceToken
            override val storage: AndroidStorage = storage
            override val advertisingIdFetchExecutor : ExecutorService? = advertisingIdFetchExecutor
            override val trackAutoSession: Boolean = trackAutoSession
            override val sessionTimeoutMillis: Long = sessionTimeoutMillis
            override val jsonAdapter: JsonAdapter = jsonAdapter
            override val options: RudderOptions = options
            override val flushQueueSize: Int = flushQueueSize
            override val maxFlushInterval: Long = maxFlushInterval
            override val isOptOut: Boolean = isOptOut
            override val shouldVerifySdk: Boolean = shouldVerifySdk
            override val gzipEnabled: Boolean = gzipEnabled
            override val sdkVerifyRetryStrategy: RetryStrategy = sdkVerifyRetryStrategy
            override val dataPlaneUrl: String = dataPlaneUrl?:Defaults.DEFAULT_ANDROID_DATAPLANE_URL
            override val controlPlaneUrl: String = controlPlaneUrl?:Defaults.DEFAULT_ANDROID_CONTROLPLANE_URL
            override val logger: Logger = logger
            override val analyticsExecutor: ExecutorService = analyticsExecutor
            override val networkExecutor: ExecutorService = networkExecutor
            override val base64Generator: Base64Generator = base64Generator

        }
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        operator fun invoke(
            configuration: Configuration,
            application: Application,
            anonymousId: String = AndroidUtils.getDeviceId(
                application
            ),
            userId: String? = null,
            trackLifecycleEvents: Boolean = Defaults.TRACK_LIFECYCLE_EVENTS,

            recordScreenViews: Boolean = Defaults.RECORD_SCREEN_VIEWS,

            isPeriodicFlushEnabled: Boolean = Defaults.IS_PERIODIC_FLUSH_ENABLED,

            autoCollectAdvertId: Boolean = Defaults.AUTO_COLLECT_ADVERT_ID,

            multiProcessEnabled: Boolean = Defaults.MULTI_PROCESS_ENABLED,

            defaultProcessName: String? = Defaults.DEFAULT_PROCESS_NAME,

            advertisingId: String? = null,

            deviceToken: String? = null,

                            storage: AndroidStorage = AndroidStorageImpl(application,
                                Defaults.USE_CONTENT_PROVIDER,
                                instanceName = "todo" // TODO()
                            ),

            advertisingIdFetchExecutor: ExecutorService? = null,
            trackAutoSession: Boolean = Defaults.AUTO_SESSION_TRACKING,
            sessionTimeoutMillis: Long = Defaults.SESSION_TIMEOUT
        ): ConfigurationAndroid=
            invoke(
                application,
                configuration.jsonAdapter,
                anonymousId,
                userId,
                configuration.options,
                configuration.flushQueueSize,
                configuration.maxFlushInterval,
                configuration.isOptOut,
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
                configuration.logger,
                storage,
                configuration.analyticsExecutor,
                configuration.networkExecutor,
                advertisingIdFetchExecutor,
                configuration.base64Generator,
                trackAutoSession,
                sessionTimeoutMillis)
    }

    override fun copy(
        jsonAdapter: JsonAdapter,
        options: RudderOptions,
        flushQueueSize: Int,
        maxFlushInterval: Long,
        isOptOut: Boolean,
        shouldVerifySdk: Boolean,
        gzipEnabled: Boolean,
        sdkVerifyRetryStrategy: RetryStrategy,
        dataPlaneUrl: String,
        controlPlaneUrl: String?,
        logger: Logger,
        storage: Storage,
        analyticsExecutor: ExecutorService,
        networkExecutor: ExecutorService,
        base64Generator: Base64Generator,
    ): Configuration {
        return copy(
            jsonAdapter,
            options,
            flushQueueSize,
            maxFlushInterval,
            isOptOut,
            shouldVerifySdk,
            gzipEnabled,
            sdkVerifyRetryStrategy,
            dataPlaneUrl,
            controlPlaneUrl,
            logger,
            (storage as? AndroidStorage)?: this.storage,
            analyticsExecutor,
            networkExecutor,
            advertisingIdFetchExecutor,
            base64Generator,
        )
    }

    fun copy(
        jsonAdapter: JsonAdapter = this.jsonAdapter,
        options: RudderOptions = this.options,
        flushQueueSize: Int = this.flushQueueSize,
        maxFlushInterval: Long = this.maxFlushInterval,
        isOptOut: Boolean = this.isOptOut,
        shouldVerifySdk: Boolean = this.shouldVerifySdk,
        gzipEnabled: Boolean = this.gzipEnabled,
        sdkVerifyRetryStrategy: RetryStrategy = this.sdkVerifyRetryStrategy,
        dataPlaneUrl: String = this.dataPlaneUrl,
        controlPlaneUrl: String? = this.controlPlaneUrl,
        logger: Logger = this.logger,
        storage: AndroidStorage = this.storage,
        analyticsExecutor: ExecutorService = this.analyticsExecutor,
        networkExecutor: ExecutorService = this.networkExecutor,
        advertisingIdFetchExecutor : ExecutorService? = this.advertisingIdFetchExecutor,
        base64Generator: Base64Generator = this.base64Generator,
        anonymousId: String = this.anonymousId,
        userId: String? = this.userId,
        advertisingId: String? = this.advertisingId,
        deviceToken: String? = this.deviceToken,
        trackAutoSession: Boolean = this.trackAutoSession,
        sessionTimeoutMillis: Long = this.sessionTimeoutMillis
    ) : ConfigurationAndroid{
        return ConfigurationAndroid(
            application,
            jsonAdapter,
            anonymousId,
            userId,
            options,
            flushQueueSize,
            maxFlushInterval,
            isOptOut,
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
            logger,
            storage,
            analyticsExecutor,
            networkExecutor,
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

    object Defaults{
        const val DEFAULT_ANDROID_DATAPLANE_URL = "https://hosted.rudderlabs.com"
        const val DEFAULT_ANDROID_CONTROLPLANE_URL = "https://api.rudderlabs.com"
        const val GZIP_ENABLED: Boolean = true
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
        const val SESSION_TIMEOUT: Long = 300000
        const val AUTO_SESSION_TRACKING = true
    }
}