package com.rudderstack.android_tv

import android.app.Application
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://shaggy-liger-28.loca.lt"
//        const val CONTROL_PLANE_URL = "https://572d-2405-201-8000-60e4-3d04-bf7d-a0ae-be41.ngrok.io"
        const val WRITE_KEY = "1pTxG1Tqxr7FCrqIy7j0p28AENV"
    }

    override fun onCreate() {
        super.onCreate()

        RudderClient.putAnonymousId("anonymous_id_1")
        RudderClient.putDeviceToken("DevToken2")

        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withControlPlaneUrl(CONTROL_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
//                .withCustomFactory(CustomFactory.FACTORY)
                .build()
        )
    }


}