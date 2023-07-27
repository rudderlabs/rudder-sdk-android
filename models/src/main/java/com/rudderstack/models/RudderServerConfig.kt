/*
 * Creator: Debanjan Chatterjee on 03/11/21, 11:47 PM Last modified: 21/10/21, 7:39 PM
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
package com.rudderstack.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.io.Serializable
import java.util.*

/**
 * Configuration of the server
 * @property source
 */
data class RudderServerConfig(
    @Json(name = "isHosted")
    @JsonProperty("isHosted")
    @SerializedName("isHosted")
    val isHosted: Boolean = false,

    @Json(name = "source")
    @JsonProperty("source")
    @SerializedName("source")
    val source: RudderServerConfigSource? = null
) : Serializable {

    /**
     * Configuration of source
     *
     * @property sourceId
     * @property sourceName
     * @property isSourceEnabled
     * @property updatedAt
     * @property destinations
     */
    data class RudderServerConfigSource(
        @Json(name = "id")
        @JsonProperty("id")
        @SerializedName("id")
        val sourceId: String? = null,

        @Json(name = "name")
        @JsonProperty("name")
        @SerializedName("name")
        val sourceName: String? = null,

        @Json(name = "enabled")
        @JsonProperty("enabled")
        @SerializedName("enabled")
        val isSourceEnabled: Boolean = false,

        @Json(name = "updatedAt")
        @JsonProperty("updatedAt")
        @SerializedName("updatedAt")
        val updatedAt: String? = null,

        @Json(name = "destinations")
        @JsonProperty("destinations")
        @SerializedName("destinations")
        val destinations: List<RudderServerDestination>? = null
    ) : Serializable

    data class RudderServerDestination(
        @Json(name = "id")
        @JsonProperty("id")
        @SerializedName("id")
        val destinationId: String,

        @Json(name = "name")
        @JsonProperty("name")
        @SerializedName("name")
        val destinationName: String? = null,

        @Json(name = "enabled")
        @JsonProperty("enabled")
        @SerializedName("enabled")
        val isDestinationEnabled: Boolean = false,

        @Json(name = "updatedAt")
        @JsonProperty("updatedAt")
        @SerializedName("updatedAt")
        val updatedAt: String? = null,

        @Json(name = "destinationDefinition")
        @JsonProperty("destinationDefinition")
        @SerializedName("destinationDefinition")
        val destinationDefinition: RudderServerDestinationDefinition? = null,

        @Json(name = "config")
        @JsonProperty("config")
        @SerializedName("config")
        val destinationConfig: Map<String, Any>,
        @JsonProperty("areTransformationsConnected")
        @Json(name = "areTransformationsConnected")
        val areTransformationsConnected : Boolean = false
    ) : Serializable

    data class RudderServerDestinationDefinition(

        @Json(name = "name")
        @JsonProperty("name")
        @SerializedName("name")
        val definitionName: String? = null,

        @Json(name = "displayName")
        @JsonProperty("displayName")
        @SerializedName("displayName")
        val displayName: String? = null,

        @Json(name = "updatedAt")
        @JsonProperty("updatedAt")
        @SerializedName("updatedAt")
        val updatedAt: String? = null
    ) : Serializable
}