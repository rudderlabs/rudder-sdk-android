package com.rudderstack.android.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class CustomerEntity(
    @SerializedName("name")
    @get:JsonProperty
    val name : String,
    @SerializedName("address")
    @get:JsonProperty
    val address: String,
    @SerializedName("work_address")
    @get:JsonProperty("work_address")
    val workAddress : String
)
