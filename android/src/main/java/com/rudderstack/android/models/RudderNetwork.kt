package com.rudderstack.android.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class RudderNetwork(
    @SerializedName("carrier")
    @JsonProperty("carrier")
    @Json(name = "carrier")
    private val carrier: String? = null,

    @SerializedName("wifi")
    @JsonProperty("wifi")
    @Json(name = "wifi")
    private val isWifiEnabled: Boolean = false,

    @SerializedName("bluetooth")
    @JsonProperty("bluetooth")
    @Json(name = "bluetooth")
    private val isBluetoothEnabled: Boolean = false,

    @SerializedName("cellular")
    @JsonProperty("cellular")
    @Json(name = "cellular")
    private val isCellularEnabled: Boolean = false,
)
