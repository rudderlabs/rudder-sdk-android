package com.rudderstack.android.gsonrudderadapter

import com.google.gson.reflect.TypeToken
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter

class GsonAdapter : JsonAdapter {
    override fun <T> writeToJson(): String? {
        val type = (object : TypeToken<T>(){}).type
        return null
    }
}