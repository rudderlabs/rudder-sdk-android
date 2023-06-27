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

import com.rudderstack.android.ruddermetricsreporterandroid.JSerialize
import com.rudderstack.rudderjsonadapter.JsonAdapter

open class MetricModel<T : Any>(val name: String, val type: MetricType,
                                val value: T, val labels: Map<String,String>) : JSerialize<MetricModel<T>> {
    override fun serialize(jsonAdapter: JsonAdapter): String? {
        mapOf<String, Any>("name" to name, "type" to type, "value" to value, "labels" to labels).let {
            return jsonAdapter.writeToJson(it)
        }
    }
}

class MetricModelWithId<T : Any>(val id: String, name: String, type: MetricType,
                                      value: T, labels: Map<String,String>) : MetricModel<T>(name, type, value, labels) {
    override fun serialize(jsonAdapter: JsonAdapter): String? {
        mapOf<String, Any>("id" to id, "name" to name, "type" to type, "value" to value, "labels" to labels).let {
            return jsonAdapter.writeToJson(it)
        }
    }
}