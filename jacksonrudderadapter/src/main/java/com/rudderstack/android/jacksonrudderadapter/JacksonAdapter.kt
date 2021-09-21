package com.rudderstack.android.jacksonrudderadapter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter

class JacksonAdapter : JsonAdapter {
    private val objectMapper = ObjectMapper().also {
        /**
         * update mapper as required
         */
    }
    override fun <T> readJsonParameterized(typeAdapter: RudderTypeAdapter<T>, json: String): T? {
        val typeRef: TypeReference<T> =
            object : TypeReference<T>() {}
        return objectMapper.readValue(json, typeRef)
    }

    override fun <T : Any> writeToJson(obj: T): String? {
        return objectMapper.writeValueAsString(obj)
    }

    override fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String? {
        return writeToJson(obj)
    }

    override fun <T : Any> readJson(json: String, resultClass: Class<T>): T {
        return objectMapper.readValue(json, resultClass)
    }
}