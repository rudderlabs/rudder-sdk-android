/*
 * Creator: Debanjan Chatterjee on 24/08/23, 11:41 am Last modified: 24/08/23, 11:41 am
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
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorEvent

@RudderEntity(
    tableName = ErrorEntity.TABLE_NAME, [
        RudderField(
            RudderField.Type.INTEGER, ErrorEntity.ColumnNames.ID,
            primaryKey = true, isNullable = false, isAutoInc = true, isIndex = true
        ),
        RudderField(
            RudderField.Type.TEXT, ErrorEntity.ColumnNames.ERROR_EVENT,
            primaryKey = false, isNullable = false
        )
    ]
)
class ErrorEntity(val errorEvent: String): Entity {

    private var _id: Long = UNINITIALIZED_ID
    val id: Long
        get() = _id

    object ColumnNames {
        const val ID = "id"
        const val ERROR_EVENT = "error_event"
    }
    companion object {
        const val TABLE_NAME = "metrics"

        // This object is not generated from database.
        const val UNINITIALIZED_ID = -1L
        fun create(values: Map<String, Any?>): ErrorEntity {
            val errorEvent = values[ColumnNames.ERROR_EVENT] as String
            val id = values[ColumnNames.ID] as? Long
            return ErrorEntity(errorEvent).also {
                if(id != null) {
                    it._id = id
                }
            }
        }
    }

    override fun generateContentValues(): ContentValues {
        val values = ContentValues()
        values.put(ColumnNames.ERROR_EVENT, errorEvent)
        return values
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(id.toString())
    }


}