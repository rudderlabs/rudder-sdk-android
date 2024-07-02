package com.rudderstack.android.models

import com.google.gson.annotations.SerializedName

class RudderLibraryInfo(
    @SerializedName("name")
    private val name: String? = null,

    @SerializedName("version")
    private val version: String = "1.2.1",
)
