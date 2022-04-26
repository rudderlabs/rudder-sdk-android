/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.moshirudderadapter

import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
/**
 * @see JsonAdapter
 *
 * A Moshi based implementation of JsonAdapter
 */
class MoshiAdapter(private val moshi : Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()) : JsonAdapter {

    override fun <T> readJson(json: String, typeAdapter: RudderTypeAdapter<T>): T? {
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

    override fun <T : Any> readMap(map: Map<String, Any>, resultClass: Class<T>): T? {
        val serialized = writeToJson(map, object : RudderTypeAdapter<Map<String,Any>>(){})
        return serialized?.let {
            readJson(it, resultClass)
        }
    }


    override fun <T : Any> readJson(json: String, resultClass: Class<T>): T? {
        //in case T is primitive, json needs to be returned as primitive
        return when (resultClass) {
            String::class.java, CharSequence::class.java ->
                json as T
            Int::class.java -> json.toInt() as T
            Double::class.java -> json.toDouble() as T
            Float::class.java -> json.toFloat() as T
            Long::class.java -> json.toLong() as T
            else -> {
                val adapter: com.squareup.moshi.JsonAdapter<T> =
                    moshi.adapter<T>(resultClass) as com.squareup.moshi.JsonAdapter<T>
                return adapter.fromJson(json)
            }

        }

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