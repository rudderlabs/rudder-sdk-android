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

import com.rudderstack.core.internal.AnalyticsDelegate
import com.rudderstack.core.internal.ConfigDownloadServiceImpl
import com.rudderstack.core.internal.DataUploadServiceImpl
import com.rudderstack.models.AliasMessage
import com.rudderstack.models.GroupMessage
import com.rudderstack.models.GroupTraits
import com.rudderstack.models.IdentifyMessage
import com.rudderstack.models.IdentifyProperties
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.models.MessageContext
import com.rudderstack.models.MessageDestinationProps
import com.rudderstack.models.ScreenMessage
import com.rudderstack.models.ScreenProperties
import com.rudderstack.models.TrackMessage
import com.rudderstack.models.TrackProperties
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class Analytics private constructor(
    private val _delegate: AnalyticsDelegate,
) : Controller by _delegate {
    /**
     * Contains methods for sending messages over to device mode and cloud mode destinations.
     * Developers are required to maintain the object throughout application lifetime,
     * since this implementation doesn't directly provide a Singleton Instance.
     *
     * If this SDK is intended to be used in Android, refrain from using this class directly.
     * Also in case of using this class directly, developer has to add their own context plugin,
     * to add context to messages.
     * synchronicity of events. Other executors are practically usable iff the difference between two events
     * is guaranteed to be at least a few ms.
     *
     * Only [MessageContext] passed in [IdentifyMessage] are stored in [Storage]
     */
    constructor(
        writeKey: String,
        configuration: Configuration,
        dataUploadService: DataUploadService? = null,
        configDownloadService: ConfigDownloadService? = null,
        //optional
        initializationListener: ((success: Boolean, message: String?) -> Unit)? = null,
        //optional called if shutdown is called
        shutdownHook: (() -> Unit)? = null
    ) : this(
        _delegate = AnalyticsDelegate(
            configuration, dataUploadService ?: DataUploadServiceImpl(
                writeKey
            ), configDownloadService ?: ConfigDownloadServiceImpl(
                writeKey
            ), initializationListener, shutdownHook

        )
    )


    companion object {
        // default base url or rudder-backend-server
        internal const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"

        // config-plane url to get the config for the writeKey
        internal const val CONTROL_PLANE_URL = "https://api.rudderlabs.com/"

    }

    init {
        _delegate.startup(this)
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

    @JvmOverloads
    fun track(
        eventName: String,
        options: RudderOptions? = null,
        userId: String? = null,
        anonymousId: String? = null,
        trackProperties: TrackProperties? = null,
        traits: Map<String, Any?>? = null,
        externalIds: List<Map<String, String>>? = null,
        customContextMap: Map<String, Any>? = null,
        destinationProps: MessageDestinationProps? = null,
    ) {
        track(
            TrackMessage.create(
                anonymousId = anonymousId,
                traits = traits,
                externalIds = externalIds,
                customContextMap = customContextMap,
                destinationProps = destinationProps,
                timestamp = RudderUtils.timeStamp,
                eventName = eventName,
                properties = trackProperties,
                userId = userId
            ), options
        )

    }

    /**
     * DSL format for track call
     *
     * analytics.track {
     *   event { +"event" }
     *   //or event("event")
     *   trackProperties {
     *       //use any of these
     *       +("property1" to "value1")
     *       +mapOf("property2" to "value2")
     *       add("property3" to "value3")
     *       add(mapOf("property4" to "value4"))
     *  }
     *  userId("user_id")
     *  rudderOptions {
     *       customContexts {
     *          +("cc1" to "cp1")
     *          +("cc2" to "cp2")
     *       }
     *       externalIds {
     *          +(mapOf("ext-1" to "ex1"))
     *          +(mapOf("ext-2" to "ex2"))
     *          +listOf(mapOf("ext-3" to "ex3"))
     *       }
     *       integrations {
     *          +("firebase" to true)
     *          +("amplitude" to false)
     *       }
     *    }
     * }
     * @param scope
     */
    fun track(scope: TrackScope.() -> Unit) {
        val trackScope = TrackScope()
        trackScope.scope()
        track(trackScope.message, trackScope.options)
    }

    fun screen(message: ScreenMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun screen(
        screenName: String,
        category: String,
        screenProperties: ScreenProperties,
        anonymousId: String? = null,
        userId: String? = null,
        options: RudderOptions? = null,
        destinationProps: MessageDestinationProps? = null,
        traits: Map<String, Any?>? = null,
        externalIds: List<Map<String, String>>? = null,
        customContextMap: Map<String, Any>? = null,
    ) {
        screen(
            ScreenMessage.create(
                userId = userId,
                anonymousId = anonymousId,
                destinationProps = destinationProps,
                externalIds = externalIds,
                customContextMap = customContextMap,
                traits = traits,
                timestamp = RudderUtils.timeStamp,
                category = category,
                name = screenName,
                properties = screenProperties
            ), options
        )
    }

    fun screen(scope: ScreenScope.() -> Unit) {
        val screenScope = ScreenScope()
        screenScope.scope()
        screen(screenScope.message, screenScope.options)
    }

    fun identify(message: IdentifyMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun identify(
        userId: String, traits: IdentifyTraits? = null,
        anonymousId: String? = null,
        options: RudderOptions? = null,
        properties: IdentifyProperties? = null,
        destinationProps: MessageDestinationProps? = null,
        externalIds: List<Map<String, String>>? = null,
        customContextMap: Map<String, Any>? = null,
    ) {
        val completeTraits = mapOf("userId" to userId) optAdd traits
        identify(
            IdentifyMessage.create(
                userId = userId,
                anonymousId = anonymousId,
                destinationProps = destinationProps,
                timestamp = RudderUtils.timeStamp,
                traits = completeTraits,
                properties = properties,
                externalIds = externalIds,
                customContextMap = customContextMap
            ), options
        )
    }

    fun identify(scope: IdentifyScope.() -> Unit) {
        val identifyScope = IdentifyScope()
        identifyScope.scope()
        identify(identifyScope.message, identifyScope.options)
    }

    fun alias(message: AliasMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun alias(
        newId: String,
        anonymousId: String? = null,
        options: RudderOptions? = null,
        destinationProps: MessageDestinationProps? = null,
        previousId: String? = null,
        externalIds: List<Map<String, String>>? = null,
        customContextMap: Map<String, Any>? = null,
    ) {
        val completeTraits = mapOf("userId" to newId)
        alias(
            AliasMessage.create(timestamp = RudderUtils.timeStamp,
                anonymousId = anonymousId,
                previousId=previousId,
                destinationProps = destinationProps,
                externalIds = externalIds,
                customContextMap = customContextMap,

                userId = newId, traits = completeTraits),
            options
        )
    }

    fun alias(scope: AliasScope.() -> Unit) {
        val aliasScope = AliasScope()
        aliasScope.scope()
        alias(aliasScope.message, aliasScope.options)
    }

    fun group(message: GroupMessage, options: RudderOptions? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun group(
        groupId: String?,
        anonymousId: String? = null,
        userId: String? = null,
        options: RudderOptions? = null,
        groupTraits: GroupTraits?,

        destinationProps: MessageDestinationProps? = null,

        externalIds: List<Map<String, String>>? = null,
        customContextMap: Map<String, Any>? = null,

    ) {
        group(
            GroupMessage.create(
                timestamp = RudderUtils.timeStamp,
                userId = userId,
                groupId = groupId,
                groupTraits = groupTraits,
                anonymousId = anonymousId,
                destinationProps = destinationProps,
                externalIds = externalIds,
                customContextMap = customContextMap
            ), options
        )
    }

    fun group(scope: GroupScope.() -> Unit) {
        val groupScope = GroupScope()
        groupScope.scope()
        group(groupScope.message, groupScope.options)
    }

    /**
     * Flush the remaining data from storage.
     * However flush returns immediately if  analytics is shutdown
     */
    fun flush() {
        _delegate.flush()
    }

    /**
     * Flushes forcefully, not if analytics has shutdown.
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
     * @param alternateFlushExecutor The flush will be processed on this [ExecutorService]
     * @param clearDb Uploaded data will be cleared from [Storage] if true, else not.
     */
    @JvmOverloads
    fun forceFlush(
        clearDb: Boolean = true,
        alternateFlushExecutor: ExecutorService? = null,
        alternateDataUploadService: DataUploadService? = null
    ) {
        currentConfiguration ?: return
        val flushExecutor = alternateFlushExecutor ?: ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue<Runnable>(1),
            ThreadPoolExecutor.DiscardOldestPolicy()
        )
        val dataUploadService = alternateDataUploadService ?: dataUploadService
        _delegate.forceFlush(
            dataUploadService, flushExecutor, clearDb
        ) {
            //shut down if data uploader/executor is initialized here
            if (alternateDataUploadService == null) dataUploadService.shutdown()
            if (alternateFlushExecutor == null) flushExecutor.shutdown()
        }
    }

    /**
     * This blocks the thread till events are flushed.
     * Users should prefer [forceFlush]
     *
     * @param alternateDataUploadService Should be sent as null unless separate implementation is provided
     * @param clearDb true to clear database after a successful flush, false otherwise
     * @param base64Generator To be provided in case any separate implementation is required other than the one used to
     * initialize the class.
     *
     */
    @JvmOverloads
    fun blockingFlush(
        clearDb: Boolean = true,
        alternateDataUploadService: DataUploadService? = null,
    ): Boolean {

        val dataUploadService = alternateDataUploadService ?: dataUploadService
        return _delegate.blockFlush(dataUploadService, clearDb)
    }


}