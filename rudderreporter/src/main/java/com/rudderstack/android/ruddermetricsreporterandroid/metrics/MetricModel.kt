/*
 * Creator: Debanjan Chatterjee on 17/06/23, 5:17 pm Last modified: 17/06/23, 5:17 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.metrics

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import com.rudderstack.android.ruddermetricsreporterandroid.JSerialize
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.squareup.moshi.Json

open class MetricModel<T : Any>(
    val name: String, val type: MetricType, val value: T, val labels: Map<String, String>
) : JSerialize<MetricModel<T>> {

    companion object {
        @Keep
        private const val NAME_TAG = "name"

        @Keep
        private const val TYPE_TAG = "type"

        @Keep
        private const val VALUE_TAG = "value"

        @Keep
        private const val LABELS_TAG = "labels"
    }

    override fun serialize(jsonAdapter: JsonAdapter): String? {
        toMap().let {
            return jsonAdapter.writeToJson(it)
        }
    }

    protected open fun toMap(): Map<String, Any> {
        return mapOf(NAME_TAG to name, TYPE_TAG to type, VALUE_TAG to value, LABELS_TAG to labels)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetricModel<*>) return false

        if (name != other.name) return false
        if (type != other.type) return false
        if (value != other.value) return false
        if (labels != other.labels) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + labels.hashCode()
        return result
    }

    override fun toString(): String {
        return "MetricModel($NAME_TAG ='$name', $TYPE_TAG = $type, $VALUE_TAG = $value, $LABELS_TAG = $labels)"
    }


}

class MetricModelWithId<T : Any>(
    @Transient @JsonIgnore @field:Json(ignore = true) val id: String,
    name: String,
    type: MetricType,
    value: T,
    labels: Map<String, String>
) : MetricModel<T>(name, type, value, labels) {
    companion object {
        @Keep private const val ID_TAG = "id"
    }

    override fun toMap(): Map<String, Any> {
        return super.toMap() + mapOf(ID_TAG to id)
    }

    override fun equals(other: Any?): Boolean {
        return other is MetricModelWithId<*> && other.id == id && super.equals(other)
    }

    override fun hashCode(): Int {
        return 31 * id.hashCode() + super.hashCode()
    }

    override fun toString(): String {
        return "MetricModelWithId(i$ID_TAG='$id'), parent = ${super.toString()})"
    }

}