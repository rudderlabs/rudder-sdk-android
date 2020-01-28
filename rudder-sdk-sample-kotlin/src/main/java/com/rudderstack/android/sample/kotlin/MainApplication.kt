package com.rudderstack.android.sample.kotlin

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        val TAG = "MainApplication"
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            "1TSRSskqa15PG7F89tkwEbl5Td8",
            RudderConfig.Builder()
                .withEndPointUri("https://a638edbb.ngrok.io")
                .withLogLevel(
                    when (BuildConfig.DEBUG) {
                        true -> RudderLogger.RudderLogLevel.VERBOSE
                        false -> RudderLogger.RudderLogLevel.NONE
                    }
                )
                .withTrackLifecycleEvents(true)
                .build()
        )

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity) {
                Log.d(TAG, "onActivityPaused ${p0.localClassName}")
            }

            override fun onActivityStarted(p0: Activity) {
                Log.d(TAG, "onActivityStarted ${p0.localClassName}")
            }

            override fun onActivityDestroyed(p0: Activity) {
                Log.d(TAG, "onActivityDestroyed ${p0.localClassName}")
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
                Log.d(TAG, "onActivitySaveInstanceState ${p0.localClassName}, Bundle: $p1")
            }

            override fun onActivityStopped(p0: Activity) {
                Log.d(TAG, "onActivityStopped ${p0.localClassName}")
            }

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                Log.d(TAG, "onActivityCreated ${p0.localClassName} Bundle: ${p1.toString()}")
            }

            override fun onActivityResumed(p0: Activity) {
                Log.d(TAG, "onActivityResumed ${p0.localClassName}")
            }

        })
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d(TAG, "onLowMemory")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory: $level")
    }
}