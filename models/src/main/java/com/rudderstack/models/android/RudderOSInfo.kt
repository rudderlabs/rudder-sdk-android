package com.rudderstack.models.android

import com.google.gson.annotations.SerializedName

class RudderOSInfo(
    @SerializedName("name")
    private val name: String = "Android",

    @SerializedName("version") // = Build.VERSION.RELEASE
    private val version: String? = null,
)
