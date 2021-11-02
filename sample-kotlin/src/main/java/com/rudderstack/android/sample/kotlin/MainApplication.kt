package com.rudderstack.android.sample.kotlin

import android.app.Application
import android.os.Handler
import android.os.StrictMode
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import com.squareup.leakcanary.LeakCanary

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://3b09-2409-4070-4e0a-1bf5-3c78-9ef4-50c8-2ff3.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1pTxG1Tqxr7FCrqIy7j0p28AENV"
    }

    override fun onCreate() {
        super.onCreate()

//        StrictMode.ThreadPolicy.Builder()
//            .detectAll()
//            .penaltyLog()
//            .build()
//            .let { StrictMode.setThreadPolicy(it) }
//
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);

rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.NONE)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .build()
        )
    }
}