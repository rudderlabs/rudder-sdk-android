package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.dummy.DummyGAIntegrationImpl
import com.rudderlabs.android.sdk.core.RudderClient
import com.rudderlabs.android.sdk.core.RudderConfig
import com.rudderlabs.android.sdk.core.RudderLogger
import com.rudderlabs.android.sdk.core.RudderMessageBuilder

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1SuZEl2bawQm274slpZs9y6NdCi"
        private const val END_POINT_URI = "https://f9eb4965.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withEndPointUri(END_POINT_URI)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .build()
        )


    }
}