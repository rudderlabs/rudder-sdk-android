package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.dummy.DummyGAIntegrationImpl
import com.rudderlabs.android.sdk.core.RudderClient
import com.rudderlabs.android.sdk.core.RudderConfig
import com.rudderlabs.android.sdk.core.RudderLogger
import com.rudderlabs.android.sdk.core.RudderMessageBuilder

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1TSRSskqa15PG7F89tkwEbl5Td8"
        private const val END_POINT_URI = "https://664970a4.ngrok.io"
    }

    override fun onCreate() {
        super.onCreate()
    }
}