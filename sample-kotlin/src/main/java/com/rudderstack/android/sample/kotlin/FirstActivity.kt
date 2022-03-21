package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sample.kotlin.MainApplication.Companion.tlsBackport
import kotlinx.android.synthetic.main.activity_first.*
import javax.net.ssl.SSLContext

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            tlsBackport(this)

        navigate_to_second.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        Thread {
            Log.e("Debug", "FirstActivity onStart")
            for (i in 1..10) {
                println(
                    "First Activity {$i} and process ${
                        MainApplication.getProcessName(
                            application
                        )
                    }"
                )
                MainApplication.rudderClient!!.track(
                    "First Activity {$i} and process ${
                        MainApplication.getProcessName(
                            application
                        )
                    }"
                )
            }
        }.start()
    }

    override fun onStart() {
        super.onStart()

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

}