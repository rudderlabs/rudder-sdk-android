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
import com.rudderstack.core.RudderUtils
import com.rudderstack.models.AliasMessage
import com.rudderstack.models.GroupMessage
import com.rudderstack.models.IdentifyMessage
import com.rudderstack.models.Message
import com.rudderstack.models.PageMessage
import com.rudderstack.models.ScreenMessage
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter

/**
 * An [Entity] delegate for [Message] model.
 *
 */
@RudderEntity(
    TABLE_NAME, [RudderField(
        RudderField.Type.TEXT, MessageEntity.ColumnNames.messageId, primaryKey = true
    ), RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.message), RudderField(
        RudderField.Type.INTEGER, MessageEntity.ColumnNames.updatedAt, isIndex = true
    )]
)
internal class MessageEntity(val message: Message, private val jsonAdapter: JsonAdapter) : Entity {
    object ColumnNames {
        internal const val messageId = "messageId"
        internal const val message = "message"
        internal const val updatedAt = "updated"
        internal const val type = "type"
    }


    override fun generateContentValues(): ContentValues {
        return ContentValues().also {
            it.put(ColumnNames.messageId, message.messageId)
            it.put(
                ColumnNames.message,
                jsonAdapter.writeToJson(message, RudderTypeAdapter {})?.replace("'", BACKLASHES_INVERTED_COMMA)
            )
            it.put(ColumnNames.updatedAt, System.currentTimeMillis())
            it.put(ColumnNames.type, message.getType().value)
        }
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(message.messageId)
    }

    companion object {

        internal const val TABLE_NAME = "message"
        private const val BACKLASHES_INVERTED_COMMA = "\\\\'"
        internal fun create(
            values: Map<String, Any?>, jsonAdapter: JsonAdapter
        ): MessageEntity {
            val type = values[ColumnNames.type] as String
            val classOfMessage = getClassBasedOnType(type)
            val message = jsonAdapter.readJson(values["message"] as String, classOfMessage)
            return MessageEntity(
                message ?: TrackMessage.create("NA", RudderUtils.timeStamp),
                jsonAdapter
            )
        }

        private fun getClassBasedOnType(type: String): Class<out Message> {
            return when (type) {
                Message.EventType.ALIAS.value -> AliasMessage::class.java
                Message.EventType.GROUP.value -> GroupMessage::class.java
                Message.EventType.IDENTIFY.value -> IdentifyMessage::class.java
                Message.EventType.PAGE.value -> PageMessage::class.java
                Message.EventType.SCREEN.value -> ScreenMessage::class.java
                Message.EventType.TRACK.value -> TrackMessage::class.java
                else -> Message::class.java
            }
        }
    }
}