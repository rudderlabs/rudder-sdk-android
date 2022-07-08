/*
 * Creator: Debanjan Chatterjee on 28/04/22, 12:26 AM Last modified: 28/04/22, 12:26 AM
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

package com.rudderstack.android.storage

import android.content.ContentValues
import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField
import com.rudderstack.android.storage.MessageEntity.Companion.TABLE_NAME
import com.rudderstack.models.*
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter

/**
 * An [Entity] delegate for [Message] model.
 *
 */
@RudderEntity(
    TABLE_NAME,
    [
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.messageId, primaryKey = true),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.anonymousId),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.userId),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.timestamp),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.channel),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.type),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.destinationProps),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.integrations),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.previousId),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.context),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.eventName),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.groupId),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.properties),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.traits),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.event),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.properties),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.category),
    ]
)
internal class MessageEntity(val message: Message, private val jsonAdapter: JsonAdapter) :
    Entity {
    object ColumnNames{
        internal const val messageId = "messageId"
        internal const val anonymousId = "anonymousId"
        internal const val userId = "userId"
        internal const val timestamp = "timestamp"
        internal const val channel = "channel"
        internal const val type = "type"
        internal const val destinationProps = "destinationProps"
        internal const val integrations = "integrations"
        internal const val previousId = "previousId"
        internal const val context = "context"
        internal const val eventName = "eventName"
        internal const val groupId = "groupId"
        internal const val traits = "traits"
        internal const val event = "event"
        internal const val properties = "properties"
        internal const val category = "category"
    }


    override fun generateContentValues(): ContentValues {
        return ContentValues().also {
            it.put("messageId", message.messageId)
            it.put("anonymousId", message.anonymousId)
            it.put("userId", message.userId)
            it.put("timestamp", message.timestamp)
            it.put("channel", message.channel)
            it.put("type", message.getType().value)

            it.put("context", message.context?.let {
                jsonAdapter.writeToJson(this, RudderTypeAdapter {})
            })
            it.put("destinationProps", message.destinationProps?.let {
                jsonAdapter.writeToJson(this, RudderTypeAdapter {})
            })
            it.put("integrations", message.integrations?.let {
                jsonAdapter.writeToJson(this, RudderTypeAdapter {})
            })

            when (message) {
                is AliasMessage -> {
                    it.put("previousId", message.previousId)
                }
                is GroupMessage -> {
                    it.put("groupId", message.groupId)
                    it.put("traits", message.traits?.let {
                        jsonAdapter.writeToJson(this, RudderTypeAdapter {})
                    })

                }
                is PageMessage -> {
                    it.put("eventName", message.name)
                    it.put("properties", message.properties?.let {
                        jsonAdapter.writeToJson(this, RudderTypeAdapter {})
                    })
                    it.put("category", message.category)
                }
                is ScreenMessage -> {
                    it.put("eventName", message.name)
                    it.put("properties", message.properties?.let {
                        jsonAdapter.writeToJson(this, RudderTypeAdapter {})
                    })
                    it.put("category", message.category)
                }
                is TrackMessage -> {
                    it.put("eventName", message.eventName)
                    it.put("properties", message.properties?.let {
                        jsonAdapter.writeToJson(this, RudderTypeAdapter {})
                    })
                }
                is IdentifyMessage -> {
                    it.put("properties", message.properties?.let {
                        jsonAdapter.writeToJson(this, RudderTypeAdapter {})
                    })
                }
            }

        }
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(message.messageId)
    }

    companion object {

        internal const val TABLE_NAME = "message"

        internal fun create(
            values: Map<String, Any?>,
            jsonAdapter: JsonAdapter
        ): MessageEntity {
            val type = Message.EventType.fromValue(
                (values["type"] as? String) ?: throw IllegalArgumentException("type is null")
            )
            val timeStamp = values["timestamp"] as String
            val anonId = values["anonymousId"] as? String?
            val userID = values["userId"] as? String?
            val eventName = values["eventName"] as? String?
            val category = values["category"] as? String?
            val destinationProps: MessageDestinationProps? =
                (values["destinationProps"] as? String?)?.let {
                    jsonAdapter.readJson(it, RudderTypeAdapter {})
                }

            val context: MessageContext? = (values["context"] as? String?)?.let {
                jsonAdapter.readJson(it, RudderTypeAdapter {})
            }

            val message = when (type) {

                Message.EventType.ALIAS -> AliasMessage.create(
                    timeStamp,
                    anonId,
                    userID,
                    destinationProps,
                    values["previousId"] as? String?,
                    context?.traits,
                    context?.externalIds,
                    context?.customContexts,
                )

                Message.EventType.GROUP -> GroupMessage.create(
                    anonId, userID, timeStamp, destinationProps,
                    values["groupId"] as? String?,
                    (values["traits"] as? String?)?.let {
                        jsonAdapter.readJson(it, RudderTypeAdapter {})
                    },
                    context?.traits, context?.externalIds, context?.customContexts
                )

                Message.EventType.PAGE -> PageMessage.create(
                    anonId,
                    userID,
                    timeStamp,
                    destinationProps,
                    eventName,
                    (values["properties"] as? String?)?.let {
                        jsonAdapter.readJson(it, RudderTypeAdapter {})
                    },
                    category,
                    context?.traits,
                    context?.externalIds,
                    context?.customContexts
                )

                Message.EventType.SCREEN -> ScreenMessage.create(
                    timeStamp,
                    anonId,
                    userID,
                    destinationProps,
                    eventName,
                    category,
                    (values["properties"] as? String?)?.let {
                        jsonAdapter.readJson(it, RudderTypeAdapter {})
                    },
                    context?.traits,
                    context?.externalIds,
                    context?.customContexts
                )

                Message.EventType.TRACK -> TrackMessage.create(
                    eventName,
                    timeStamp,
                    (values["properties"] as? String?)?.let {
                        jsonAdapter.readJson(it, RudderTypeAdapter {})
                    },
                    anonId,
                    userID,
                    destinationProps,
                    context?.traits,
                    context?.externalIds,
                    context?.customContexts
                )

                Message.EventType.IDENTIFY -> IdentifyMessage.create(
                    anonId,
                    userID,
                    timeStamp,
                    (values["properties"] as? String?)?.let {
                        jsonAdapter.readJson(it, RudderTypeAdapter {})
                    },
                    destinationProps,
                    context?.traits,
                    context?.externalIds,
                    context?.customContexts
                )
            }
            return MessageEntity(message, jsonAdapter)
        }
    }
}