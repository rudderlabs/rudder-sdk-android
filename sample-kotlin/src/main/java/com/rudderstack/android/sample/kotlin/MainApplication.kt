package com.rudderstack.android.sample.kotlin

import android.app.Application
import android.os.Handler
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://e5757ebd4d1a.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1pTxG1Tqxr7FCrqIy7j0p28AENV"
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
                .withCustomFactory(CustomFactory.FACTORY)
                .build()
        )
        rudderClient!!.putDeviceToken("some_device_token")
        rudderClient!!.track("first_event")

        Handler().postDelayed({
            RudderClient.updateWithAdvertisingId("some_idfa_changed")
            rudderClient!!.track("second_event")
        }, 3000)
    }
}