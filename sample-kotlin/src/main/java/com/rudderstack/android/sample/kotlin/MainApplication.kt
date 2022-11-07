package com.rudderstack.android.sample.kotlin

import android.app.ActivityManager
import android.app.Application

import android.content.Context
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.rudderstack.android.integrations.appcenter.AppcenterIntegrationFactory

import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext


class MainApplication : Application(), Configuration.Provider {
    companion object {
        var rudderClient: RudderClient? = null
        const val DATA_PLANE_URL = "https://54b4-2409-4070-4e9f-ded2-c901-b6e6-5b1a-38b2.ngrok.io"
        const val WRITE_KEY = "1pTxG1Tqxr7FCrqIy7j0p28AENV"

        fun getProcessName(application: Application): String? {
            val mypid = Process.myPid()
            val manager = application.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val infos = manager.runningAppProcesses
            for (info in infos) {
                if (info.pid == mypid) {
                    return info.processName
                }
            }
            return null
        }

        fun tlsBackport(activity: AppCompatActivity) {
            try {
                ProviderInstaller.installIfNeeded(activity)
                Log.e("Rudder", "Play present")
                val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, null, null)
                sslContext.createSSLEngine()
            } catch (e: GooglePlayServicesRepairableException) {
                GoogleApiAvailability.getInstance()
                    .showErrorNotification(activity, e.connectionStatusCode)
                Log.e("Rudder", "Play install")
            } catch (e: GooglePlayServicesNotAvailableException) {
                Log.e("SecurityException", "Google Play Services not available.");
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

//        val rudderConfig = RudderConfig.Builder()
//            .withDataPlaneUrl(MainApplication.DATA_PLANE_URL)
//            .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//            .withTrackLifecycleEvents(false)
//            .withFlushPeriodically(15, TimeUnit.MINUTES)
//            .withFlushQueueSize(90)
//            .withSleepCount(180)
//            .withRecordScreenViews(false)
//            .build()
//
//        rudderClient = RudderClient.getInstance(
//            this,
//            WRITE_KEY,
//            rudderConfig
//        )

//        RudderClient.putAnonymousId("anonymous_id_1")
//        RudderClient.putDeviceToken("DevToken2")

        Log.e("Debug", "Application OnCreate")
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(MainApplication.DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(true)
                .withFlushPeriodically(15, TimeUnit.MINUTES)
                .withFactory(AppcenterIntegrationFactory.FACTORY)
                .withRecordScreenViews(true)
                .build()
        )
        Log.e("Debug","Application OnCreate")

        Thread {
            for (i in 1..10) {
                println("Event from Main Application {$i}")
                rudderClient!!.track("Event from Main Application {$i}")
            }
        }.start()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    // To initialize WorkManager on demand instead of on startup
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}