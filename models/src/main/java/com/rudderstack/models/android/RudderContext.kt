/*
 * Creator: Debanjan Chatterjee on 15/12/21, 3:21 PM Last modified: 12/12/21, 8:02 PM
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

package com.rudderstack.models.android

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class RudderContext {
    @SerializedName("app")
    @JsonProperty("app")
    @Json(name = "app")
    private var app: RudderApp? = null

    @SerializedName("traits")
    @JsonProperty("traits")
    @Json(name = "traits")
    private var traits: MutableMap<String, Any?>? = null

    @SerializedName("library")
    @JsonProperty("library")
    @Json(name = "library")
    private var libraryInfo: RudderLibraryInfo? = null

    @SerializedName("os")
    @JsonProperty("os")
    @Json(name = "os")
    private var osInfo: RudderOSInfo? = null

    @SerializedName("screen")
    @JsonProperty("screen")
    @Json(name = "screen")
    private var screenInfo: RudderScreenInfo? = null

    @SerializedName("userAgent")
    @JsonProperty("userAgent")
    @Json(name = "userAgent")
    private var userAgent: String? = null

    @SerializedName("locale")
    @JsonProperty("locale")
    @Json(name = "locale")
    private var locale: String? = null

    @SerializedName("device")
    @JsonProperty("device")
    @Json(name = "device")
    private var deviceInfo: RudderDeviceInfo? = null

    @SerializedName("network")
    @JsonProperty("network")
    @Json(name = "network")
    private var networkInfo: RudderNetwork? = null

    @SerializedName("timezone")
    @JsonProperty("timezone")
    @Json(name = "timezone")
    private var timezone: String? = null

    @SerializedName("externalId")
    @JsonProperty("externalId")
    @Json(name = "externalId")
    private var externalIds: MutableList<MutableMap<String, Any?>>? = null
    var customContextMap: MutableMap<String, Any>? = null

   }