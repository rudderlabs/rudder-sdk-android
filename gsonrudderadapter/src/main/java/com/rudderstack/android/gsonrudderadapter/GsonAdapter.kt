package com.rudderstack.android.gsonrudderadapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter

class GsonAdapter : JsonAdapter {
    private val gson = GsonBuilder().create()
    override fun <T> readJson(json: String, typeAdapter: RudderTypeAdapter<T>): T? {
        return gson.fromJson(json, object : TypeToken<T>() {}.type)
    }

    override fun <T : Any> writeToJson(obj: T): String? {
        return gson.toJson(obj)
    }

    override fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String? {
        return writeToJson(obj)
    }

    override fun <T : Any> readJson(json: String, resultClass: Class<T>): T {
        return gson.fromJson(json, resultClass)
    }
}