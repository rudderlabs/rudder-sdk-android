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
        const val DATA_PLANE_URL = "https://7203f5894e2f.ngrok.io"
        const val WRITE_KEY = "1cZKYwoLsDiMaw9WUvxLsG8GDc8"
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .build()
        )

        rudderClient!!.rudderContext.putDeviceToken("some_device_token")
    }
}