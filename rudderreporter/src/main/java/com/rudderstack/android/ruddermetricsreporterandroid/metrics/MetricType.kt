/*
 * Creator: Debanjan Chatterjee on 13/06/23, 7:44 pm Last modified: 13/06/23, 7:44 pm
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

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

enum class MetricType(val value: String) {
    @SerializedName("count")
    @JsonProperty("count")
    @Json(name = "count")
    COUNTER("count"),
    @SerializedName("gauge")
    @JsonProperty("gauge")
    @Json(name = "gauge")
    GAUGE("gauge");

    companion object {
        fun getType(type: String): MetricType {
            return when (type) {
                "count" -> COUNTER
                "gauge" -> GAUGE
                else -> throw IllegalArgumentException("Invalid metric type $type")
            }
        }
    }
}