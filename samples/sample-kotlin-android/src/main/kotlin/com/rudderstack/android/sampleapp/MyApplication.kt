package com.rudderstack.android.sampleapp

import android.app.Application
import android.util.Log
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initAnalyticSdk()
    }

    private fun initAnalyticSdk() {
        RudderAnalyticsUtils.initialize(
            this
        ) { instanceName, success, message ->
            if (success) Log.d("RudderStack", "Analytics initialized for $instanceName")
            else Log.e("RudderStack", "Analytics failed to initialize for $instanceName: $message")
        }
    }
}
