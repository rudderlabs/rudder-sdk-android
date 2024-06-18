package com.rudderstack.android.sampleapp.analytics.workmanger

import android.app.Application
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils
import com.rudderstack.android.sync.WorkManagerAnalyticsFactory
import com.rudderstack.core.Analytics

class SampleWorkManagerAnalyticsFactory : WorkManagerAnalyticsFactory {

    override fun createAnalytics(application: Application): Analytics {
        return RudderAnalyticsUtils.analytics
    }
}
