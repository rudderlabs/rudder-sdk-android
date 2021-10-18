/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
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

@RudderEntity("sample", [RudderField(RudderField.Type.TEXT, "name", true),
    RudderField(RudderField.Type.INTEGER, "count", false),
    RudderField(RudderField.Type.TEXT, "items", false)])
data class SampleEntity(
    val name: String,
    val count: Int, val items: List<String>
) : Entity {
    override fun generateContentValues(): ContentValues {
        return ContentValues(3).also {
            it.put("name", name)
            it.put("count", count)
            it.put("items", items.takeIf { it.isNotEmpty() }?.reduce { acc, s -> "$acc,$s" } ?: "")
        }
    }

    override fun getPrimaryKeyValues(): Array<String> {
        return arrayOf(name)
    }
}
