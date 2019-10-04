package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.adjust.AdjustIntegrationFactory
import com.rudderlabs.android.sdk.core.RudderClient
import com.rudderlabs.android.sdk.core.RudderConfigBuilder

class MainApplication : Application() {
    companion object {
//        private const val WRITE_KEY = "1R3JbxsqWZlbYjJlBxf0ZNWZOH6"
        private const val WRITE_KEY = "1R3JbxsqWZlbYjJlBxf0ZNWZOH6"
        private const val END_POINT_URI = "https://95d625b4.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfigBuilder()
                .withEndPointUri(END_POINT_URI)
                .withFactory(AdjustIntegrationFactory.FACTORY)
                .build()
        )
    }
}