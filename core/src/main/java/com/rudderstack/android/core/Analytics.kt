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

package com.rudderstack.android.core

import com.rudderstack.android.core.internal.*
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.*
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Analytics private constructor(
    private val _writeKey: String,
    private val _jsonAdapter: JsonAdapter,
    private val _dataPlaneUrl: String,
    private val _delegate: AnalyticsDelegate
) : Controller by _delegate {
    /**
     * Contains methods for sending messages over to device mode and cloud mode destinations.
     * Developers are required to maintain the object throughout application lifetime,
     * since this implementation doesn't directly provide a Singleton Instance.
     *
     * If this SDK is intended to be used in Android, refrain from using this class directly.
     * Also in case of using this class directly, developer has to add their own context plugin,
     * to add context to messages.
     *
     * @constructor
     *
     *
     * @param writeKey
     * @param settings
     * @param jsonAdapter
     * @param options
     * @param dataPlaneUrl
     * @param controlPlaneUrl
     * @param storage
     * @param analyticsExecutor
     * @param networkExecutor
     * @param dataUploadService
     */
    constructor(
        writeKey: String,
        settings: Settings,
        jsonAdapter: JsonAdapter,
        shouldVerifySdk: Boolean = true,
        sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
        options: RudderOptions = RudderOptions.default(),
        dataPlaneUrl: String = DATA_PLANE_URL,
        controlPlaneUrl: String = CONTROL_PLANE_URL,
        logger: Logger = KotlinLogger,
        storage: Storage = BasicStorageImpl(logger = logger),
        analyticsExecutor: ExecutorService = Executors.newCachedThreadPool(),
        networkExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
        dataUploadService: DataUploadService = DataUploadServiceImpl(
            writeKey,
            jsonAdapter,
            SettingsState,
            dataPlaneUrl,
            networkExecutor
        ),
        configDownloadService: ConfigDownloadService = ConfigDownloadServiceImpl(
            writeKey,
            controlPlaneUrl,
            jsonAdapter,
            networkExecutor
        ),
        defaultTraits: IdentifyTraits? = null,
        defaultExternalIds: List<Map<String, String>>? = null,
        defaultContextMap: Map<String, Any>? = null
    ) : this(
        _writeKey = writeKey, _jsonAdapter = jsonAdapter, _dataPlaneUrl = dataPlaneUrl,
        _delegate = AnalyticsDelegate(
            settings,
            storage,
            options,
            jsonAdapter,
            shouldVerifySdk,
            sdkVerifyRetryStrategy,
            dataUploadService,
            configDownloadService,
            analyticsExecutor,
            logger,
            createContext(defaultTraits, defaultExternalIds, defaultContextMap)

        )
    )


    companion object {
        // default base url or rudder-backend-server
        private const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"

        // config-plane url to get the config for the writeKey
        private const val CONTROL_PLANE_URL = "https://api.rudderlabs.com/"

    }
    //TODO(ADD callback for messages, flush api)
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
                timestamp = Utils.timeStamp, eventName = eventName, properties = trackProperties,
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
                userId = userID, timestamp = Utils.timeStamp, category = category,
                name = screenName, properties = screenProperties
            ), options
        )
    }

    fun identify(message: IdentifyMessage, options: RudderOptions? = null) {
        //TODO(save userId)
        processMessage(message, options)
    }

    fun identify(
        userID: String, traits: Map<String, Any>? = null,
        options: RudderOptions? = null,
    ) {
        val completeTraits = mapOf("userId" to userID) optAdd traits
        identify(
            IdentifyMessage.create(
                userId = userID,
                timestamp = Utils.timeStamp,
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
            AliasMessage.create(timestamp = Utils.timeStamp, userId = newId), options
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
                timestamp = Utils.timeStamp, userId = userID,
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
        alternateExecutor: ExecutorService = Executors.newCachedThreadPool(),
        clearDb: Boolean = true
    ) {
        val dataUploadService = alternateDataUploadService ?: DataUploadServiceImpl(_writeKey,
            _jsonAdapter, dataPlaneUrl = _dataPlaneUrl)
        _delegate.forceFlush( dataUploadService
            , alternateExecutor, clearDb
        )
        //shut down if data uploader is initialized here
        if(alternateDataUploadService == null)
            dataUploadService.shutdown()
    }

}