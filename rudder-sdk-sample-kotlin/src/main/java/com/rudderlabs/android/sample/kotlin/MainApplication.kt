package com.rudderlabs.android.sample.kotlin

import android.app.Application
import android.content.res.Resources
import com.rudderlabs.android.integration.dummy.DummyGAIntegrationImpl
import com.rudderlabs.android.sdk.core.RudderClient
import com.rudderlabs.android.sdk.core.RudderConfig
import com.rudderlabs.android.sdk.core.RudderLogger
import com.rudderlabs.android.sdk.core.RudderMessageBuilder

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1TSRSskqa15PG7F89tkwEbl5Td8"
        private const val END_POINT_URI = "https://019f1fdb.ngrok.io"
        var rudderClient: RudderClient? = null
    }

    override fun onCreate() {
        super.onCreate()

        val writeKey = resources.getString(R.string.write_key)
        val endPointUrl = resources.getString(R.string.end_point_url)

        rudderClient = RudderClient.getInstance(
            this,
            writeKey,
            RudderConfig.Builder()
                .withEndPointUri(endPointUrl)
                .withLogLevel(
                    when (BuildConfig.DEBUG) {
                        true -> RudderLogger.RudderLogLevel.DEBUG
                        false -> RudderLogger.RudderLogLevel.NONE
                    }
                )
                .build()
        ); }
}