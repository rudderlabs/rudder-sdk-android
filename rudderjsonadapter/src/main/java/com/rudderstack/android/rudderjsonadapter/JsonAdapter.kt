package com.rudderstack.android.rudderjsonadapter

import java.lang.reflect.Type

interface JsonAdapter {
    fun <T> readJson( json: String,typeAdapter:RudderTypeAdapter<T>) : T?
    fun <T : Any> writeToJson(obj: T): String?
    fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String?
    fun <T : Any> readJson(json: String, resultClass : Class<T>) : T?
}