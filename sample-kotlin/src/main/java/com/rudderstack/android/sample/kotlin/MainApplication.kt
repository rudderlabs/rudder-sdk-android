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
        const val DATA_PLANE_URL = "https://8f27920d3fc6.ngrok.io"
        const val CONTROL_PLANE_URL = "https://986733ed.ngrok.io"
        const val WRITE_KEY = "1cGJAn3VgQByqQsU5yhWtsK5nwx"
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .withTrackLifecycleEvents(false)
                .withRecordScreenViews(false)
                .build()
        )

        rudderClient!!.rudderContext.putDeviceToken("some_device_token")
    }
}