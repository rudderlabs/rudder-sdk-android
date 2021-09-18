package com.rudderstack.android.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class OrderEntity(
    @SerializedName("order_id")
    @get:JsonProperty("order_id")
    val orderId: Int,
    @SerializedName("quantity")
    @get:JsonProperty("quantity")
    var quantity : Int,
    @SerializedName("total_price")
    @get:JsonProperty("total_price")
    val totalPrice: Double)
