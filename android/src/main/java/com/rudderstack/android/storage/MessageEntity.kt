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
import com.rudderstack.android.internal.STATUS_NEW
import com.rudderstack.android.internal.maskWith
import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField
import com.rudderstack.android.storage.MessageEntity.Companion.TABLE_NAME
import com.rudderstack.models.AliasMessage
import com.rudderstack.models.GroupMessage
import com.rudderstack.models.IdentifyMessage
import com.rudderstack.models.Message
import com.rudderstack.models.ScreenMessage
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter

/**
 * An [Entity] delegate for [Message] model.
 * @constructor creates a [MessageEntity] from a [Message] and [JsonAdapter]
 * If this is a legacy message, pass the updatedAt as well, otherwise will be auto generated
 */
@RudderEntity(
    TABLE_NAME, [
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.messageId, primaryKey = true),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.message),
        RudderField(RudderField.Type.INTEGER, MessageEntity.ColumnNames.updatedAt, isIndex = true),
        RudderField(RudderField.Type.TEXT, MessageEntity.ColumnNames.type),
        RudderField(RudderField.Type.INTEGER, MessageEntity.ColumnNames.status),
    ]
)
internal class MessageEntity(
    val message: Message,
    private val jsonAdapter: JsonAdapter,
    private val updatedAt: Long? = null
) : Entity {
    object ColumnNames {
        internal const val messageId = "id"
        internal const val message = "message"
        internal const val updatedAt = "updated"
        internal const val type = "type"
        internal const val status = "status"
    }

    val status: Int
        get() = _status
    private var _status: Int = STATUS_NEW

    fun maskWithDmtStatus(dmtStatus: Int) {
        _status = _status.maskWith(dmtStatus)
    }

    override fun generateContentValues(): ContentValues {
        return ContentValues().also {
            it.put(ColumnNames.messageId, message.messageId)
            it.put(
                ColumnNames.message,
                jsonAdapter.writeToJson(message, RudderTypeAdapter<Message> {})
                    ?.replace("'", BACKLASHES_INVERTED_COMMA)
            )
            it.put(ColumnNames.updatedAt, updatedAt ?: System.currentTimeMillis())
            it.put(ColumnNames.type, message.getType().value)
            it.put(ColumnNames.status, _status)
        }
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(message.messageId)
    }

    companion object {

        internal const val TABLE_NAME = "events"
        private const val BACKLASHES_INVERTED_COMMA = "\\\\'"
        internal fun create(
            values: Map<String, Any?>, jsonAdapter: JsonAdapter
        ): MessageEntity? {
            val type = values[ColumnNames.type] as String
            val classOfMessage = getClassBasedOnType(type)
            val message = jsonAdapter.readJson(values[ColumnNames.message] as String, classOfMessage)
            val status = values[ColumnNames.status] as? Int
            return MessageEntity(
                message ?: return null,
                jsonAdapter
            ).also { entity ->
                status?.let {
                    entity.maskWithDmtStatus(it)
                }
            }
        }

        private fun getClassBasedOnType(type: String): Class<out Message> {
            return when (type) {
                Message.EventType.ALIAS.value -> AliasMessage::class.java
                Message.EventType.GROUP.value -> GroupMessage::class.java
                Message.EventType.IDENTIFY.value -> IdentifyMessage::class.java
                Message.EventType.SCREEN.value -> ScreenMessage::class.java
                Message.EventType.TRACK.value -> TrackMessage::class.java
                else -> Message::class.java
            }
        }
    }
}
