/*
 * Creator: Debanjan Chatterjee on 19/06/23, 8:04 pm Last modified: 19/06/23, 8:04 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.error

import com.fasterxml.jackson.annotation.JsonIgnore
import com.rudderstack.android.ruddermetricsreporterandroid.JSerialize
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.squareup.moshi.Json

class ErrorModel(
    private val libraryMetadata: LibraryMetadata,
    internal val eventsJson: List<String>) : JSerialize<ErrorModel> {

    override fun serialize(jsonAdapter: JsonAdapter): String? {
        return jsonAdapter.writeToJson(toMap(jsonAdapter))
    }
    fun toMap(jsonAdapter: JsonAdapter) = mapOf("events" to
            eventsJson.map {
                jsonAdapter.readJson(it,
                    RudderTypeAdapter<Map<String, Any>>{}
                )
            },
        "payloadVersion" to 5,
        "notifier" to mapOf(
            "name" to libraryMetadata.name,
            "version" to libraryMetadata.sdkVersion,
            "url" to "https://github.com/rudderlabs/rudder-sdk-android",
            "os_version" to libraryMetadata.osVersion
        )
    )
}