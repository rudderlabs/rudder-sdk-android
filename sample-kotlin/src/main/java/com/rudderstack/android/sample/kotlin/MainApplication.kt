package com.rudderstack.android.sample.kotlin

import android.app.Application
import androidx.work.Configuration
import com.rudderstack.android.integration.braze.BrazeIntegrationFactory
import com.rudderstack.android.integrations.amplitude.AmplitudeIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import java.util.concurrent.TimeUnit

class MainApplication : Application(), Configuration.Provider {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = BuildConfig.DATA_PLANE_URL
        const val WRITE_KEY = BuildConfig.WRITE_KEY
    }

    override fun onCreate() {
        super.onCreate()

        RudderClient.putAuthToken("testAuthToken");
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withControlPlaneUrl(BuildConfig.CONTROL_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.NONE)
                .withFlushPeriodically(20,TimeUnit.MINUTES)
                .withCollectDeviceId(false)
                .withFactory(BrazeIntegrationFactory.FACTORY)
                .withFactory(AmplitudeIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(true)
                .withNewLifecycleEvents(true)
                .withRecordScreenViews(false)
                .withDbEncryption(RudderConfig.DBEncryption(false, "xyz"))
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