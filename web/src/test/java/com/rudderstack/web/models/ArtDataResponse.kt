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

package com.rudderstack.web.models


import com.fasterxml.jackson.annotation.JsonProperty
import com.rudderstack.web.Info
import com.squareup.moshi.Json

data class ArtDataResponse(
    @Json(name = "config")
    @JsonProperty("config")
    val config: Config,
    @Json(name = "data")
    @JsonProperty("data")
    val `data`: Data,
    @Json(name = "info")
    @JsonProperty("info")
    val info: Info
)