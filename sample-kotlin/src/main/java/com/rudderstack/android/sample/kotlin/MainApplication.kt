package com.rudderstack.android.sample.kotlin

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.multidex.MultiDex
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://a86b-2409-4070-2c11-8b9f-903f-e344-da4f-136b.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1n0JdVPZTRUIkLXYccrWzZwdGSx"
    }

    override fun onCreate() {
        super.onCreate()

//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
//                .withTrackLifecycleEvents(false)
//                .withRecordScreenViews(false)
//                .build(), RudderOption()
//                .putIntegration("MIXPANEL",true)


//        )
        RudderClient.putDeviceToken("DEVTOKEN1");

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .withCustomFactory(CustomFactory.FACTORY)
                .build()
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}