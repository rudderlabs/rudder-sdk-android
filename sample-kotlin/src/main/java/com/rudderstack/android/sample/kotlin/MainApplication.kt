package com.rudderstack.android.sample.kotlin

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import com.rudderstack.android.sdk.core.RudderClient

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://6b1a12340e0b.ngrok.io"
        const val CONTROL_PLANE_URL = "https://6b1a12340e0b.ngrok.io"
        const val WRITE_KEY = "1bt0vcThjsXCUngMjgTFB62xAyg"
    }

    override fun onCreate() {
        super.onCreate()
        var input = mapOf("Bugsnag" to true)
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withControlPlaneUrl(CONTROL_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .withFactory(DummyGAIntegrationImpl.FACTORY)
                .withDefaultOptions(input)
                .build()
        )

        rudderClient!!.rudderContext.putDeviceToken("some_device_token")
    }
}
