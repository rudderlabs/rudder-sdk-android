package com.rudderstack.android.jacksonrudderadapter

import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.JsonAdapterFactory

class JacksonAdapterFactory : JsonAdapterFactory() {
    override fun getAdapter(): JsonAdapter {
        return JacksonAdapter()
    }
}