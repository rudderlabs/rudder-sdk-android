package com.rudderstack.android.sample.kotlin

import android.app.Application

import android.content.Context
import androidx.multidex.MultiDex
import androidx.work.Configuration

import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application(), Configuration.Provider {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://rudderstacz.dataplane.rudderstack.com" //"https://6dbd-175-101-36-4.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1wvsoF3Kx2SczQNlx1dvcqW9ODW" //"1pAKRv50y15Ti6UWpYroGJaO0Dj"
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(false)
                .build()
        )

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    // To initialize WorkManager on demand instead of on startup
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}