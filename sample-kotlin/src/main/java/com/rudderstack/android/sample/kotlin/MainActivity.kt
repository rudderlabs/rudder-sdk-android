package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.rudderstack.android.integrations.appcenter.AppcenterIntegrationFactory
import com.rudderstack.android.sample.kotlin.MainApplication.Companion.tlsBackport
import com.rudderstack.android.sdk.core.RudderClient
import java.util.*
import javax.net.ssl.SSLContext
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity() {
    private var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            tlsBackport(this)

        navigate_to_first.setOnClickListener {
            startActivity(Intent(this, FirstActivity::class.java))
        }


        flush.setOnClickListener {
            MainApplication.rudderClient!!.flush();
        }
        track.setOnClickListener {
            MainApplication.rudderClient!!.track("Track 1")
            MainApplication.rudderClient!!.track("Track 2")
            for (i in 1..10) {
                MainApplication.rudderClient!!.track("Event on Button Click $i")
            }
        }
        MainApplication.rudderClient!!.onIntegrationReady(
            "Custom Factory",
            NativeCallBack("Custom Factory")
        );
    }

    private fun tlsBackport() {
        try {
            ProviderInstaller.installIfNeeded(this)
            Log.e("Rudder", "Play present")
            val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, null, null)
            sslContext.createSSLEngine()
        } catch (e: GooglePlayServicesRepairableException) {
            // Prompt the user to install/update/enable Google Play services.
            GoogleApiAvailability.getInstance()
                .showErrorNotification(this, e.connectionStatusCode)
            Log.e("Rudder", "Play install")
        } catch (e: GooglePlayServicesNotAvailableException) {
            // Indicates a non-recoverable error: let the user know.
            Log.e("SecurityException", "Google Play Services not available.");
            e.printStackTrace()
        }
    }

    fun onUserSession(view: View) {
        startActivity(Intent(this, UserSessionActivity::class.java))
    }
}

internal class NativeCallBack(private val integrationName: String) : RudderClient.Callback {
    override fun onReady(instance: Any) {
        println("Call back of integration : " + integrationName + " is called");
    }
}