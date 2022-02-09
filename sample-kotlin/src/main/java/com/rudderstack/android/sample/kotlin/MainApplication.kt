package com.rudderstack.android.sample.kotlin

import android.app.Application
import android.content.Context
import android.os.Handler
//import androidx.multidex.MultiDex
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import com.rudderstack.android.sdk.core.RudderProperty

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://f6f2-175-101-36-4.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1pAKRv50y15Ti6UWpYroGJaO0Dj"
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
//        RudderClient.setAnonymousId("anonymous_id")
//        RudderClient.updateWithAdvertisingId("DEVTOKEN1")

        RudderClient.putAnonymousId("anonymous_id_1")
        RudderClient.putDeviceToken("DevToken2")

        val rudderConfig = RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.NONE)
                .withTrackLifecycleEvents(false)
                .withFlushQueueSize(90)
                .withSleepCount(180)
                .withRecordScreenViews(false)
//                .withCustomFactory(CustomFactory.FACTORY)
                .build()

        rudderClient = RudderClient.getInstance(
                this,
                WRITE_KEY,
                rudderConfig
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
//        MultiDex.install(this)
    }
}