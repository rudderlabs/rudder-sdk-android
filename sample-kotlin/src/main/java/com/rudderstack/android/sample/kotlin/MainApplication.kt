package com.rudderstack.android.sample.kotlin

import android.app.Application
import android.content.Context
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import java.util.concurrent.TimeUnit

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://2d76-61-95-158-116.ngrok.io"
        const val CONTROL_PLANE_URL = "https://0e741f50e567.ngrok.io"
        const val WRITE_KEY = "1pAKRv50y15Ti6UWpYroGJaO0Dj"
    }

    override fun onCreate() {
        super.onCreate()


    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
}