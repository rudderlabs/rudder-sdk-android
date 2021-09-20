package com.rudderstack.android.rudderjsonadapter

import java.lang.reflect.Type

interface JsonAdapter {
    fun <T> readJsonParameterized(typeAdapter:RudderTypeAdapter<T>, json: String) : T?
    fun writeToJson(obj : Any) : String?
    fun <T> readJson(json: String, resultClass : Class<T>) : T
}