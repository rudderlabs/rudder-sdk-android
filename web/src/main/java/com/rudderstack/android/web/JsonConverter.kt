package com.rudderstack.android.web

interface JsonConverter {

    fun <T:Any> serialize(obj : T) : String
    fun <T> deserialize ( json: String) : T
}