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

import com.rudderstack.android.core.internal.AnalyticsDelegate
import com.rudderstack.android.core.internal.DataUploadServiceImpl
import com.rudderstack.android.core.internal.BasicStorageImpl
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
    shouldVerifySdk : Boolean = true,
    options: RudderOptions = RudderOptions.default(),
    dataPlaneUrl: String = DATA_PLANE_URL,
    controlPlaneUrl: String = CONTROL_PLANE_URL,
    storage: Storage = BasicStorageImpl(),
    analyticsExecutor: ExecutorService = Executors.newCachedThreadPool(),
    networkExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
    dataUploadService: DataUploadService = DataUploadServiceImpl(
        writeKey,
        jsonAdapter,
        SettingsState,
        dataPlaneUrl,
        networkExecutor
    )
) : Controller by AnalyticsDelegate(
//    writeKey,
    settings,
    storage,
    options,
    jsonAdapter,
    shouldVerifySdk,
    RetryStrategy.exponential(),
    dataUploadService,

    /*controlPlaneUrl,
    shouldVerifySdk,
    analyticsExecutor,
    networkExecutor*/
) {


    companion object {
        // default base url or rudder-backend-server
        private const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"

        // config-plane url to get the config for the writeKey
        private const val CONTROL_PLANE_URL = "https://api.rudderlabs.com/"

    }

    fun track(message: TrackMessage, options: RudderOptions? = null) {
        processMessage(message,options)
    }

    fun track(eventName: String, properties: Map<String, Any>, options: RudderOptions? = null) {
//        track(TrackMessage())

    }
    fun screen(message: ScreenMessage, options: RudderOptions? = null) {}
    fun screen(
        screenName: String,
        category: String,
        properties: Map<String, Any>,
        options: RudderOptions? = null
    ) {
    }

    fun identify(message: IdentifyMessage, options: RudderOptions? = null) {
    }

    fun identify(userID: String, traits: Map<String, Any>? = null, options: RudderOptions? = null) {
    }

    fun alias(message: AliasMessage, options: RudderOptions? = null) {}
    fun alias(newId: String, options: RudderOptions? = null) {}

    fun group(message: GroupMessage, options: RudderOptions? = null) {}
    fun group(groupID: String, traits: Map<String, Any>? = null, options: RudderOptions? = null) {}
    private fun processMessage(message: Message) {

    }

}