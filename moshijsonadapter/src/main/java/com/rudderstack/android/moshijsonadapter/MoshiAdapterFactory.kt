package com.rudderstack.android.moshijsonadapter

import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.JsonAdapterFactory
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter


class MoshiAdapterFactory : JsonAdapterFactory() {
    override fun getAdapter(): JsonAdapter {
        return MoshiAdapter()
    }

}