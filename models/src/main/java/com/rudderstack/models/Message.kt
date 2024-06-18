/*
 * Creator: Debanjan Chatterjee on 06/12/21, 8:01 PM Last modified: 06/12/21, 8:01 PM
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
@file:Suppress("FunctionName")

package com.rudderstack.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.rudderstack.models.Message.EventType
import com.squareup.moshi.Json
import java.util.Locale
import java.util.UUID

typealias MessageContext = Map<String, Any?>
typealias PageProperties = Map<String, Any>
typealias ScreenProperties = Map<String, Any>
typealias TrackProperties = Map<String, Any>
typealias IdentifyTraits = Map<String, Any?>
typealias IdentifyProperties = Map<String, Any>

// integrations might change from String,Boolean to String, Object at a later point of time
typealias MessageIntegrations = Map<String, Boolean>

typealias MessageDestinationProps = Map<String, Map<*, *>>
typealias GroupTraits = Map<String, Any>

@JsonIgnoreProperties("type\$models")
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class Message(

    /**
     * @return Type of event
     * @see EventType
     */
    @SerializedName("type")
    // @Expose
    @param:JsonProperty("type") @field:JsonProperty("type") @get:JsonProperty("type") @Json(name = "type") internal var type: EventType,

    // @Expose
    // convert to json to put any object as value
    @SerializedName("context") @JsonProperty("context") @Json(name = "context") val context: MessageContext? = null,

    // @Expose
    @SerializedName("anonymousId") @JsonProperty("anonymousId") @Json(name = "anonymousId") var anonymousId: String?,

    /** @return User ID for the event */
    // @Expose
    @SerializedName("userId") @JsonProperty("userId") @Json(name = "userId") var userId: String? = null,

    // format - yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    // @Expose
    @SerializedName("originalTimestamp") @field:JsonProperty("originalTimestamp") @get:JsonProperty(
        "originalTimestamp"
    ) @Json(name = "originalTimestamp") val timestamp: String,

    // @Expose
    @SerializedName("destinationProps") @JsonProperty("destinationProps") @Json(name = "destinationProps") val destinationProps: MessageDestinationProps? = null,

//    @Transient
    _messageId: String? = null,
    _channel: String? = null,
) {
    // @Expose
    @SerializedName("messageId")
    @JsonProperty("messageId")
    @Json(name = "messageId")
    val messageId: String = _messageId ?: String.format(
        Locale.US,
        "%d-%s",
        System.currentTimeMillis(),
        UUID.randomUUID().toString(),
    )

    // ugly hack for moshi
    // https://github.com/square/moshi/issues/609#issuecomment-798805367
    @Json(name = "channel")
    @JsonProperty("channel")
    @SerializedName("channel")
    var channel: String = _channel ?: "server"
        set(value) {
            field = value
        }
        get() = field ?: "server" // required for gson, cause it might set the property null
    // through reflection

    @JsonIgnore
    fun getType() = type

    /**
     * For internal use. Any value set over here will be overwritten
     * internally. For setting custom configuration for integrations for a
     * particular message, use RudderOptions
     */
    // @Expose
    @SerializedName("integrations")
    @JsonProperty("integrations")
    @Json(name = "integrations")
    var integrations: MessageIntegrations? = null

    /**
     * For internal usage. This variable will return null when called.
     * Setting this variable will make no difference as this value will be overridden while
     * syncing the data with server.
     */
    @SerializedName("sentAt")
    @JsonProperty("sentAt")
    @Json(name = "sentAt")
    var sentAt: String?= null
    open fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
    ): Message = when (this) {
        is AliasMessage -> copy(
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            previousId,
        )

        is GroupMessage -> copy(
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            groupId,
            traits,
        )

        is IdentifyMessage -> copy(
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            properties,
        )

        is PageMessage -> copy(
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            name,
            properties,
            category,
        )

        is ScreenMessage -> copy(
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            properties = properties,
        )

        is TrackMessage -> copy(
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            eventName,
            properties,
        )
    }.also {
        it.integrations = integrations
        it.channel = channel
    }

    enum class EventType(val value: String) {
        @SerializedName("alias")
        @JsonProperty("alias")
        @Json(name = "alias")
        ALIAS("alias"),

        @SerializedName("group")
        @JsonProperty("group")
        @Json(name = "group")
        GROUP("group"),

        @SerializedName("page")
        @JsonProperty("page")
        @Json(name = "page")
        PAGE("page"),

        @SerializedName("screen")
        @JsonProperty("screen")
        @Json(name = "screen")
        SCREEN("screen"),

        @SerializedName("track")
        @JsonProperty("track")
        @Json(name = "track")
        TRACK("track"),

        @SerializedName("identify")
        @JsonProperty("identify")
        @Json(name = "identify")
        IDENTIFY("identify"), ;

        companion object {
            fun fromValue(value: String) = when (value.lowercase()) {
                "alias" -> EventType.ALIAS
                "group" -> EventType.GROUP
                "page" -> EventType.PAGE
                "screen" -> EventType.SCREEN
                "track" -> EventType.TRACK
                "identify" -> EventType.IDENTIFY
                else -> throw IllegalArgumentException("Wrong value for event type")
            }
        }
    }

    override fun toString(): String {
        return "type = $type, " + "messageId = $messageId, " + "context = $context, " + "anonymousId = $anonymousId, " + "userId = $userId, " + "timestamp = $timestamp, " + "destinationProps = $destinationProps, " + "integrations = $integrations, " + "channel = $channel"
    }

    override fun equals(other: Any?): Boolean {
        return other is Message && other.type === this.type && other.messageId == this.messageId && other.context == this.context && other.anonymousId == this.anonymousId && other.userId == this.userId && other.timestamp == this.timestamp && other.destinationProps == this.destinationProps && other.integrations == this.integrations && other.channel == this.channel
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + (context?.hashCode() ?: 0)
        result = 31 * result + anonymousId.hashCode()
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (destinationProps?.hashCode() ?: 0)
        result = 31 * result + integrations.hashCode()
        return result
    }
}

class AliasMessage internal constructor(

    @JsonProperty("context") @Json(name = "context") context: MessageContext? = null,
    @JsonProperty("anonymousId") @Json(name = "anonymousId") anonymousId: String?,
    @JsonProperty("userId") @Json(name = "userId") userId: String? = null,
    @JsonProperty("originalTimestamp") @Json(name = "originalTimestamp") timestamp: String,

    @JsonProperty("destinationProps") @Json(name = "destinationProps") destinationProps: MessageDestinationProps? = null,
    @SerializedName("previousId") @JsonProperty("previousId") @Json(name = "previousId") var previousId: String? = null,
    @JsonProperty("not_applicable", required = false) // work-around to ignore value param
    // jackson serialisation
    _messageId: String? = null,
) : Message(
    EventType.ALIAS,
    context,
    anonymousId,
    userId,
    timestamp,
    destinationProps,
    _messageId,
) {
    companion object {
        @JvmStatic
        fun create(
            timestamp: String,

            anonymousId: String? = null,
            userId: String? = null,

            destinationProps: MessageDestinationProps? = null,
            previousId: String? = null,
            traits: Map<String, Any?>? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null,
            _messageId: String? = null,

            ) = AliasMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            previousId,
            _messageId,
        )
    }

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,

        destinationProps: MessageDestinationProps? = this.destinationProps,
        previousId: String? = this.previousId,

        ) = AliasMessage(
        context,
        anonymousId,
        userId,
        timestamp,
        destinationProps,
        previousId,
        _messageId = this.messageId,
    )

    override fun toString(): String {
        return "${super.toString()}, " + "previousId = $previousId"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is AliasMessage && other.previousId == previousId
    }

    override fun hashCode(): Int {
        return super.hashCode() * (previousId?.hashCode() ?: 1)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class GroupMessage internal constructor(

    @JsonProperty("context") @Json(name = "context") context: MessageContext? = null,
    @JsonProperty("anonymousId") @Json(name = "anonymousId") anonymousId: String?,
    @JsonProperty("userId") @Json(name = "userId") userId: String? = null,
    @JsonProperty("originalTimestamp") @Json(name = "originalTimestamp") timestamp: String,

    @JsonProperty("destinationProps") @Json(name = "destinationProps") destinationProps: MessageDestinationProps? = null,
    /** @return Group ID for the event */
    @SerializedName("groupId") @JsonProperty("groupId") @Json(name = "groupId") var groupId: String? = null,

    @SerializedName("traits") @JsonProperty("traits") @Json(name = "traits") val traits: GroupTraits? = null,
    @JsonProperty("not_applicable", required = false) // work-around to ignore value param
    // jackson serialisation
    _messageId: String? = null,

    ) : Message(
    EventType.GROUP,
    context,
    anonymousId,
    userId,
    timestamp,
    destinationProps,
    _messageId,
) {
    companion object {
        @JvmStatic
        fun create(
            anonymousId: String? = null,
            userId: String? = null,
            timestamp: String,

            destinationProps: MessageDestinationProps? = null,
            groupId: String?,
            groupTraits: GroupTraits?,

            traits: Map<String, Any?>? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null,
            _messageId: String? = null,
        ) = GroupMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            groupId,
            groupTraits,
            _messageId,
        )
    }

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,

        destinationProps: MessageDestinationProps? = this.destinationProps,
        groupId: String? = this.groupId,
        traits: GroupTraits? = this.traits,
    ) = GroupMessage(
        context,
        anonymousId,
        userId,
        timestamp,
        destinationProps,
        groupId,
        traits,
        _messageId = this.messageId,
    )

    override fun toString(): String {
        return "${super.toString()}, " + "groupId = $groupId, " + "traits = $traits"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is GroupMessage && other.groupId == groupId && other.traits == traits
    }

    override fun hashCode(): Int {
        var result = groupId?.hashCode() ?: 0
        result = 31 * result + (traits?.hashCode() ?: 0)
        return result
    }
}

class PageMessage internal constructor(

    @JsonProperty("context") @Json(name = "context") context: MessageContext? = null,
    @JsonProperty("anonymousId") @Json(name = "anonymousId") anonymousId: String?,
    @JsonProperty("userId") @Json(name = "userId") userId: String? = null,
    @JsonProperty("originalTimestamp") @Json(name = "originalTimestamp") timestamp: String,

    @JsonProperty("destinationProps") @Json(name = "destinationProps") destinationProps: MessageDestinationProps? = null,
    /** @return Name of the event tracked */

    @SerializedName("event") @get:JsonProperty("event") @field:JsonProperty("event") @param:JsonProperty(
        "event"
    ) @Json(name = "event") var name: String? = null,

    /**
     * Get the properties back as set to the event Always convert objects to
     * it's json equivalent before setting it as values
     *
     * @return Map of String-Object
     */

    @SerializedName("properties") @JsonProperty("properties") @Json(name = "properties") val properties: PageProperties? = null,

    @SerializedName("category") @JsonProperty("category") @Json(name = "category") val category: String? = null,
    @JsonProperty("not_applicable", required = false) // work-around to ignore value param
    // jackson serialisation
    _messageId: String? = null,
) : Message(
    EventType.PAGE,
    context,
    anonymousId,
    userId,
    timestamp,
    destinationProps,
    _messageId,
) {
    companion object {
        @JvmStatic
        fun create(
            anonymousId: String? = null,
            userId: String? = null,
            timestamp: String,
            destinationProps: MessageDestinationProps? = null,
            name: String? = null,
            properties: PageProperties? = null,
            category: String? = null,
            traits: Map<String, Any?>? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null,
            _messageId: String? = null,
        ) = PageMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId, userId, timestamp, destinationProps, name, properties,
            category, _messageId,
        )
    }

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,
        destinationProps: MessageDestinationProps? = this.destinationProps,
        name: String? = this.name,
        properties: PageProperties? = this.properties,
        category: String? = this.category,

        ) = PageMessage(
        context, anonymousId, userId, timestamp, destinationProps, name, properties,
        category, _messageId = this.messageId,
    )

    override fun toString(): String {
        return "${super.toString()}, " + "name = $name, " + "properties = $properties, " + "category = $category"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is PageMessage && other.name == name && other.properties == properties && other.category == category
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (properties?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        return result
    }
}

class ScreenMessage internal constructor(

    @JsonProperty("context") @Json(name = "context") context: MessageContext? = null,
    @JsonProperty("anonymousId") @Json(name = "anonymousId") anonymousId: String?,
    @JsonProperty("userId") @Json(name = "userId") userId: String? = null,
    @JsonProperty("originalTimestamp") @Json(name = "originalTimestamp") timestamp: String,

    @JsonProperty("destinationProps") @Json(name = "destinationProps") destinationProps: MessageDestinationProps? = null,

    /**
     * Get the properties back as set to the event Always convert objects to
     * it's json equivalent before setting it as values
     *
     * @return Map of String-Object
     */

    @SerializedName("properties") @JsonProperty("properties") @Json(name = "properties") val properties: ScreenProperties? = null,
    @JsonProperty("not_applicable", required = false) // work-around to ignore value param
    // jackson serialisation
    _messageId: String? = null,

    ) : Message(
    EventType.SCREEN,
    context,
    anonymousId,
    userId,
    timestamp,
    destinationProps,
    _messageId,
) {
    companion object {
        @JvmStatic
        fun create(

            timestamp: String,
            anonymousId: String? = null,
            userId: String? = null,

            destinationProps: MessageDestinationProps? = null,
            name: String? = null,
            category: String? = null,
            properties: ScreenProperties? = null,

            traits: Map<String, Any?>? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null,
            _messageId: String? = null,
        ) = ScreenMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId, userId, timestamp, destinationProps,
            (properties ?: ScreenProperties()).let {
                if (name != null) it.plus("name" to name) else it
            }.let {
                if (category != null) it.plus("category" to category) else it
            },
            _messageId,
        )
    }

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,
        destinationProps: MessageDestinationProps? = this.destinationProps,
        name: String? = this.properties?.get("name") as String?,
        category: String? = this.properties?.get("category") as String?,
        properties: ScreenProperties? = this.properties,

        ) = ScreenMessage(
        context, anonymousId, userId, timestamp, destinationProps,
        (properties ?: ScreenProperties()).let {
            if (name != null) it.plus("name" to name) else it
        }.let {
            if (category != null) it.plus("category" to category) else it
        },
        _messageId = this.messageId,
    )

    override fun toString(): String {
        return "${super.toString()}, " + "properties = $properties"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is ScreenMessage && other.properties == properties
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (properties?.hashCode() ?: 0)
        return result
    }
}

@JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true)
class TrackMessage internal constructor(

    @JsonProperty("context") @Json(name = "context") context: MessageContext? = null,
    @JsonProperty("anonymousId") @Json(name = "anonymousId") anonymousId: String?,
    @JsonProperty("userId") @Json(name = "userId") userId: String? = null,
    @JsonProperty("originalTimestamp") @Json(name = "originalTimestamp")
    timestamp: String,
    /*channel: String? = null,*/
    @JsonProperty("destinationProps") @Json(name = "destinationProps") destinationProps: MessageDestinationProps? = null,
    /** @return Name of the event tracked */

    @SerializedName("event") @field:JsonProperty("event") @param:JsonProperty("event") @get:JsonProperty(
        "event"
    ) @Json(name = "event") val eventName: String? = null,
    /**
     * Get the properties back as set to the event Always convert objects to
     * it's json equivalent before setting it as values
     *
     * @return Map of String-Object
     */

    @SerializedName("properties") @field:JsonProperty("properties") @param:JsonProperty("properties") @get:JsonProperty(
        "properties"
    ) @Json(name = "properties") val properties: TrackProperties? = null,
    @JsonProperty("not_applicable", required = false) // work-around to ignore value param
    // jackson serialisation
    _messageId: String? = null,
) : Message(
    EventType.TRACK,
    context,
    anonymousId,
    userId,
    timestamp,
    destinationProps,
    _messageId,
) {
    companion object {
        @JvmStatic
        fun create(
            eventName: String?,
            timestamp: String,
            properties: TrackProperties? = null,
            anonymousId: String? = null,
            userId: String? = null,
            destinationProps: MessageDestinationProps? = null,
            traits: Map<String, Any?>? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null,
            _messageId: String? = null,
        ) = TrackMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            eventName,
            properties,
            _messageId,
        )
    }

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,
        destinationProps: MessageDestinationProps? = this.destinationProps,
        eventName: String? = this.eventName,
        properties: TrackProperties? = this.properties,
    ) = TrackMessage(
        context,
        anonymousId,
        userId,
        timestamp,
        destinationProps,
        eventName,
        properties,
        _messageId = this.messageId,
    )

    override fun toString(): String {
        return "${super.toString()}, " + "eventName = $eventName, " + "properties = $properties"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is TrackMessage && other.eventName == eventName && other.properties == properties
    }

    override fun hashCode(): Int {
        var result = (eventName?.hashCode() ?: 0)
        result = 31 * result + (properties?.hashCode() ?: 0)
        return result
    }
}

class IdentifyMessage internal constructor(

    @JsonProperty("context") @Json(name = "context") context: MessageContext? = null,
    @JsonProperty("anonymousId") @Json(name = "anonymousId") anonymousId: String?,
    @JsonProperty("userId") @Json(name = "userId") userId: String? = null,
    @JsonProperty("originalTimestamp") @Json(name = "originalTimestamp") timestamp: String,

    @JsonProperty("destinationProps") @Json(name = "destinationProps") destinationProps: MessageDestinationProps? = null,
    /**
     * Get the properties back as set to the event Always convert objects to
     * it's json equivalent before setting it as values
     */

    @SerializedName("properties") @JsonProperty("properties") @Json(name = "properties") val properties: IdentifyProperties? = null,
    @JsonProperty("not_applicable", required = false) // work-around to ignore value param
    // jackson serialisation
    _messageId: String? = null,
) : Message(
    EventType.IDENTIFY,
    context,
    anonymousId,
    userId,
    timestamp,
    destinationProps,
    _messageId,
) {

    companion object {
        @JvmStatic
        fun create(
            anonymousId: String? = null,
            userId: String? = null,
            timestamp: String,
            properties: IdentifyProperties? = null,
            destinationProps: MessageDestinationProps? = null,
            traits: IdentifyTraits? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null,
            _messageId: String? = null,
        ) = IdentifyMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            properties,
            _messageId,
        )
    }

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,
        destinationProps: MessageDestinationProps? = this.destinationProps,
        properties: IdentifyProperties? = this.properties,

        ) = IdentifyMessage(
        context,
        anonymousId,
        userId,
        timestamp,
        destinationProps,
        properties,
        _messageId = messageId,
    )

    override fun toString(): String {
        return "${super.toString()}, " + "properties = $properties"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is IdentifyMessage && other.properties == properties
    }

    override fun hashCode(): Int {
        return properties?.hashCode() ?: 0
    }
}
// verbose methods
// verbose methods

fun TrackProperties(vararg keyPropertyPair: Pair<String, Any>): TrackProperties =
    mapOf(*keyPropertyPair)

// fun PageProperties(vararg keyPropertyPair: Pair<String, Any>) : PageProperties = mapOf(*keyPropertyPair)

fun ScreenProperties(vararg keyPropertyPair: Pair<String, Any>): ScreenProperties =
    mapOf(*keyPropertyPair)

fun IdentifyProperties(vararg keyPropertyPair: Pair<String, Any>): IdentifyProperties =
    mapOf(*keyPropertyPair)

fun MessageIntegrations(vararg keyPropertyPair: Pair<String, Boolean>): MessageIntegrations =
    mapOf(*keyPropertyPair)

fun MessageDestinationProps(vararg keyPropertyPair: Pair<String, Map<*, *>>): MessageDestinationProps =
    mapOf(*keyPropertyPair)

fun IdentifyTraits(vararg keyPropertyPair: Pair<String, Any?>): IdentifyTraits =
    mapOf(*keyPropertyPair)

fun GroupTraits(vararg keyPropertyPair: Pair<String, Any>): GroupTraits = mapOf(*keyPropertyPair)
