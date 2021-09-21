package com.rudderstack.android.moshijsonadapter

import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.ParameterizedType

class MoshiAdapter : JsonAdapter {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    override fun <T> readJsonParameterized(typeAdapter: RudderTypeAdapter<T>, json: String): T? {
        val jsonAdapter = typeAdapter.type?.let {
            moshi.adapter<T>(it)
        }

        return jsonAdapter?.fromJson(json)
    }

    override fun <T : Any> writeToJson(obj: T): String? {
        return writeToJson(obj, null)
    }

    override fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String? {
        val adapter: com.squareup.moshi.JsonAdapter<T> =
            createMoshiAdapter<T>(typeAdapter, obj)
        return adapter.toJson(obj)
    }


    override fun <T : Any> readJson(json: String, resultClass: Class<T>): T? {
        val adapter: com.squareup.moshi.JsonAdapter<T> =
            moshi.adapter<T>(resultClass) as com.squareup.moshi.JsonAdapter<T>
        return adapter.fromJson(json)
    }

    private fun <T : Any> createMoshiAdapter(
        typeAdapter: RudderTypeAdapter<T>?,
        obj: T
    ): com.squareup.moshi.JsonAdapter<T> {
        return typeAdapter?.type?.let {
            moshi.adapter(it)
        } ?: moshi.adapter<T>(obj::class.java)
    }
}