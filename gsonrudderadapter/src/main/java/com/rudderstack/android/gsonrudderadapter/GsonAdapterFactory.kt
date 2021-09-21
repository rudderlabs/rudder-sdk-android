package com.rudderstack.android.gsonrudderadapter

import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.JsonAdapterFactory

class GsonAdapterFactory : JsonAdapterFactory() {
    override fun getAdapter(): JsonAdapter {
        return GsonAdapter()
    }
}