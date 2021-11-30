package com.rudderstack.android.sample.kotlin

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Process
import android.util.Log
import androidx.multidex.MultiDex
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://2569-14-97-100-194.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1n0JdVPZTRUIkLXYccrWzZwdGSx"
        fun getProcessName(application: Application): String? {

            val mypid = Process.myPid()

            val manager = application.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val infos = manager.runningAppProcesses
            for (info in infos) {
                if (info.pid == mypid) {
                    return info.processName
                }
            }
            // may never return null
            return null
        }
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

//        RudderClient.putAnonymousId("anonymous_id_1")
//        RudderClient.putDeviceToken("DevToken2")

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .withCustomFactory(CustomFactory.FACTORY)
                .build()
        )
        Log.e("Debug","Application OnCreate")

        Thread {
            for (i in 1..10) {
                println("Event from Main Application {$i}")
                rudderClient!!.track("Event from Main Application {$i}")
            }
        }.start()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}