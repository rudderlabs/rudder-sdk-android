package com.rudderstack.android.sample.kotlin

import android.app.Application
import android.content.Context
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://6dbd-175-101-36-4.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1pAKRv50y15Ti6UWpYroGJaO0Dj"
    }

    override fun onCreate() {
        super.onCreate()
        thread(start = true) {
            for (i in 1..100) {
                println("${Thread.currentThread()} $i has run.")
            }
        }
//        val rudderConfig = RudderConfig.Builder()
//            .withDataPlaneUrl(MainApplication.DATA_PLANE_URL)
//            .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//            .withTrackLifecycleEvents(false)
//            .withFlushPeriodically(15, TimeUnit.MINUTES)
//            .withFlushQueueSize(90)
//            .withSleepCount(180)
//            .withRecordScreenViews(false)
//            .build()
//
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            rudderConfig
//        )


    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
}