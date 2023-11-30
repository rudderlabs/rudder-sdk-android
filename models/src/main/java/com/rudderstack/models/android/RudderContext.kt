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

class RudderContext(contextMap: Map<String, Any?>) : HashMap<String, Any?>(contextMap) {
    constructor() : this(mapOf())

    var app: RudderApp? by this

    var traits: RudderTraits? by this

    var library: RudderLibraryInfo? by this

    var os: RudderOSInfo? by this

    var screen: RudderScreenInfo? by this

    var userAgent: String? by this

    var locale: String? by this

    var device: RudderDeviceInfo? by this

    var network: RudderNetwork? by this

    var timezone: String? by this

    var externalId: MutableSet<Map<String, Any?>>? by this
    var customContextMap: MutableMap<String, Any>? by this

   }