package com.rudderstack.android.sample.kotlin

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.rudderstack.android.integrations.appcenter.AppcenterIntegrationFactory


import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import com.rudderstack.android.sdk.core.RudderOption
import java.util.concurrent.TimeUnit

class MainApplication : Application() {
    companion object {
        var rudderClient: RudderClient? = null
        const val TAG = "MainApplication"
        const val DATA_PLANE_URL = "https://rudderstachvf.dataplane.rudderstack.com"
        const val WRITE_KEY = "1pTxG1Tqxr7FCrqIy7j0p28AENV"
    }

    override fun onCreate() {
        super.onCreate()
        // TestCase #1
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withAutoSessionTracking(true)
//                .withTrackLifecycleEvents(true)
//                .withRecordScreenViews(true)
//                .build()
//        )

        // TestCase #2
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withTrackLifecycleEvents(true)
//                .withAutoSessionTracking(false)
//                .withRecordScreenViews(true)
//                .build()
//        )

        // TestCase #3,4
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withTrackLifecycleEvents(true)
//                .withAutoSessionTracking(true)
//                .withRecordScreenViews(true)
//                .withSessionTimeoutMillis(60000)
//                .build()
//        )

//        TestCase #5
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withTrackLifecycleEvents(true)
//                .withAutoSessionTracking(true)
//                .withRecordScreenViews(false)
//                .withSessionTimeoutMillis(60000)
//                .build()
//        )
//
//        rudderClient!!.identify("testUserId1", null, null)
//        TimeUnit.SECONDS.sleep(1L)
//        rudderClient!!.reset()
//        rudderClient!!.identify("testUserId2", null, null)

        //  TestCase #6
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withTrackLifecycleEvents(true)
//                .withAutoSessionTracking(false)
//                .withRecordScreenViews(false)
//                .withSessionTimeoutMillis(60000)
//                .build()
//        )
//        RudderLogger.logDebug("RudderSDK: Starting the manual session on ${MainActivity.count+1} App Open")
//        rudderClient!!.startSession();

        //  TestCase #7,8
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withTrackLifecycleEvents(true)
//                .withAutoSessionTracking(true)
//                .withRecordScreenViews(false)
//                .withSessionTimeoutMillis(60000)
//                .build()
//        )

//        TestCase #9
//             rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withTrackLifecycleEvents(true)
//                .withAutoSessionTracking(false)
//                .withRecordScreenViews(false)
//                .withSessionTimeoutMillis(60000)
//                .build()
//        )
//        rudderClient!!.startSession()
//        rudderClient!!.identify("testUserId1", null, null)
//        TimeUnit.SECONDS.sleep(1L)
//        rudderClient!!.reset()
//        rudderClient!!.identify("testUserId2", null, null)

//        TestCase #20, 21, 22, 23, 24, 25
//             rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .withFactory(AppcenterIntegrationFactory.FACTORY)
//                .build()
//        )
//        rudderClient!!.track("    Test Event 1");
//        rudderClient!!.track("   Test Event 2     ");
//        rudderClient!!.track("Test Event 3     ");
//        rudderClient!!.track("Test Event 4");
//        rudderClient!!.track("Test Event 5");
//        rudderClient!!.track("Test Event 6");
//
//          only for #24
//        rudderClient!!.identify("testUserId1");
//        rudderClient!!.screen("Event Filtering Screen")

//        TestCase #26, 27, 28
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .build()
//        )
//        rudderClient!!.track("Test Event before Opting out");
//        rudderClient!!.screen("Test Screen before Opting out");
//        RudderLogger.logDebug("User opting out from tracking his behaviour");
//        rudderClient!!.optOut(true);
//        rudderClient!!.track("Test Event after Opting out");
//        rudderClient!!.screen("Test Screen After Opting out");

        //        TestCase #29
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .build()
//        )
//        rudderClient!!.track("Test Event before Opting out");
//        rudderClient!!.screen("Test Screen before Opting out");
//        RudderLogger.logDebug("User opting out from tracking his behaviour");
//        rudderClient!!.identify("testUserId1");
//        rudderClient!!.optOut(true);
//        rudderClient!!.identify("testUserId2");
//        rudderClient!!.alias("userId2");
//        rudderClient!!.track("Test Event after Opting out");
//        rudderClient!!.screen("Test Screen After Opting out");
//        rudderClient!!.optOut(false);
//        rudderClient!!.track("User Opted Back IN");

//        TestCase #30, 31
//        RudderClient.putAdvertisingId("advertId${MainActivity.count}");
//        RudderClient.putDeviceToken("deviceToken${MainActivity.count}");
//        RudderClient.putAnonymousId("anonId${MainActivity.count}")
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withTrackLifecycleEvents(true)
//                .withRecordScreenViews(true)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .build()
//        )
//        rudderClient!!.optOut(true);

//        TestCase #32, 33, 34
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
//                .withCustomFactory(CustomFactory.FACTORY)
//                .build()
//        )
//        rudderClient!!.identify("testUserId1")
//        rudderClient!!.track("testEvent1")
//        rudderClient!!.screen("testScreen")
//        rudderClient!!.alias("userId1")
//        Handler().postDelayed({
//            rudderClient!!.reset()
//        }, 30000)
//
//        rudderClient!!.identify("userId2");

//        TestCase #35, #36
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
//                .withAutoCollectAdvertId(true)
//                .withCustomFactory(CustomFactory.FACTORY)
//                .build()
//        )
//        rudderClient!!.track("Test Event 1");
//        Handler().postDelayed({
//            rudderClient!!.track("Auto retrieved Advert Id")
//        }, 60000)

//        TestCase #37-43
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder().withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .withTrackLifecycleEvents(false)
                .withRecordScreenViews(false)
                .withFlushQueueSize(3)
                .build()
        )
        // only for #37, #38, #39, #40, #43
//        for (i in 1..30) {
//            rudderClient!!.track("Test Event ${i}")
//        }

        // for #41
//        for (i in 1..28) {
//            rudderClient!!.track("Test Event ${i}")
//        }
        // for #42
        for (i in 1..32) {
            rudderClient!!.track("Test Event ${i}")
        }


        Handler().postDelayed({
            rudderClient!!.flush();
        }, 10000)

//        TestCase 49, 50, 51
//        var option = RudderOption().putIntegration("All", false).putIntegration("App Center", true)
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            RudderConfig.Builder().withDataPlaneUrl(DATA_PLANE_URL)
//                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG).withAutoCollectAdvertId(true)
//                .withCustomFactory(CustomFactory.FACTORY).build(), option
//        )
//        // #49
//        rudderClient!!.track("Global level options")
//        option = RudderOption().putIntegration("App Center", false)
//        // #50
//        rudderClient!!.track("Event level options", null,  option)
//        // #51
//        option = RudderOption().putExternalId("sampleExternalIdType", "sampleExternalId")
//        rudderClient!!.identify("testUserId1", null, option);

    }
}