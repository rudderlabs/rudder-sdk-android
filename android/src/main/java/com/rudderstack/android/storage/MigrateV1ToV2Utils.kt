/*
 * Creator: Debanjan Chatterjee on 23/02/24, 12:19 pm Last modified: 23/02/24, 12:19 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.rudderstack.android.internal.STATUS_CLOUD_MODE_DONE
import com.rudderstack.android.internal.STATUS_DEVICE_MODE_DONE
import com.rudderstack.android.internal.STATUS_NEW
import com.rudderstack.android.internal.isCloudModeDone
import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.EntityFactory
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.core.RudderLogger
import com.rudderstack.models.AliasMessage
import com.rudderstack.models.GroupMessage
import com.rudderstack.models.IdentifyMessage
import com.rudderstack.models.Message
import com.rudderstack.models.MessageIntegrations
import com.rudderstack.models.ScreenMessage
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.util.concurrent.ExecutorService

private const val V1_DATABASE_NAME = "rl_persistence.db"
private val synchronizeOn = Any()

/**
 * Migrate the V1 events to V2 storage
 *
 * @param context Android context
 * @param v2Database The recent database
 * @param jsonAdapter Json adapter to parse events or messages
 * @param executorService Passed to V1 database to perform operations, will be shutdown post
 * migration
 */
fun migrateV1MessagesToV2Database(
    context: Context,
    v2Database: RudderDatabase,
    jsonAdapter: JsonAdapter,
    rudderLogger: RudderLogger? = null,
    executorService: ExecutorService? = null
) : Boolean{
    rudderLogger?.info(log = "Migrating V1 messages to V2 database")
    synchronized(synchronizeOn) {
        val prevVersion = findPreviousVersion(context).takeIf { it > 0 }?:return false
        rudderLogger?.debug(log = "Migrating from version: $prevVersion")
        val v1Database = RudderDatabase(
            context,
            V1_DATABASE_NAME,
            V1EntityFactory(jsonAdapter),
            false,
            prevVersion,
            providedExecutorService = executorService
        )
        val v1MessagesDao = v1Database.getDao<MessageEntity>(MessageEntity::class.java)
        val v1Messages = v1MessagesDao.getAllSync()?.filterNot {
            it.status.isCloudModeDone()
        }?.takeIf {
                it.isNotEmpty()
            } ?: run {
            v1Database.delete()
            v1Database.shutDown()
            return true
        }
        with(v2Database.getDao(MessageEntity::class.java)) {
            rudderLogger?.info(log = "Migrating ${v1Messages.size} messages")
            v1Messages.insertSync()
        }
        v1Database.delete()
        v1Database.shutDown()
    }
    return true
}

private fun findPreviousVersion(context: Context): Int {
    return try {
        val db = SQLiteDatabase.openDatabase(
            context.getDatabasePath(V1_DATABASE_NAME).absolutePath, null,
            SQLiteDatabase.OPEN_READONLY
        )
        val prevVersion = db.version
        db.close()
        prevVersion
    }catch (ex: SQLiteException){
        -1
    }
}

private const val V1_MESSAGE_ID_COL = "id"
private const val V1_STATUS_COL = "status"

//status values for database version 2 =. Check createSchema documentation for details.
internal const val V1_STATUS_CLOUD_MODE_DONE = 2
internal const val V1_STATUS_DEVICE_MODE_DONE = 1
internal const val V1_STATUS_ALL_DONE = 3
internal const val V1_STATUS_NEW = 0

// This column purpose is to identify if an event is dumped to device mode destinations without transformations or not.
private const val V1_DM_PROCESSED_COL = "dm_processed"

// status value for DM_PROCESSED column
private const val V1_DM_PROCESSED_PENDING = 0
private const val V1_DM_PROCESSED_DONE = 1
const val MESSAGE_COL = "message"
const val UPDATED_COL = "updated"

class V1EntityFactory(private val jsonAdapter: JsonAdapter) : EntityFactory {
    override fun <T : Entity> getEntity(entity: Class<T>, values: Map<String, Any?>): T? {
        return when (entity) {
            MessageEntity::class.java -> {
                val message = values[MESSAGE_COL] as String
                val updatedAt = values[UPDATED_COL] as Long

                val status = when (values[V1_STATUS_COL].toString().toIntOrNull()) {
                    V1_STATUS_CLOUD_MODE_DONE -> STATUS_CLOUD_MODE_DONE
                    V1_STATUS_DEVICE_MODE_DONE -> STATUS_DEVICE_MODE_DONE
                    V1_STATUS_ALL_DONE -> STATUS_CLOUD_MODE_DONE or STATUS_DEVICE_MODE_DONE
                    else -> STATUS_NEW
                }
                //TODO - DMT
                /*val dmProcessed = when (values[V1_DM_PROCESSED_COL] as Int) {
                    V1_DM_PROCESSED_PENDING -> false
                    V1_DM_PROCESSED_DONE -> true
                    else -> false
                }*/
                MessageEntity(
                    deserializeV1EntityToMessage(message) ?: return null, jsonAdapter, updatedAt
                ).also {
                    it.maskWithDmtStatus(status) } as T
            }

            else -> null
        }
    }

    private fun deserializeV1EntityToMessage(v1EventJson: String): Message? {
        val v1EventMap =
            jsonAdapter.readJson(v1EventJson, object : RudderTypeAdapter<Map<String, Any?>>() {})
                ?: return null


        val type = v1EventMap["type"] as? String ?: return null
        val channel = v1EventMap["channel"] as? String ?: "android"
        val integrations = v1EventMap["integrations"] as? MessageIntegrations
        return when (type) {
            V1MessageType.TRACK -> jsonAdapter.readJson(v1EventJson, TrackMessage::class.java)

            V1MessageType.SCREEN -> jsonAdapter.readJson(v1EventJson, ScreenMessage::class.java)

            V1MessageType.IDENTIFY -> jsonAdapter.readJson(v1EventJson, IdentifyMessage::class.java)

            V1MessageType.ALIAS -> jsonAdapter.readJson(v1EventJson, AliasMessage::class.java)

            V1MessageType.GROUP -> jsonAdapter.readJson(v1EventJson, GroupMessage::class.java)


            else -> null
        }?.also {
            it.channel = channel
            it.integrations = integrations
        }
    }

    object V1MessageType {
        internal const val TRACK = "track"
        internal const val SCREEN = "screen"
        internal const val IDENTIFY = "identify"
        internal const val ALIAS = "alias"
        internal const val GROUP = "group"
    }


}
