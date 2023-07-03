/*
 * Creator: Debanjan Chatterjee on 07/10/21, 11:41 PM Last modified: 07/10/21, 11:41 PM
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

package com.rudderstack.android.repository.models

import android.content.ContentValues
import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField

@RudderEntity(
    "sautogen", fields = [
        RudderField(
            RudderField.Type.INTEGER,
            "id",
            true,
            isNullable = false,
            isAutoInc = true,
            isIndex = true
        ),
        RudderField(
            RudderField.Type.TEXT,
            "name",
            false,
            isNullable = false,
            isAutoInc = false,
            isIndex = false
        )
    ]
)
data class SampleAutoGenEntity(val name: String) : Entity {
    var id = 0
    override fun generateContentValues(): ContentValues {
        return ContentValues(1).also {
            it.put("name", name)
        }
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(id.toString())
    }
}
