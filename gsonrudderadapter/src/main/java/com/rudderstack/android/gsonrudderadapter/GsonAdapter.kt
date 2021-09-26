/*
 * Creator: Debanjan Chatterjee on 24/09/21, 11:09 PM Last modified: 22/09/21, 8:00 PM
 * Copyright: All rights reserved Ⓒ 2021 http://hiteshsahu.com
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

package com.rudderstack.android.gsonrudderadapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import com.google.gson.JsonElement




class GsonAdapter : JsonAdapter {
    private val gson = GsonBuilder().create()
    override fun <T> readJson(json: String, typeAdapter: RudderTypeAdapter<T>): T? {
        return gson.fromJson(json, typeAdapter.type)
    }

    override fun <T : Any> writeToJson(obj: T): String? {
        return gson.toJson(obj)
    }

    override fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String? {
        return writeToJson(obj)
    }

    override fun <T : Any> readMap(map: Map<String, Any>, resultClass: Class<T>): T? {
        val jsonElement = gson.toJsonTree(map)
        return gson.fromJson(jsonElement, resultClass)

    }

    override fun <T : Any> readJson(json: String, resultClass: Class<T>): T {
        return gson.fromJson(json, resultClass)
    }
}