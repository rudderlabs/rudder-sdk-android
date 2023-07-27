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

package com.rudderstack.android.jacksonrudderadapter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import java.lang.reflect.Type

/**
 * @see JsonAdapter
 *
 * A Jackson based implementation of JsonAdapter
 */
class JacksonAdapter : JsonAdapter {
    private val objectMapper = ObjectMapper().also {
        /**
         * update mapper as required
         */
        it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    override fun <T> readJson(json: String, typeAdapter: RudderTypeAdapter<T>): T? {
        val type = typeAdapter.type ?: return null

        val typeRef: TypeReference<T> =
            object : TypeReference<T>() {

                override fun getType(): Type {
                    return type
                }
            }
        return objectMapper.readValue(json, typeRef)
    }

    override fun <T : Any> writeToJson(obj: T): String? {
        return objectMapper.writeValueAsString(obj)
    }

    override fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String? {
        return writeToJson(obj)
    }

    override fun <T : Any> readMap(map: Map<String, Any>, resultClass: Class<T>): T? {
        return objectMapper.convertValue(map, resultClass)
    }

    override fun <T : Any> readJson(json: String, resultClass: Class<T>): T {
        return objectMapper.readValue(json, resultClass)
    }
}
