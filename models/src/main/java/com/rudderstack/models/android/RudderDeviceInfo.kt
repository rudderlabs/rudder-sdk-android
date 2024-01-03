package com.rudderstack.models.android

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class RudderDeviceInfo(
    /*RudderDeviceInfo(String advertisingId, String token) {
        this.deviceId = Utils.getDeviceId(RudderClient.getApplication());
        if (advertisingId != null && !advertisingId.isEmpty()) {
            this.advertisingId = advertisingId;
            this.adTrackingEnabled = true;
        }
        if (token != null && !token.isEmpty()) {
            this.token = token;
        }
    }*/
    @SerializedName("id")
    @JsonProperty("id")
    @Json(name = "id")
    val deviceId: String? = null,

    @SerializedName("manufacturer")
    @JsonProperty("manufacturer")
    @Json(name = "manufacturer")
    private val manufacturer: String /*= Build.MANUFACTURER*/,

    @SerializedName("model")
    @JsonProperty("model")
    @Json(name = "model")
    private val model: String /*= Build.MODEL*/,

    @SerializedName("name")
    @JsonProperty("name")
    @Json(name = "name")
    private val name: String /*= Build.DEVICE*/,

    @SerializedName("type")
    @JsonProperty("type")
    @Json(name = "type")
    private val type: String = "Android",

    @SerializedName("token")
    @JsonProperty("token")
    @Json(name = "token")
    private var token: String? = null,

    @SerializedName("adTrackingEnabled")
    @JsonProperty("adTrackingEnabled")
    @Json(name = "adTrackingEnabled")
    var isAdTrackingEnabled: Boolean? = null,
) {

    @SerializedName("advertisingId")
    var advertisingId: String? = null
    fun setToken(token: String?) {
        this.token = token
    }
}
