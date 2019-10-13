package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.sdk.core.RudderClient
import com.rudderlabs.android.sdk.core.RudderConfig
import com.rudderlabs.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        //        private const val WRITE_KEY = "1R3JbxsqWZlbYjJlBxf0ZNWZOH6"
        private const val WRITE_KEY = "1R3JbxsqWZlbYjJlBxf0ZNWZOH6"
        private const val END_POINT_URI = "https://95d625b4.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    WRITE_KEY,
    RudderConfig.Builder()
                .withEndPointUri(END_POINT_URI)
                .withLogLevel(4)
//                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
        .build()
)
    }
}