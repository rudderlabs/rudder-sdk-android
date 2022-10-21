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
//        const val DATA_PLANE_URL = "https://rudderstacbumvdrexzj.dataplane.rudderstack.com"
//        const val CONTROL_PLANE_URL = "https://api.dev.rudderlabs.com"
//        const val WRITE_KEY = "2FzbBRoGHjlwlTFprKlvbwaE9uH"

        const val DATA_PLANE_URL = "https://rudderstacgwyx.dataplane.rudderstack.com"
        const val WRITE_KEY = "1xXCubSHWXbpBI2h6EpCjKOsxmQ"
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withControlPlaneUrl(CONTROL_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.NONE)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .withAutoCollectAdvertId(false)
//                .withFactory(AmplitudeIntegrationFactory.FACTORY)
//                .withFactory(BrazeIntegrationFactory.FACTORY)
                .build()
        )

    }
}