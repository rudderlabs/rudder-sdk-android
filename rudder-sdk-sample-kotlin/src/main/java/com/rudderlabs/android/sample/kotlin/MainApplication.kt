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
        var rudderClient: RudderClient? = null
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            BuildConfig.WRITE_KEY,
            RudderConfig.Builder()
                .withEndPointUri(BuildConfig.END_POINT_URL)
                .withLogLevel(
                    when (BuildConfig.DEBUG) {
                        true -> RudderLogger.RudderLogLevel.DEBUG
                        false -> RudderLogger.RudderLogLevel.NONE
                    }
                )
                .build()
        ); }
}