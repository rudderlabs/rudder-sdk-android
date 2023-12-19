/*
 * Creator: Debanjan Chatterjee on 09/10/22, 12:00 PM Last modified: 09/10/22, 12:00 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

import com.rudderstack.models.*

class TrackScope internal constructor() : MessageScope<TrackMessage>() {
    private var eventName : String? = null
    private var trackProperties: TrackProperties? = null
    private var userID: String? = null

    fun trackProperties(scope: MapScope<String,Any>.() -> Unit){
        val propertiesScope = MapScope(trackProperties)
        propertiesScope.scope()
        trackProperties = propertiesScope.map
    }
    fun userId(scope: StringScope.() -> Unit){
        val titleScope = StringScope()
        titleScope.scope()
        userID = titleScope.value
    }
    fun userId(userId : String){
        userID = userId
    }
    fun event(scope: StringScope.() -> Unit){
        val eventScope = StringScope()
        eventScope.scope()
        eventName = eventScope.value
    }
    fun event(name: String){
        eventName = name
    }
    override val message: TrackMessage
        get() = TrackMessage.create(eventName?: throw IllegalArgumentException("No event name for track"),
        RudderUtils.timeStamp, trackProperties, userId = userID, destinationProps = destinationProperties)

}
class ScreenScope internal constructor() : MessageScope<ScreenMessage>() {
    private var screenName : String? = null
    private var category : String? = null
    private var screenProperties: TrackProperties? = null
    private var userID: String? = null

    fun screenProperties(scope: MapScope<String,Any>.() -> Unit){
        val propertiesScope = MapScope(screenProperties)
        propertiesScope.scope()
        screenProperties = propertiesScope.map
    }
    fun userId(scope: StringScope.() -> Unit){
        val titleScope = StringScope()
        titleScope.scope()
        userID = titleScope.value
    }
    fun userId(userId : String){
        userID = userId
    }
    fun screenName(scope: StringScope.() -> Unit){
        val eventScope = StringScope()
        eventScope.scope()
        screenName = eventScope.value
    }
    fun screenName(name: String){
        screenName = name
    }
    fun category(scope: StringScope.() -> Unit){
        val eventScope = StringScope()
        eventScope.scope()
        category = eventScope.value
    }
    fun category(name: String){
        category = name
    }
    override val message: ScreenMessage
        get() = ScreenMessage.create(RudderUtils.timeStamp, name = screenName, category = category,
        properties = screenProperties, userId = userID, destinationProps = destinationProperties)

}
class IdentifyScope internal constructor() : MessageScope<IdentifyMessage>() {
    private var traits: IdentifyTraits? = null
    private var userID: String? = null

    fun traits(scope: MapScope<String,Any?>.() -> Unit){
        val traitsScope = MapScope(traits)
        traitsScope.scope()
        traits = traitsScope.map
    }
    fun userId(scope: StringScope.() -> Unit){
        val titleScope = StringScope()
        titleScope.scope()
        userID = titleScope.value
    }
    fun userId(userId : String){
        userID = userId
    }

    override val message: IdentifyMessage
        get() = IdentifyMessage.create(
            userId = userID,
            timestamp = RudderUtils.timeStamp,
            traits = traits,
         destinationProps = destinationProperties)

}
class AliasScope internal constructor() : MessageScope<AliasMessage>() {
    private var newID: String? = null


    fun newId(scope: StringScope.() -> Unit){
        val titleScope = StringScope()
        titleScope.scope()
        newID = titleScope.value
    }
    fun newId(newId : String){
        newID = newId
    }

    override val message: AliasMessage
        get() = AliasMessage.create(timestamp = RudderUtils.timeStamp, userId = newID,
         destinationProps = destinationProperties)

}
class GroupScope internal constructor() : MessageScope<GroupMessage>() {
    private var groupId : String? = null
    private var traits: GroupTraits? = null
    private var userID: String? = null

    fun traits(scope: MapScope<String,Any>.() -> Unit){
        val groupScope = MapScope(traits)
        groupScope.scope()
        traits = groupScope.map
    }
    fun userId(scope: StringScope.() -> Unit){
        val titleScope = StringScope()
        titleScope.scope()
        userID = titleScope.value
    }
    fun userId(userId : String){
        userID = userId
    }
    fun groupId(scope: StringScope.() -> Unit){
        val groupScope = StringScope()
        groupScope.scope()
        groupId = groupScope.value
    }
    fun groupId(id: String){
        groupId = id
    }

    override val message: GroupMessage
        get() = GroupMessage.create(
            timestamp = RudderUtils.timeStamp, userId = userID,
            groupId = groupId, groupTraits = traits
            , destinationProps = destinationProperties)

}

@MessageScopeDslMarker
abstract class MessageScope<T : Message> internal constructor(/*private val analytics: Analytics*/) {
    private var _options: RudderOptions? = null
    internal val options
        get() = _options

    private var _destinationProperties: MessageDestinationProps? = null
    protected val destinationProperties
    get() = _destinationProperties

    fun rudderOptions(scope: RudderOptionsScope.() -> Unit) {
        val optionsScope = RudderOptionsScope()
        optionsScope.scope()
        _options = optionsScope.rudderOptions
    }

    fun destinationProperties(scope: MapScope<String, Map<*, *>>.() -> Unit){
        val destinationPropsScope = MapScope(_destinationProperties)
        destinationPropsScope.scope()
        _destinationProperties = destinationPropsScope.map
    }

    internal abstract val message: T
    /*internal fun send(){
        analytics.processMessage(message, options)
    }*/

}

class StringScope internal constructor(){
    private var _value : String? = null
    val value
    get() = _value

    operator fun String.unaryPlus(){
        _value = this
    }
}
@OptionsScopeDslMarker
class RudderOptionsScope internal constructor() {

    //    private var rudderOptionsBuilder: RudderOptions.Builder = RudderOptions.Builder()
    internal val rudderOptions: RudderOptions?
        get() {
            externalIds ?: integrations ?: customContexts ?: return null
            return RudderOptions.Builder().also { builder ->
                externalIds?.let { builder.withExternalIds(it) }
                integrations?.let { builder.withIntegrations(it) }
                customContexts?.let { builder.withCustomContexts(it) }
            }.build()
        }

    private var externalIds: List<Map<String, String>>? = null
    private var integrations: MessageIntegrations? = null
    private var customContexts: Map<String, Any>? = null

    fun externalIds(scope: CollectionsScope<Map<String, String>>.() -> Unit) {
        val externalIdsScope = CollectionsScope(externalIds)
        externalIdsScope.scope()
        externalIds = externalIdsScope.collection?.toList()
    }

    fun integrations(scope: MapScope<String, Boolean>.() -> Unit) {
        val integrationsScope = MapScope(integrations)
        integrationsScope.scope()
        integrations = integrationsScope.map
    }

    fun customContexts(scope: MapScope<String, Any>.() -> Unit) {
        val customContextsScope = MapScope(customContexts)
        customContextsScope.scope()
        customContexts = customContextsScope.map
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
