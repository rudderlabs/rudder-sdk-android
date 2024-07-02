package com.rudderstack.android.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class RudderApp(
    @SerializedName("build")
    @JsonProperty("build")
    @Json(name = "build")
    private val build: String,
    @SerializedName("name")
    @JsonProperty("name")
    @Json(name = "name")
    private val name: String,
    @SerializedName("namespace")
    @JsonProperty("namespace")
    @Json(name = "namespace")
    private val nameSpace: String,
    @SerializedName("version")
    @JsonProperty("version")
    @Json(name = "version")
    private val version: String,
)
