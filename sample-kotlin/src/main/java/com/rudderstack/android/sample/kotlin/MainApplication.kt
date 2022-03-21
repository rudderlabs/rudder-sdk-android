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

import com.rudderstack.android.sdk.core.RudderClient
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
                // Prompt the user to install/update/enable Google Play services.
                GoogleApiAvailability.getInstance()
                    .showErrorNotification(activity, e.connectionStatusCode)
                Log.e("Rudder", "Play install")
            } catch (e: GooglePlayServicesNotAvailableException) {
                // Indicates a non-recoverable error: let the user know.
                Log.e("SecurityException", "Google Play Services not available.");
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("Debug", "Application OnCreate")

        Thread {
            for (i in 1..10) {
                println("Event from Main Application {$i}")
//                rudderClient!!.track("Event from Main Application {$i}")
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