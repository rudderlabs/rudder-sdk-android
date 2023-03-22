package com.rudderstack.android.sample.kotlin

import android.app.Application

import androidx.work.Configuration

import com.rudderstack.android.integration.braze.BrazeIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import com.rudderstack.android.integrations.amplitude.AmplitudeIntegrationFactory

class MainApplication : Application(), Configuration.Provider {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://rudderstachvf.dataplane.rudderstack.com"
        const val WRITE_KEY = "1pTxG1Tqxr7FCrqIy7j0p28AENV"
    }

    override fun onCreate() {
        super.onCreate()

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
               .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withFactory(BrazeIntegrationFactory.FACTORY)
                .withFactory(AmplitudeIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(false)
                .build()
        )

    }


    // To initialize WorkManager on demand instead of on startup
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}