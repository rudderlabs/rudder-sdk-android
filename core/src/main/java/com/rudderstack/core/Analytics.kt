/*
 * Creator: Debanjan Chatterjee on 26/11/21, 12:24 AM Last modified: 26/11/21, 12:24 AM
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

import com.rudderstack.core.internal.*
import com.rudderstack.core.internal.states.ContextState
import com.rudderstack.core.internal.states.SettingsState
import com.rudderstack.models.*
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.*
import java.util.concurrent.*


class Analytics private constructor(
    private val _writeKey: String,
    private val _jsonAdapter: JsonAdapter,
    private val _dataPlaneUrl: String,
    private val _delegate: AnalyticsDelegate,
    val analyticsExecutor: ExecutorService
) : Controller by _delegate {
    /**
     * Contains methods for sending messages over to device mode and cloud mode destinations.
     * Developers are required to maintain the object throughout application lifetime,
     * since this implementation doesn't directly provide a Singleton Instance.
     *
     * If this SDK is intended to be used in Android, refrain from using this class directly.
     * Also in case of using this class directly, developer has to add their own context plugin,
     * to add context to messages.
     *  [analyticsExecutor] is by default [Executors.newSingleThreadExecutor] to maintain
     *  synchronicity of events. Other executors are practically usable iff the difference between two events
     *  is guaranteed to be at least a few ms.
     * [defaultTraits] and [defaultExternalIds] are stored in [ContextState] but not in [Storage]
     * Only [MessageContext] passed in [IdentifyMessage] are stored in [Storage]
     */
    constructor(
        writeKey: String,
        settings: Settings,
        jsonAdapter: JsonAdapter,
        shouldVerifySdk: Boolean = true,
        sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
        dataPlaneUrl: String? = null, //defaults to https://hosted.rudderlabs.com
        controlPlaneUrl: String? = null, //defaults to https://api.rudderlabs.com/
        logger: Logger = KotlinLogger,
        storage: Storage = BasicStorageImpl(logger = logger),
        analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
        networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
        base64Generator: Base64Generator = Base64Generator {
            Base64.getEncoder().encodeToString(
                String.format(Locale.US, "%s:", it).toByteArray(charset("UTF-8"))
            )
        },
        dataUploadService: DataUploadService = DataUploadServiceImpl(
            base64Generator.generateBase64(writeKey),
            jsonAdapter,
            SettingsState,
            dataPlaneUrl ?: DATA_PLANE_URL,
            networkExecutor
        ),
        configDownloadService: ConfigDownloadService = ConfigDownloadServiceImpl(
            base64Generator.generateBase64(writeKey),
            controlPlaneUrl ?: CONTROL_PLANE_URL,
            jsonAdapter,
            networkExecutor
        ),
        defaultTraits: IdentifyTraits? = storage.context?.traits,
        defaultExternalIds: List<Map<String, String>>? = storage.context?.externalIds,
        defaultContextMap: Map<String, Any>? = null,
        contextAddOns: Map<String, Any>? = null,
        //optional
        initializationListener: ((success: Boolean, message: String?) -> Unit)? = null,
        //optional called if shutdown is called
        shutdownHook: (() -> Unit)? = null
    ) : this(
        _writeKey = writeKey,
        _jsonAdapter = jsonAdapter,
        _dataPlaneUrl = dataPlaneUrl ?: DATA_PLANE_URL,
        _delegate = AnalyticsDelegate(
            settings,
            storage,
            jsonAdapter,
            shouldVerifySdk,
            sdkVerifyRetryStrategy,
            dataUploadService,
            configDownloadService,
            analyticsExecutor,
            logger,
            storage.context ?: createContext(
                defaultTraits,
                defaultExternalIds,
                defaultContextMap,
                contextAddOns
            ).also {
                analyticsExecutor.submit {
                    storage.cacheContext(it)
                }
            },
            initializationListener,
            shutdownHook

        ),
        analyticsExecutor = analyticsExecutor
    )


    companion object {
        // default base url or rudder-backend-server
        private const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"

        // config-plane url to get the config for the writeKey
        private const val CONTROL_PLANE_URL = "https://api.rudderlabs.com/"

    }

    /**
     * Track with a built [TrackMessage]
     * Date format should be yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     *
     * @param message
     * @param options
     */
    fun track(message: TrackMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    fun track(
        eventName: String,
        trackProperties: TrackProperties,
        userID: String? = null,
        options: RudderOptions? = null
    ) {
        track(
            TrackMessage.create(
                timestamp = RudderUtils.timeStamp,
                eventName = eventName,
                properties = trackProperties,
                userId = userID
            ), options
        )

    }

    fun screen(message: ScreenMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    fun screen(
        screenName: String,
        category: String,
        screenProperties: ScreenProperties,
        userID: String? = null,
        options: RudderOptions? = null
    ) {
        screen(
            ScreenMessage.create(
                userId = userID, timestamp = RudderUtils.timeStamp, category = category,
                name = screenName, properties = screenProperties
            ), options
        )
    }

    fun identify(message: IdentifyMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    fun identify(
        userID: String, traits: IdentifyTraits? = null,
        options: RudderOptions? = null,
    ) {
        val completeTraits = mapOf("userId" to userID) optAdd traits
        identify(
            IdentifyMessage.create(
                userId = userID,
                timestamp = RudderUtils.timeStamp,
                traits = completeTraits,
            ), options
        )
    }

    fun alias(message: AliasMessage, options: RudderOptions? = null) {
        //TODO(change userId)
        processMessage(message, options)
    }

    fun alias(
        newId: String,
        options: RudderOptions? = null
    ) {
        alias(
            AliasMessage.create(timestamp = RudderUtils.timeStamp, userId = newId), options
        )
    }

    fun group(message: GroupMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    fun group(
        groupID: String, traits: GroupTraits? = null,
        userID: String? = null,
        options: RudderOptions? = null
    ) {
        group(
            GroupMessage.create(
                timestamp = RudderUtils.timeStamp, userId = userID,
                groupId = groupID, groupTraits = traits
            ), options
        )
    }

    /**
     * Flush the remaining data from storage.
     * However flush returns immediately if  analytics is shutdown
     */
    fun flush() {
        _delegate.flush()
    }

    /**
     * Flushes forcefully, even if analytics has shutdown.
     * One can optionally provide alternate data upload service and alternate executor
     * for force flush, and force flush will act on those.
     *
     * N.B - Can be used to flush to a different destination.
     * In case this is used as an alternate to sync data to different upload service,
     * it will be noteworthy to set [clearDb] to false, otherwise uploaded data will be
     * cleared from database
     *
     * @param alternateDataUploadService The [DataUploadService] to upload data. Default is null. In
     * case null is sent, Analytics will create a [DataUploadServiceImpl] instance to pass over data.
     *
     * @param alternateExecutor The flush will be processed on this [ExecutorService]
     * @param clearDb Uploaded data will be cleared from [Storage] if true, else not.
     */
    fun forceFlush(
        alternateDataUploadService: DataUploadService? = null,
        alternateExecutor: ExecutorService? = null,
        clearDb: Boolean = true
    ) {
        val flushExecutor = alternateExecutor ?: ThreadPoolExecutor(
            1, 1,
            0L, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue<Runnable>(1),
            ThreadPoolExecutor.DiscardOldestPolicy()
        )
        val dataUploadService = alternateDataUploadService ?: DataUploadServiceImpl(
            _writeKey,
            _jsonAdapter, dataPlaneUrl = _dataPlaneUrl
        )
        _delegate.forceFlush(
            dataUploadService, flushExecutor, clearDb
        )
        //shut down if data uploader/executor is initialized here
        if (alternateDataUploadService == null)
            dataUploadService.shutdown()
        if (alternateExecutor == null)
            flushExecutor.shutdown()
    }

}