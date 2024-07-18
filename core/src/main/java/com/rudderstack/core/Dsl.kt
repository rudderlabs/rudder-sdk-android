package com.rudderstack.core

import com.rudderstack.core.models.GroupTraits
import com.rudderstack.core.models.IdentifyTraits
import com.rudderstack.core.models.TrackProperties
import com.rudderstack.core.models.*

class TrackScope internal constructor() : MessageScope<TrackMessage>() {
    private var eventName: String? = null
    private var properties: TrackProperties? = null

    fun trackProperties(scope: MapScope<String, Any>.() -> Unit) {
        val propertiesScope = MapScope(properties)
        propertiesScope.scope()
        properties = propertiesScope.map
    }

    fun event(scope: StringScope.() -> Unit) {
        val eventScope = StringScope()
        eventScope.scope()
        eventName = eventScope.value
    }

    fun event(name: String) {
        eventName = name
    }

    override val message: TrackMessage
        get() = TrackMessage.create(
            eventName = eventName ?: throw IllegalArgumentException("No event name for track"),
            timestamp = RudderUtils.timeStamp,
            properties = properties,
        )
}

class ScreenScope internal constructor() : MessageScope<ScreenMessage>() {
    private var screenName: String? = null
    private var category: String? = null
    private var screenProperties: TrackProperties? = null

    fun screenProperties(scope: MapScope<String, Any>.() -> Unit) {
        val propertiesScope = MapScope(screenProperties)
        propertiesScope.scope()
        screenProperties = propertiesScope.map
    }

    fun screenName(scope: StringScope.() -> Unit) {
        val eventScope = StringScope()
        eventScope.scope()
        screenName = eventScope.value
    }

    fun screenName(name: String) {
        screenName = name
    }

    fun category(scope: StringScope.() -> Unit) {
        val eventScope = StringScope()
        eventScope.scope()
        category = eventScope.value
    }

    fun category(name: String) {
        category = name
    }

    override val message: ScreenMessage
        get() = ScreenMessage.create(
            name = screenName
                ?: throw IllegalArgumentException("Screen name is not provided for screen event"),
            timestamp = RudderUtils.timeStamp,
            category = category,
            properties = screenProperties
        )
}

class IdentifyScope internal constructor() : MessageScope<IdentifyMessage>() {
    private var traits: IdentifyTraits? = null
    private var userId: String? = null

    fun userId(scope: StringScope.() -> Unit) {
        val titleScope = StringScope()
        titleScope.scope()
        this.userId = titleScope.value
    }

    fun userId(userId: String) {
        this.userId = userId
    }

    fun traits(scope: MapScope<String, Any?>.() -> Unit) {
        val traitsScope = MapScope(traits)
        traitsScope.scope()
        traits = traitsScope.map
    }

    override val message: IdentifyMessage
        get() = IdentifyMessage.create(
            userId = userId,
            anonymousId = anonymousId,
            timestamp = RudderUtils.timeStamp,
            traits = traits,
        )
}

class AliasScope internal constructor() : MessageScope<AliasMessage>() {
    private var newID: String? = null

    fun newId(scope: StringScope.() -> Unit) {
        val titleScope = StringScope()
        titleScope.scope()
        newID = titleScope.value
    }

    fun newId(newId: String) {
        newID = newId
    }

    override val message: AliasMessage
        get() = AliasMessage.create(
            timestamp = RudderUtils.timeStamp,
            userId = newID,
        )
}

class GroupScope internal constructor() : MessageScope<GroupMessage>() {
    private var groupId: String? = null
    private var traits: GroupTraits? = null
    fun traits(scope: MapScope<String, Any>.() -> Unit) {
        val groupScope = MapScope(traits)
        groupScope.scope()
        traits = groupScope.map
    }

    fun groupId(scope: StringScope.() -> Unit) {
        val groupScope = StringScope()
        groupScope.scope()
        groupId = groupScope.value
    }

    fun groupId(id: String) {
        groupId = id
    }

    override val message: GroupMessage
        get() = GroupMessage.create(
            timestamp = RudderUtils.timeStamp,
            anonymousId = anonymousId,
            groupId = groupId,
            groupTraits = traits,
        )
}

@MessageScopeDslMarker
abstract class MessageScope<T : Message> internal constructor(/*private val analytics: Analytics*/) {
    private var _options: RudderOption? = null
    internal val options
        get() = _options
    protected var anonymousId: String? = null

    fun rudderOptions(scope: RudderOptionsScope.() -> Unit) {
        val optionsScope = RudderOptionsScope()
        optionsScope.scope()
        _options = optionsScope.rudderOption
    }

    fun anonymousId(scope: StringScope.() -> Unit) {
        val anonymousIdScope = StringScope()
        anonymousIdScope.scope()
        anonymousId = anonymousIdScope.value
    }

    fun anonymousId(id: String) {
        anonymousId = id
    }

    internal abstract val message: T
}

class StringScope internal constructor() {
    private var _value: String? = null
    val value
        get() = _value

    operator fun String.unaryPlus() {
        _value = this
    }
}

@OptionsScopeDslMarker
class RudderOptionsScope internal constructor() {
    //    private var rudderOptionsBuilder: RudderOptions.Builder = RudderOptions.Builder()
    internal val rudderOption: RudderOption
        get() = _rudderOption
    private val _rudderOption: RudderOption by lazy {
        RudderOption()
    }

    fun externalId(type: String, id: String) {
        _rudderOption.putExternalId(type, id)
    }

    fun integration(destinationKey: String, enabled: Boolean) {
        _rudderOption.putIntegration(destinationKey, enabled)
    }

    fun integration(destination: BaseDestinationPlugin<*>, enabled: Boolean) {
        _rudderOption.putIntegration(destination, enabled)
    }

    fun customContexts(key: String, context: Map<String?, Any?>) {
        _rudderOption.putCustomContext(key, context)
    }
}

class CollectionsScope<E>
internal constructor(private var _collection: Collection<E>?) {
    operator fun E.unaryPlus() {
        _collection = _collection?.let { it + this } ?: listOf(this)
    }

    operator fun Collection<E>.unaryPlus() {
        _collection = _collection?.let { it + this } ?: this
    }

    infix fun add(item: E) {
        +item
    }

    infix fun add(items: Collection<E>) {
        +items
    }

    internal val collection
        get() = _collection
}

class MapScope<K, V>
internal constructor(private var _map: Map<K, V>?) {
    operator fun Pair<K, V>.unaryPlus() {
        _map = _map optAdd this
    }

    operator fun Map<K, V>.unaryPlus() {
        _map = _map optAdd this
    }

    infix fun add(item: Pair<K, V>) {
        +item
    }

    infix fun add(items: Map<K, V>) {
        +items
    }

    internal val map
        get() = _map
}

@DslMarker
annotation class MessageScopeDslMarker

@DslMarker
annotation class PropertyScopeDslMarker

@DslMarker
annotation class OptionsScopeDslMarker
