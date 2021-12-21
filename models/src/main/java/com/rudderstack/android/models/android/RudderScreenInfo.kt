package com.rudderstack.android.models.android

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json


internal class RudderScreenInfo (
    @SerializedName("density")
    @JsonProperty("density")
    @Json(name = "density")
    private val density : Int = 0,

    @SerializedName("width")
    @JsonProperty("width")
    @Json(name = "width")
    private var width : Int= 0,

    @SerializedName("height")
    @JsonProperty("height")
    @Json(name = "height")
    private var height : Int = 0){

    /*init {
        val manager: WindowManager =
            application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (manager != null) {
            val display: Display = manager.getDefaultDisplay()
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            density = displayMetrics.densityDpi
            height = displayMetrics.heightPixels
            width = displayMetrics.widthPixels
        }
    }*/
}