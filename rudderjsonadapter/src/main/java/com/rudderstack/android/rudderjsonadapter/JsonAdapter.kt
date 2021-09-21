package com.rudderstack.android.rudderjsonadapter

import java.lang.reflect.Type

interface JsonAdapter {
    fun <T> readJsonParameterized(typeAdapter:RudderTypeAdapter<T>, json: String) : T?
    fun <T : Any> writeToJson(obj: T): String?
    fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String?
    fun <T : Any> readJson(json: String, resultClass : Class<T>) : T?
}