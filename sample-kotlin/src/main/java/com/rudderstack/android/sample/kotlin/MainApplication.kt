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
        const val DATA_PLANE_URL = "https://a4f291d1e130.ngrok.io"
        const val CONTROL_PLANE_URL = "https://a4f291d1e130.ngrok.io"
        const val WRITE_KEY = "1cGJAn3VgQByqQsU5yhWtsK5nwx"
    }

    override fun onCreate() {
        super.onCreate()

        RudderClient.setAnonymousId("31de5a69-c27c-4514-bee2-eb94ad5c0b3a");
        RudderClient.updateWithAdvertisingId("some_idfa");
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
        rudderClient!!.putDeviceToken("some_device_token")
    }
}