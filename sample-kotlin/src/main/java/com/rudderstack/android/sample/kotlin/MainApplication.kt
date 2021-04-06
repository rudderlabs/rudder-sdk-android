package com.rudderstack.android.sample.kotlin

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderOption

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://28dd551d85e3.ngrok.io"
        const val CONTROL_PLANE_URL = "https://a4f291d1e130.ngrok.io"
        const val WRITE_KEY = "1qnO5R5NMgI5oRHToPsWmau2Jcz"
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
        //rudderClient!!.putDeviceToken("some_device_token")
    }
}