package com.rudderstack.android.sampleapp

import android.app.Application
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initAnalyticSdk()
    }

    private fun initAnalyticSdk() {
        RudderAnalyticsUtils.initialize(this)
    }
}
