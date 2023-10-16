/*
 * Creator: Debanjan Chatterjee on 22/06/23, 5:01 pm Last modified: 22/06/23, 5:01 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.squareup.moshi.Json

data class LibraryMetadata(
    @get:JsonProperty("name")
    @SerializedName("name")
    @Json(name = "name")
    val name: String,
    @get:JsonProperty("sdk_version")
    @SerializedName("sdk_version")
    @Json(name = "sdk_version")
    val sdkVersion: String,
    @get:JsonProperty("version_code")
    @SerializedName("version_code")
    @Json(name = "version_code")
    val versionCode: String,
    @get:JsonProperty("write_key")
    @SerializedName("write_key")
    @Json(name = "write_key")
    val writeKey: String
) : JSerialize<LibraryMetadata> {
    override fun serialize(jsonAdapter: JsonAdapter): String? {
        return jsonAdapter.writeToJson(this)
    }
}