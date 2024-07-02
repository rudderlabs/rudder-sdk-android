package com.rudderstack.core.models

import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.io.BufferedReader
import java.io.IOException

object MockResponse {

    var jsonAdapter: JsonAdapter = JacksonAdapter()

    @Throws(NullPointerException::class, IOException::class)
    inline fun <reified T> fromJsonFile(jsonPath: String, typeAdapter: RudderTypeAdapter<T>): T? {
        val reader = javaClass.classLoader?.getResourceAsStream(jsonPath)?.reader()
            ?.let { BufferedReader(it) }
            ?: throw IOException("Null InputStream.")
        val content: String
        reader.use { content = it.readText() }
        return try {
            jsonAdapter.readJson(content, typeAdapter)
        } catch (exception: Exception) {
            throw IOException(exception)
        }
    }
}
