package com.rudderstack.android.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class RudderScreenInfo(
    @SerializedName("density")
    @JsonProperty("density")
    @Json(name = "density")
    private val density: Int = 0,

    @SerializedName("width")
    @JsonProperty("width")
    @Json(name = "width")
    private var width: Int = 0,

    @SerializedName("height")
    @JsonProperty("height")
    @Json(name = "height")
    private var height: Int = 0,
)
