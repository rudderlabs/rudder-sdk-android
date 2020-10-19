package com.rudderstack.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://69f2b920a648.ngrok.io"
        const val CONTROL_PLANE_URL = "https://8a7ec66bc40a.ngrok.io"
        const val WRITE_KEY = "1ZOVzjHRL0Vpk627qpkmcIYLrv3"
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
//                .withFactory(AdjustIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .build()
        )
//
//        rudderClient!!.rudderContext.putDeviceToken("some_device_token")

//        val adjust = Adjust.getDefaultInstance()
//        val apiToken: String? = "t1yurrb968zk"
//        val delay = 7.0
//        val adjustConfig = AdjustConfig(
//            this,
//            apiToken,
//            AdjustConfig.ENVIRONMENT_SANDBOX
//        )
//        adjustConfig.setLogLevel(LogLevel.VERBOSE)
//        if (delay > 0) {
//            adjustConfig.setDelayStart(delay)
//        }

//        adjustConfig.setOnAttributionChangedListener { attribution ->
//            Log.d("AdjustFactory", "Attribution callback called!")
//            Log.d("AdjustFactory", "Attribution: $attribution")
//        }
//        adjustConfig.setOnEventTrackingSucceededListener { eventSuccessResponseData ->
//            Log.d("AdjustFactory", "Event success callback called!")
//            Log.d("AdjustFactory", "Event success data: $eventSuccessResponseData")
//        }
//        adjustConfig.setOnEventTrackingFailedListener { eventFailureResponseData ->
//            Log.d("AdjustFactory", "Event failure callback called!")
//            Log.d("AdjustFactory", "Event failure data: $eventFailureResponseData")
//        }
//        adjustConfig.setOnSessionTrackingSucceededListener { sessionSuccessResponseData ->
//            Log.d("AdjustFactory", "Session success callback called!")
//            Log.d("AdjustFactory", "Session success data: $sessionSuccessResponseData")
//        }
//        adjustConfig.setOnSessionTrackingFailedListener { sessionFailureResponseData ->
//            Log.d("AdjustFactory", "Session failure callback called!")
//            Log.d("AdjustFactory", "Session failure data: $sessionFailureResponseData")
//        }
//        adjustConfig.setOnDeeplinkResponseListener { deeplink ->
//            Log.d("AdjustFactory", "Deferred deep link callback called!")
//            Log.d("AdjustFactory", "Deep link URL: $deeplink")
//            true
//        }
//        adjustConfig.setSendInBackground(true)
//        adjust.onCreate(adjustConfig)
//        this.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
//            override fun onActivityCreated(activity: Activity, bundle: Bundle) {}
//            override fun onActivityStarted(activity: Activity) {}
//            override fun onActivityResumed(activity: Activity) {
//                Adjust.onResume()
//            }
//
//            override fun onActivityPaused(activity: Activity) {
//                Adjust.onPause()
//            }
//
//            override fun onActivityStopped(activity: Activity) {}
//            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
//            override fun onActivityDestroyed(activity: Activity) {}
//        })
    }
}