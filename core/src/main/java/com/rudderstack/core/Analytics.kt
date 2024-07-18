package com.rudderstack.core

import com.rudderstack.core.internal.AnalyticsDelegate
import com.rudderstack.core.internal.ConfigDownloadServiceImpl
import com.rudderstack.core.internal.DataUploadServiceImpl
import com.rudderstack.core.models.AliasMessage
import com.rudderstack.core.models.GroupMessage
import com.rudderstack.core.models.GroupTraits
import com.rudderstack.core.models.IdentifyMessage
import com.rudderstack.core.models.IdentifyTraits
import com.rudderstack.core.models.MessageContext
import com.rudderstack.core.models.ScreenMessage
import com.rudderstack.core.models.ScreenProperties
import com.rudderstack.core.models.TrackMessage
import com.rudderstack.core.models.TrackProperties

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
        storage: Storage? = null,
        //optional
        initializationListener: ((success: Boolean, message: String?) -> Unit)? = null,
        //optional called if shutdown is called
        shutdownHook: (Analytics.() -> Unit)? = null
    ) : this(
        _delegate = AnalyticsDelegate(
            configuration = configuration,
            storage = storage ?: BasicStorageImpl(),
            writeKey = writeKey,
            dataUploadService = dataUploadService ?: DataUploadServiceImpl(
                writeKey
            ),
            configDownloadService = configDownloadService ?: ConfigDownloadServiceImpl(
                writeKey
            ),
            initializationListener = initializationListener,
            shutdownHook = shutdownHook,
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
    fun track(message: TrackMessage, options: RudderOption? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun track(
        eventName: String,
        trackProperties: TrackProperties? = null,
        options: RudderOption? = null,
    ) {
        track(
            TrackMessage.create(
                eventName = eventName,
                properties = trackProperties,
                timestamp = RudderUtils.timeStamp,
            ), options
        )
    }

    /**
     * DSL format for track call
     *
     * ```kotlin
     * analytics.track {
     *     event { +"event" }
     *     // or event("event")
     *     trackProperties {
     *         // use any of these
     *         +("property1" to "value1")
     *         +mapOf("property2" to "value2")
     *         add("property3" to "value3")
     *         add(mapOf("property4" to "value4"))
     *     }
     *     rudderOptions {
     *         customContexts("cc1", mapOf("cc_1_1" to "ccv"))
     *         integration("firebase", true)
     *     }
     * }
     * ```
     *
     * @param scope
     */
    fun track(scope: TrackScope.() -> Unit) {
        val trackScope = TrackScope()
        trackScope.scope()
        track(trackScope.message, trackScope.options)
    }

    fun screen(message: ScreenMessage, options: RudderOption? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun screen(
        screenName: String,
        category: String? = null,
        screenProperties: ScreenProperties,
        options: RudderOption? = null,
    ) {
        screen(
            ScreenMessage.create(
                name = screenName,
                category = category,
                properties = screenProperties,
                timestamp = RudderUtils.timeStamp,
            ), options
        )
    }

    fun screen(scope: ScreenScope.() -> Unit) {
        val screenScope = ScreenScope()
        screenScope.scope()
        screen(screenScope.message, screenScope.options)
    }

    fun identify(message: IdentifyMessage, options: RudderOption? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun identify(
        userId: String,
        traits: IdentifyTraits? = null,
        options: RudderOption? = null,
    ) {
        val completeTraits = mapOf("userId" to userId) optAdd traits
        identify(
            IdentifyMessage.create(
                userId = userId,
                traits = completeTraits,
                timestamp = RudderUtils.timeStamp,
            ), options
        )
    }

    fun identify(scope: IdentifyScope.() -> Unit) {
        val identifyScope = IdentifyScope()
        identifyScope.scope()
        identify(identifyScope.message, identifyScope.options)
    }

    fun alias(message: AliasMessage, options: RudderOption? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun alias(
        newId: String,
        options: RudderOption? = null,
    ) {
        val completeTraits = mapOf("userId" to newId)
        alias(
            AliasMessage.create(
                userId = newId,
                traits = completeTraits,
                timestamp = RudderUtils.timeStamp,
            ),
            options
        )
    }

    fun alias(scope: AliasScope.() -> Unit) {
        val aliasScope = AliasScope()
        aliasScope.scope()
        alias(aliasScope.message, aliasScope.options)
    }

    fun group(message: GroupMessage, options: RudderOption? = null) {
        processMessage(message, options)
    }

    @JvmOverloads
    fun group(
        groupId: String?,
        groupTraits: GroupTraits?,
        options: RudderOption? = null,
    ) {
        group(
            GroupMessage.create(
                groupId = groupId,
                groupTraits = groupTraits,
                timestamp = RudderUtils.timeStamp,
            ), options
        )
    }

    fun group(scope: GroupScope.() -> Unit) {
        val groupScope = GroupScope()
        groupScope.scope()
        group(groupScope.message, groupScope.options)
    }
}
