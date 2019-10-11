package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.sdk.core.RudderClient
import com.rudderlabs.android.sdk.core.RudderConfig

class MainApplication : Application() {
    companion object {
//        private const val WRITE_KEY = "1R3JbxsqWZlbYjJlBxf0ZNWZOH6"
        private const val WRITE_KEY = "1R3JbxsqWZlbYjJlBxf0ZNWZOH6"
        private const val END_POINT_URI = "https://5c40a958.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withEndPointUri(END_POINT_URI)
                .withLogLevel(4)
//                .withFactory(AdjustIntegrationFactory.FACTORY)
                .build()
        )
    }
}