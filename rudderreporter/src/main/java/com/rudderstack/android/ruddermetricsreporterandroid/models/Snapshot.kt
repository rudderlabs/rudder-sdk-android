/*
 * Creator: Debanjan Chatterjee on 06/11/23, 9:27 am Last modified: 02/11/23, 8:02 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

package com.rudderstack.android.ruddermetricsreporterandroid.models

import android.content.ContentValues
import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField

data class Snapshot(val id: String, val snapshot: String)

@RudderEntity(
    tableName = SnapshotEntity.TABLE_NAME, [
        RudderField(
            RudderField.Type.TEXT, SnapshotEntity.ColumnNames.ID,
            primaryKey = true, isNullable = false
        ),
        RudderField(
            RudderField.Type.TEXT, SnapshotEntity.ColumnNames.SNAPSHOT,
            primaryKey = false, isNullable = false
        )
    ]
)
internal class SnapshotEntity(private val id: String, private val snapshot: String): Entity {
    constructor(snapshot: Snapshot): this(snapshot.id, snapshot.snapshot)
    object ColumnNames {
        const val ID = "id"
        const val SNAPSHOT = "snapshot"
    }
    companion object {
        const val TABLE_NAME = "snapshots"
        fun create(values: Map<String, Any?>): Snapshot{
            val id = values[ColumnNames.ID] as String
            val snapshot = values[ColumnNames.SNAPSHOT] as String
            return Snapshot(id, snapshot)
        }
    }

    override fun generateContentValues(): ContentValues {
        return ContentValues().apply {
            put(ColumnNames.ID, id)
            put(ColumnNames.SNAPSHOT, snapshot)
        }
    }
    fun toSnapshot(): Snapshot {
        return Snapshot(id, snapshot)
    }
    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(id)
    }

    override fun equals(other: Any?): Boolean {
        return (other is SnapshotEntity) &&
            id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}