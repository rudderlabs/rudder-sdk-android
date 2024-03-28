package com.rudderstack.models.android

import com.google.gson.annotations.SerializedName

class RudderLibraryInfo(
    // = BuildConfig.LIBRARY_PACKAGE_NAME
    @SerializedName("name")
    private val name: String? = null,

    @SerializedName("version")
    private val version: String = "1.2.1",
)
