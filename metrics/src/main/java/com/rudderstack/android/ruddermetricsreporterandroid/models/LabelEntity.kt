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

@RudderEntity(LabelEntity.TABLE_NAME, [
    RudderField(RudderField.Type.INTEGER, LabelEntity.Columns.ID, primaryKey = true, isNullable = false,
    isAutoInc = true, isIndex = true),
    RudderField(RudderField.Type.TEXT, LabelEntity.Columns.NAME, ),
    RudderField(RudderField.Type.INTEGER, LabelEntity.Columns.VALUE)
])
class LabelEntity : Entity {
    object Columns{
        const val ID = "id"
        const val NAME = "name"
        const val VALUE = "value"
    }

    override fun generateContentValues(): ContentValues {
        TODO("Not yet implemented")
    }

    override fun getPrimaryKeyValues(): Array<String> {
        TODO("Not yet implemented")
    }

    companion object{
        const val TABLE_NAME = "label"
    }

}