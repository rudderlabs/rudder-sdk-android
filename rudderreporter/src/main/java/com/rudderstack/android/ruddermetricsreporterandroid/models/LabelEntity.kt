/*
 * Creator: Debanjan Chatterjee on 14/06/23, 11:30 pm Last modified: 14/06/23, 11:30 pm
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

@RudderEntity(
    LabelEntity.TABLE_NAME,
    [
        RudderField(
            RudderField.Type.INTEGER,
            LabelEntity.Columns.ID,
            primaryKey = false,
            isNullable = false,
            isAutoInc = true,
            isIndex = true,
        ),
        RudderField(
            RudderField.Type.TEXT,
            LabelEntity.Columns.NAME,
            primaryKey = true,
            isNullable = false,
            isUnique = true,
        ),
        RudderField(
            RudderField.Type.TEXT,
            LabelEntity.Columns.VALUE,
            primaryKey = true,
            isNullable = false,
            isUnique = true,
        ),
    ],
)
internal class LabelEntity(val name: String, val value: String) : Entity {
    private var _id: Long = UNINITIALIZED_ID
    val id: Long
        get() = _id

    object Columns {
        const val ID = "label_id"
        const val NAME = "name"
        const val VALUE = "value"
    }

    override fun generateContentValues(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(Columns.NAME, name)
        contentValues.put(Columns.VALUE, value)
        return contentValues
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(name, value)
    }

    companion object {
        const val TABLE_NAME = "label"
        const val UNINITIALIZED_ID = -1L

        fun create(values: Map<String, Any?>): LabelEntity {
            val id = values[Columns.ID] as Long
            val name = values[Columns.NAME] as String
            val value = values[Columns.VALUE] as String
            return LabelEntity(name, value).also {
                it._id = id
            }
        }
    }
}
