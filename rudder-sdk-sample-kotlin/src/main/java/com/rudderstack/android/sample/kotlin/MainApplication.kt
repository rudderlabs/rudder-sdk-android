package com.rudderstack.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            BuildConfig.WRITE_KEY,
            RudderConfig.Builder()
                .withEndPointUri(BuildConfig.END_POINT_URL)
                .withLogLevel(
                    when (BuildConfig.DEBUG) {
                        true -> RudderLogger.RudderLogLevel.DEBUG
                        false -> RudderLogger.RudderLogLevel.NONE
                    }
                )
                .withTrackLifecycleEvents(true)
                .build()
        ); }
}