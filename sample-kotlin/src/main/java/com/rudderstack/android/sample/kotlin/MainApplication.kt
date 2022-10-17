package com.rudderstack.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integration.braze.BrazeIntegrationFactory
import com.rudderstack.android.integrations.amplitude.AmplitudeIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "your_dataplane_url"
        const val CONTROL_PLANE_URL = "your_control_plane_url"
        const val WRITE_KEY = "write_key"
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withControlPlaneUrl(CONTROL_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .withAutoCollectAdvertId(false)
                .withFactory(AmplitudeIntegrationFactory.FACTORY)
                .withFactory(BrazeIntegrationFactory.FACTORY)
                .build()
        )

    }
}