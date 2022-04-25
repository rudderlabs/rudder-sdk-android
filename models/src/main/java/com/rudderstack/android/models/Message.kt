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

package com.rudderstack.android.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.rudderstack.android.models.Message.EventType
import com.squareup.moshi.Json
import java.util.*

typealias MessageContext = Map<String, Any?>
typealias PageProperties = Map<String, String>
typealias ScreenProperties = Map<String, String>
typealias TrackProperties = Map<String, String>
typealias IdentifyTraits = Map<String, Any?>
typealias IdentifyProperties = Map<String, String>
//integrations might change from String,Boolean to String, Object at a later point of time
typealias MessageIntegrations = Map<String, Boolean>
typealias MessageDestinationProps = Map<String, Map<*, *>>
typealias GroupTraits = Map<String, String>

@JsonIgnoreProperties("type\$models")
sealed class Message(

    /**
     * @return Type of event
     * @see EventType
     */
    @SerializedName("type")
    //@Expose
    @param:JsonProperty("type")
    @field:JsonProperty("type")
    @get:JsonProperty("type")
    @Json(name = "type")

    internal var type: EventType,


    @SerializedName("context")
    @JsonProperty("context")
    @Json(name = "context")
    //@Expose
    //convert to json to put any object as value
    val context: MessageContext? = null,

    @SerializedName("anonymousId")
    @JsonProperty("anonymousId")
    @Json(name = "anonymousId")
    //@Expose
    var anonymousId: String?,

    /**
     * @return User ID for the event
     */
    @SerializedName("userId")
    @JsonProperty("userId")
    @Json(name = "userId")
    //@Expose
    var userId: String? = null,

    @SerializedName("timestamp")
    @JsonProperty("timestamp")
    @Json(name = "timestamp")
    //format - yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    //@Expose
    val timestamp: String,

    @SerializedName("destinationProps")
    @JsonProperty("destinationProps")
    @Json(name = "destinationProps")
    //@Expose
    protected var destinationProps: MessageDestinationProps? = null,


//    @Transient
    _channel: String? = null


) {
    @SerializedName("messageId")
    @JsonProperty("messageId")
    @Json(name = "messageId")
    //@Expose
    val messageId: String = String.format(
        Locale.US,
        "%d-%s",
        System.currentTimeMillis(),
        UUID.randomUUID().toString()
    )

    //ugly hack for moshi
    //https://github.com/square/moshi/issues/609#issuecomment-798805367
    @Json(name = "channel")
    @JsonProperty("channel")
    @SerializedName("channel")
    var channel: String = _channel ?: "server"
        get() = field ?: "server"

    @JsonIgnore
    fun getType() = type

    /**
     * For internal use. Any value set over here will be overwritten internally.
     * For setting custom configuration for integrations for a particular message,
     * use RudderOptions
     */
    @SerializedName("integrations")
    @JsonProperty("integrations")
    @Json(name = "integrations")
    //@Expose
    var integrations: MessageIntegrations? = null
    /*@Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class ChannelMitigationMoshi

    class ChannelMoshiAdapter {
        @FromJson @ChannelMitigationMoshi fun fromJson(channel: String?):  String {
            return channel?:"server"
        }

        @ToJson fun toJson(@ChannelMitigationMoshi channel: String) : String? {
            return channel
        }
    }*/
    /*@Transient
    var rudderOption: RudderOption? = null
        set(rudderOption) {
            field = rudderOption
            if (rudderOption != null) {
                setIntegrations(rudderOption.getIntegrations())
                setCustomContexts(rudderOption.getCustomContexts())
            }
        }
*/
    /*fun setPreviousId(previousId: String?) {
        this.previousId = previousId
    }*/

    /*      fun setGroupTraits(groupTraits: RudderTraits?) {
              traits = groupTraits
          }

          fun setProperty(property: RudderProperty?) {
              if (property != null) properties = property.getMap()
          }

          fun setUserProperty(userProperty: RudderUserProperty) {
              userProperties = userProperty.getMap()
          }

          fun updateTraits(traits: RudderTraits?) {
              RudderElementCache.updateTraits(traits)
              updateContext()
          }

          fun updateTraits(traits: Map<String?, Any?>?) {
              RudderElementCache.updateTraits(traits)
              updateContext()
          }

          fun updateExternalIds(option: RudderOption?) {
              if (option != null) {
                  val externalIds: List<Map<String, Any>> = option.getExternalIds()
                  if (externalIds != null && !externalIds.isEmpty()) {
                      RudderElementCache.updateExternalIds(externalIds)
                      updateContext()
                  }
              }
          }
  */
    /*fun addIntegrationProps(integrationKey: String, isEnabled: Boolean, props: Map<*, *>) {
        integrations[integrationKey] = isEnabled
        if (isEnabled) {
            if (destinationProps == null) destinationProps = HashMap()
            destinationProps!![integrationKey] = props
        }
    }

    fun setIntegrations(integrations: Map<String, Any>?) {
        if (integrations == null) return
        for (key in integrations.keys) {
            this.integrations[key] = integrations[key] as Any
        }
    }*/

/*
        fun setCustomContexts(customContexts: Map<String, Any>?) {
            if (customContexts == null) return
            this.customContexts = customContexts
            context.setCustomContexts(customContexts)
        }

        fun getTraits(): Map<String, Any> {
            return context.getTraits()
        }
*/

    /**
     * @return Returns message level context
     *//*
        fun getContext(): RudderContext {
            return context
        }

        fun updateContext() {
            context = RudderElementCache.getCachedContext()
            if (customContexts != null) context.setCustomContexts(customContexts)
        }

        */
    /**
     * @return Integrations Map passed for the event
     *//*
        fun getIntegrations(): Map<String, Any?> {
            return integrations
        }

        init {
            context = RudderElementCache.getCachedContext()
            anonymousId = RudderContext.getAnonymousId()
            val traits: Map<String, Any> = context.getTraits()
            if (traits != null && traits.containsKey("id")) {
                userId = traits["id"].toString()
            }
        }*/

    open fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId
    ): Message = when (this) {
        is AliasMessage -> copy(
//            messageId,
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
//            integrations,
            previousId
        )
        is GroupMessage -> copy(
//            messageId,
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
//            integrations,
            groupId,
            traits
        )
        is IdentifyMessage -> copy(
//            messageId,
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
//            integrations,
            properties
        )
        is PageMessage -> copy(
//            messageId,
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
//            integrations,
            name,
            properties,
            category
        )
        is ScreenMessage -> copy(
//            messageId,
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
//            integrations,
            name,
            category,
            properties
        )
        is TrackMessage -> copy(
//            messageId,
            context,
            anonymousId,
            userId,
            timestamp,
            destinationProps,
//            integrations,
            eventName,
            properties
        )
    }.also {
        it.integrations = integrations
    }

    enum class EventType(val value: String) {
        @SerializedName("Alias")
        @JsonProperty("Alias")
        @Json(name = "Alias")
        ALIAS("Alias"),

        @SerializedName("Group")
        @JsonProperty("Group")
        @Json(name = "Group")
        GROUP("Group"),

        @SerializedName("Page")
        @JsonProperty("Page")
        @Json(name = "Page")
        PAGE("Page"),

        @SerializedName("Screen")
        @JsonProperty("Screen")
        @Json(name = "Screen")
        SCREEN("Screen"),

        @SerializedName("Track")
        @JsonProperty("Track")
        @Json(name = "Track")
        TRACK("Track"),

        @SerializedName("Identify")
        @JsonProperty("Identify")
        @Json(name = "Identify")
        IDENTIFY("Identify")
    }

    override fun toString(): String {
        return "type = $type, " +
                "messageId = $messageId, " +
                "context = $context, " +
                "anonymousId = $anonymousId, " +
                "userId = $userId, " +
                "timestamp = $timestamp, " +
                "destinationProps = $destinationProps, " +
                "integrations = $integrations, " +
                "channel = $channel"
    }

    override fun equals(other: Any?): Boolean {
        return other is Message && other.type === this.type &&
                other.messageId == this.messageId &&
                other.context == this.context &&
                other.anonymousId == this.anonymousId &&
                other.userId == this.userId &&
                other.timestamp == this.timestamp &&
                other.destinationProps == this.destinationProps &&
                other.integrations == this.integrations &&
                other.channel == this.channel
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

    @JsonProperty("context")
    @Json(name = "context")
    context: MessageContext? = null,
    @JsonProperty("anonymousId")
    @Json(name = "anonymousId")
    anonymousId: String?,
    @JsonProperty("userId")
    @Json(name = "userId")
    userId: String? = null,
    @JsonProperty("timestamp")
    @Json(name = "timestamp")
    timestamp: String,

    @JsonProperty("destinationProps")
    @Json(name = "destinationProps")
    destinationProps: MessageDestinationProps? = null,
//    @JsonProperty("integrations")
//    @Json(name = "integrations")
//    integrations: MessageIntegrations = HashMap(),
    @SerializedName("previousId")
    @JsonProperty("previousId")
    @Json(name = "previousId")
    var previousId: String? = null,
//    _channel : String? = null
) : Message(
    EventType.ALIAS,
//    messageId,
    context,
    anonymousId,
    userId,
    timestamp,
    destinationProps,
//    integrations,
) {
    companion object {
        fun create(
            timestamp: String,

            anonymousId: String? = null,
            userId: String? = null,

            destinationProps: MessageDestinationProps? = null,
            previousId: String? = null,
            traits: Map<String, Any?>? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null,
        ) = AliasMessage(createContext(traits, externalIds, customContextMap),
            anonymousId, userId, timestamp, destinationProps, previousId)
    }

    /*override fun copy(
        context: MessageContext?,
        anonymousId: String?,
        userId: String?
    ): AliasMessage {
        return super.copy(context, anonymousId, userId) as AliasMessage
    }*/

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,

        destinationProps: MessageDestinationProps? = this.destinationProps,
        previousId: String? = this.previousId,
    ) = AliasMessage(context, anonymousId, userId, timestamp, destinationProps, previousId)

    override fun toString(): String {
        return "${super.toString()}, " +
                "previousId = $previousId"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) &&
                other is AliasMessage &&
                other.previousId == previousId
    }

    override fun hashCode(): Int {
        return super.hashCode() * (previousId?.hashCode() ?: 1)
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class GroupMessage internal constructor(

    @JsonProperty("context")
    @Json(name = "context")
    context: MessageContext? = null,
    @JsonProperty("anonymousId")
    @Json(name = "anonymousId")
    anonymousId: String?,
    @JsonProperty("userId")
    @Json(name = "userId")
    userId: String? = null,
    @JsonProperty("timestamp")
    @Json(name = "timestamp")
    timestamp: String,

    @JsonProperty("destinationProps")
    @Json(name = "destinationProps")
    destinationProps: MessageDestinationProps? = null,
//    @JsonProperty("integrations")
//    @Json(name = "integrations")
//    integrations: MessageIntegrations = HashMap(),
    /**
     * @return Group ID for the event
     */
    @SerializedName("groupId")
    @JsonProperty("groupId")
    @Json(name = "groupId")
    var groupId: String? = null,

    @SerializedName("traits")
    @JsonProperty("traits")
    @Json(name = "traits")
    val traits: GroupTraits? = null,


    ) : Message(
    EventType.GROUP,
//    messageId,
    context,
    anonymousId,
    userId,
    timestamp,
//    channel,
    destinationProps,
//    integrations
) {
    companion object{
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
        ) = GroupMessage(createContext(traits, externalIds, customContextMap),
            anonymousId, userId, timestamp, destinationProps, groupId, groupTraits)
    }
    /*override fun copy(
        context: MessageContext?,
        anonymousId: String?,
        userId: String?
    ): GroupMessage {
        return super.copy(context, anonymousId, userId) as GroupMessage
    }*/

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,

        destinationProps: MessageDestinationProps? = this.destinationProps,
        groupId: String? = this.groupId,
        traits: GroupTraits? = this.traits,
    ) = GroupMessage(context, anonymousId, userId, timestamp, destinationProps, groupId, traits)

    override fun toString(): String {
        return "${super.toString()}, " +
                "groupId = $groupId, " +
                "traits = $traits"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) &&
                other is GroupMessage &&
                other.groupId == groupId &&
                other.traits == traits
    }

    override fun hashCode(): Int {
        var result = groupId?.hashCode() ?: 0
        result = 31 * result + (traits?.hashCode() ?: 0)
        return result
    }

}


class PageMessage internal constructor(

    @JsonProperty("context")
    @Json(name = "context")
    context: MessageContext? = null,
    @JsonProperty("anonymousId")
    @Json(name = "anonymousId")
    anonymousId: String?,
    @JsonProperty("userId")
    @Json(name = "userId")
    userId: String? = null,
    @JsonProperty("timestamp")
    @Json(name = "timestamp")
    timestamp: String,

    @JsonProperty("destinationProps")
    @Json(name = "destinationProps")
    destinationProps: MessageDestinationProps? = null,
//    @JsonProperty("integrations")
//    @Json(name = "integrations")
//    integrations: MessageIntegrations = HashMap(),
    /**
     * @return Name of the event tracked
     */

    @SerializedName("event")
    @get:JsonProperty("event")
    @field:JsonProperty("event")
    @param:JsonProperty("event")
    @Json(name = "event")
    var name: String? = null,

    /**
     * Get the properties back as set to the event
     * Always convert objects to it's json equivalent before setting it as values
     * @return Map of String-Object
     */

    @SerializedName("properties")
    @JsonProperty("properties")
    @Json(name = "properties")
    val properties: PageProperties? = null,

    @SerializedName("category")
    @JsonProperty("category")
    @Json(name = "category")
    val category: String? = null,

    ) : Message(
    EventType.PAGE,
//    messageId,
    context,
    anonymousId,
    userId,
    timestamp,
//    channel,
    destinationProps,
//    integrations
) {
    companion object{
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
            customContextMap: Map<String, Any>? = null
            ) = PageMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId, userId, timestamp, destinationProps, name, properties,
            category)
    }
    /*override fun copy(
        context: MessageContext?,
        anonymousId: String?,
        userId: String?
    ): PageMessage {
        return super.copy(context, anonymousId, userId) as PageMessage
    }*/

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
        category
    )

    override fun toString(): String {
        return "${super.toString()}, " +
                "name = $name, " +
                "properties = $properties, " +
                "category = $category"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) &&
                other is PageMessage &&
                other.name == name &&
                other.properties == properties &&
                other.category == category
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (properties?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        return result
    }
}


class ScreenMessage internal constructor(

    @JsonProperty("context")
    @Json(name = "context")
    context: MessageContext? = null,
    @JsonProperty("anonymousId")
    @Json(name = "anonymousId")
    anonymousId: String?,
    @JsonProperty("userId")
    @Json(name = "userId")
    userId: String? = null,
    @JsonProperty("timestamp")
    @Json(name = "timestamp")
    timestamp: String,

    @JsonProperty("destinationProps")
    @Json(name = "destinationProps")
    destinationProps: MessageDestinationProps? = null,
//    @JsonProperty("integrations")
//    @Json(name = "integrations")
//    integrations: MessageIntegrations = HashMap(),

    @JsonProperty("category")
    @Json(name = "category")
    internal val category: String?,
    /**
     * @return Name of the event tracked
     */
    @SerializedName("event")
    @get:JsonProperty("event")
    @field:JsonProperty("event")
    @param:JsonProperty("event")
    @Json(name = "event")
    var name: String? = null,

    /**
     * Get the properties back as set to the event
     * Always convert objects to it's json equivalent before setting it as values
     * @return Map of String-Object
     */

    @SerializedName("properties")
    @JsonProperty("properties")
    @Json(name = "properties")
    val properties: ScreenProperties? = null,

    ) : Message(
    EventType.SCREEN,
//    messageId,
    context,
    anonymousId,
    userId,
    timestamp,
//    channel,
    destinationProps,
//    integrations
) {
    companion object{
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
                   customContextMap: Map<String, Any>? = null)
         = ScreenMessage(
        createContext(traits, externalIds, customContextMap),
            anonymousId, userId, timestamp, destinationProps, category, name,
        properties
        )
    }
    /*override fun copy(
        context: MessageContext?,
        anonymousId: String?,
        userId: String?
    ): ScreenMessage {
        return super.copy(context, anonymousId, userId) as ScreenMessage
    }*/

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,

        destinationProps: MessageDestinationProps? = this.destinationProps,
        name: String? = this.name,
        category: String? = this.category,
        properties: ScreenProperties? = this.properties,

        ) = ScreenMessage(
        context, anonymousId, userId, timestamp, destinationProps, category, name,
        properties
    )

    override fun toString(): String {
        return "${super.toString()}, " +
                "name = $name, " +
                "properties = $properties"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) &&
                other is ScreenMessage &&
                other.name == name &&
                other.properties == properties
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (properties?.hashCode() ?: 0)
        return result
    }
}

@JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true)
class TrackMessage internal constructor(

    @JsonProperty("context")
    @Json(name = "context")
    context: MessageContext? = null,
    @JsonProperty("anonymousId")
    @Json(name = "anonymousId")
    anonymousId: String?,
    @JsonProperty("userId")
    @Json(name = "userId")
    userId: String? = null,
    @JsonProperty("timestamp")
    @Json(name = "timestamp")
    timestamp: String,
    /*channel: String? = null,*/
    @JsonProperty("destinationProps")
    @Json(name = "destinationProps")
    destinationProps: MessageDestinationProps? = null,
//    @JsonProperty("integrations")
//    @Json(name = "integrations")
//    integrations: MessageIntegrations = HashMap(),
    /**
     * @return Name of the event tracked
     */

    @SerializedName("event")
    @field:JsonProperty("event")
    @param:JsonProperty("event")
    @get:JsonProperty("event")
    @Json(name = "event")
    val eventName: String? = null,
    /**
     * Get the properties back as set to the event
     * Always convert objects to it's json equivalent before setting it as values
     * @return Map of String-Object
     */

    @SerializedName("properties")
    @JsonProperty("properties")
    @Json(name = "properties")
    val properties: TrackProperties? = null,
) : Message(
    EventType.TRACK,
//    messageId,
    context,
    anonymousId,
    userId,
    timestamp,
//    channel,
    destinationProps,
//    integrations
) {
    companion object{
        fun create(
            eventName: String?,
            timestamp: String,
            properties: TrackProperties? = null,
            anonymousId: String? = null,
            userId: String? = null,
            destinationProps: MessageDestinationProps? = null,
            traits: Map<String, Any?>? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null
        ) = TrackMessage(
            createContext(traits, externalIds, customContextMap),
            anonymousId,
            userId,
            timestamp,
            destinationProps,
            eventName,
            properties
        )
    }
    /*override fun copy(
        context: MessageContext?,
        anonymousId: String?,
        userId: String?
    ): TrackMessage {
        return super.copy(context, anonymousId, userId) as TrackMessage
    }*/

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
        properties
    )

    override fun toString(): String {
        return "${super.toString()}, " +
                "eventName = $eventName, " +
                "properties = $properties"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) &&
                other is TrackMessage &&
                other.eventName == eventName &&
                other.properties == properties
    }

    override fun hashCode(): Int {
        var result = (eventName?.hashCode() ?: 0)
        result = 31 * result + (properties?.hashCode() ?: 0)
        return result
    }


}


class IdentifyMessage internal constructor(

    @JsonProperty("context")
    @Json(name = "context")
    context: MessageContext? = null,
    @JsonProperty("anonymousId")
    @Json(name = "anonymousId")
    anonymousId: String?,
    @JsonProperty("userId")
    @Json(name = "userId")
    userId: String? = null,
    @JsonProperty("timestamp")
    @Json(name = "timestamp")
    timestamp: String,

    @JsonProperty("destinationProps")
    @Json(name = "destinationProps")
    destinationProps: MessageDestinationProps? = null,
//    @JsonProperty("integrations")
//    @Json(name = "integrations")
//    integrations: MessageIntegrations = HashMap(),
    /**
     * Get the properties back as set to the event
     * Always convert objects to it's json equivalent before setting it as values
     */

    @SerializedName("properties")
    @JsonProperty("properties")
    @Json(name = "properties")
    val properties: IdentifyProperties? = null,

    ) : Message(
    EventType.IDENTIFY,
//    messageId,
    context,
    anonymousId,
    userId,
    timestamp,
//    channel,
    destinationProps,
//    integrations
) {

    companion object{
        fun create(
            anonymousId: String? = null,
            userId: String? = null,
            timestamp: String,
            properties: IdentifyProperties? = null,
            destinationProps: MessageDestinationProps? = null,
            traits: IdentifyTraits? = null,
            externalIds: List<Map<String, String>>? = null,
            customContextMap: Map<String, Any>? = null
            ) = IdentifyMessage(createContext(traits, externalIds, customContextMap),
            anonymousId, userId, timestamp, destinationProps, properties)

    }
    /*override fun copy(
        context: MessageContext?,
        anonymousId: String?,
        userId: String?
    ): IdentifyMessage {
        return super.copy(context, anonymousId, userId) as IdentifyMessage
    }*/

    fun copy(
        context: MessageContext? = this.context,
        anonymousId: String? = this.anonymousId,
        userId: String? = this.userId,
        timestamp: String = this.timestamp,
        destinationProps: MessageDestinationProps? = this.destinationProps,
        properties: IdentifyProperties? = this.properties,

        ) = IdentifyMessage(context, anonymousId, userId, timestamp, destinationProps, properties)

    override fun toString(): String {
        return "${super.toString()}, " +
                "properties = $properties"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) &&
                other is IdentifyMessage &&
                other.properties == properties
    }

    override fun hashCode(): Int {
        return properties?.hashCode() ?: 0
    }
}




