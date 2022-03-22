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
import com.rudderstack.android.core.internal.AnalyticsDelegate
import com.rudderstack.android.core.internal.BasicStorageImpl
import com.rudderstack.android.core.internal.ConfigDownloadServiceImpl
import com.rudderstack.android.core.internal.DataUploadServiceImpl
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.*
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
 * TODO
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
class Analytics(
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

    ) : Controller by AnalyticsDelegate(
//    writeKey,
    settings,
    storage,
    options,
    jsonAdapter,
    shouldVerifySdk,
    sdkVerifyRetryStrategy,
    dataUploadService,
    configDownloadService,
    analyticsExecutor,
    logger

) {


    companion object {
        // default base url or rudder-backend-server
        private const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"

        // config-plane url to get the config for the writeKey
        private const val CONTROL_PLANE_URL = "https://api.rudderlabs.com/"

    }

    fun track(message: TrackMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    fun track(eventName: String, trackProperties: TrackProperties, options: RudderOptions? = null) {
        track(TrackMessage(null,  null, null,
        Utils.timeStamp, eventName = eventName, properties = trackProperties), options)

    }

    fun screen(message: ScreenMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }
    fun screen(
        screenName: String,
        category: String,
        screenProperties: ScreenProperties,
        options: RudderOptions? = null
    ) {
        screen(
            ScreenMessage(null,
        null, null, Utils.timeStamp, category = category,
                name = screenName, properties = screenProperties), options
        )
    }

    fun identify(message: IdentifyMessage, options: RudderOptions? = null) {
        //TODO(save userId)
        processMessage(message, options)
    }

    fun identify(userID: String, traits: Map<String, Any>? = null, options: RudderOptions? = null) {
        val completeTraits = (traits?: mapOf()) + mapOf("userId" to userID)
        identify(
            IdentifyMessage(null, null, null, Utils.timeStamp, traits = completeTraits, ), options
        )
    }

    fun alias(message: AliasMessage, options: RudderOptions? = null) {
        //TODO(change userId)
        processMessage(message, options)
    }
    fun alias(newId: String, options: RudderOptions? = null) {
//        alias(
//            AliasMessage(timestamp = Utils.timeStamp, )
//        )
    }

    fun group(message: GroupMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }
    fun group(groupID: String, traits: GroupTraits? = null, options: RudderOptions? = null) {
        group(GroupMessage(null, null, null, Utils.timeStamp, null,
        groupID, traits), options)
    }


}