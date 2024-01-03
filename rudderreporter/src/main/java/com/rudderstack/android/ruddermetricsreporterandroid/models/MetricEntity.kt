/*
 * Creator: Debanjan Chatterjee on 14/06/23, 5:04 pm Last modified: 14/06/23, 5:04 pm
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
import com.rudderstack.android.ruddermetricsreporterandroid.models.MetricEntity.Companion.TABLE_NAME

/**
 * An entity class to store the metric data.
 * Both Gauge and Counter metrics have number as values
 *
 */
@RudderEntity(
    tableName = TABLE_NAME,
    [
        RudderField(
            RudderField.Type.INTEGER,
            MetricEntity.ColumnNames.ID,
            primaryKey = false,
            isNullable = false,
            isAutoInc = true,
            isIndex = true,
        ),
        RudderField(
            RudderField.Type.TEXT,
            MetricEntity.ColumnNames.NAME,
            primaryKey = true,
            isNullable = false,
        ),
        RudderField(
            RudderField.Type.INTEGER,
            MetricEntity.ColumnNames.VALUE,
            primaryKey = false,
            isNullable = false,
        ),
        RudderField(
            RudderField.Type.TEXT,
            MetricEntity.ColumnNames.TYPE,
            primaryKey = true,
            isNullable = false,
        ),
        RudderField(
            RudderField.Type.TEXT,
            MetricEntity.ColumnNames.LABEL,
            primaryKey = true,
            isNullable = false,
            isIndex = true,
        ),
    ],
)
internal class MetricEntity(
    val name: String,
    val value: Long,
    val type: String,
    val label: String,
) : Entity {
    object ColumnNames {
        const val ID = "id"
        const val NAME = "name"
        const val VALUE = "value"
        const val TYPE = "type"
        const val LABEL = "label"
    }

    private var _id: Long = UNINITIALIZED_ID
    val id: Long
        get() = _id

    override fun generateContentValues(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(ColumnNames.NAME, name)
        contentValues.put(ColumnNames.VALUE, value)
        contentValues.put(ColumnNames.TYPE, type)
        contentValues.put(ColumnNames.LABEL, label)
        return contentValues
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(name, type, label)
    }

    companion object {
        const val TABLE_NAME = "metrics"

        // This object is not generated from database.
        const val UNINITIALIZED_ID = -1L

        fun create(values: Map<String, Any?>): MetricEntity {
            val id = values[ColumnNames.ID] as Long
            val name = values[ColumnNames.NAME] as String
            val value = values[ColumnNames.VALUE] as Long
            val type = values[ColumnNames.TYPE] as String
            val label = values[ColumnNames.LABEL] as String
            return MetricEntity(name, value, type, label).also {
                it._id = id
            }
        }
    }
}
