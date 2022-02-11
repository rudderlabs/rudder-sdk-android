package com.rudderstack.android.sample.kotlin

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.rudderstack.android.sdk.core.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext


class MainActivity : AppCompatActivity() {
    private var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            tlsBackport()
    }

    override fun onStart() {
        super.onStart()

        lateinit var rudderClient : RudderClient;
        RudderClient.putAnonymousId("anonymous_id_1")
        RudderClient.putDeviceToken("DevToken2")

        println("Desu:Debugging: Inside the Application Code");
        initialize.setOnClickListener {
            val rudderConfig = RudderConfig.Builder()
                .withDataPlaneUrl(MainApplication.DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(false)
                .withFlushPeriodically(15, TimeUnit.MINUTES)
                .withFlushQueueSize(90)
                .withSleepCount(180)
                .withRecordScreenViews(false)
                .build()

            rudderClient = RudderClient.getInstance(
                this,
                MainApplication.WRITE_KEY,
                rudderConfig
            )
        }

        trackBtn.setOnClickListener {
            for (i in 1..89) {
                val eventName = "Event number Testing 10:30 ${i}"
                println(eventName);
                rudderClient!!.track(eventName)
            }
        }

        flush.setOnClickListener {
            rudderClient!!.track("Event Number 90")
            rudderClient!!.flush();
        }

        // 54 events needed 3 seconds of time, but the events weren't cleared sometimes
        // 20 events needed 2 seconds of time, but the events weren't cleared sometimes
        // SO lesser the number of events lesser the time needed to flush the batch
        // when the app was closed automatically, the amount of time we will be having is very low and chances of events being flushed is very low
//        Handler().postDelayed({
//            RudderClient.putAdvertisingId("some_idfa_changed")
//            MainApplication.rudderClient!!.track("second_event")
//        }, 3000)
//        val option = RudderOption()
//            .putExternalId("brazeExternalId", "some_external_id_1")
//            .putExternalId("braze_id", "some_braze_id_2")
//            .putIntegration("GA", true).putIntegration("Amplitude", true)
//            .putCustomContext(
//                "customContext", mapOf(
//                    "version" to "1.0.0",
//                    "language" to "kotlin"
//                )
//            )
//        MainApplication.rudderClient!!.identify(
//            "userId",
//            RudderTraits().putFirstName("Test First Name").putBirthday(Date()),
//            option
//        )

//        val properties = RudderProperty().putValue(mapOf("1" to "Geeks", "2" to "for" , "3" to "Geeks"))
//        println(properties)
//        MainApplication.rudderClient!!.track("Testing desu latch", properties)
//
////        MainApplication.rudderClient!!.reset()
//        val props = RudderProperty()
//        props.put("Name", "John")
//        props.put("city", "NYC")
//        MainApplication.rudderClient!!.track("test event john", props, option)
//
//        RudderClient.putDeviceToken("DEVTOKEN2")
//
//        MainApplication.rudderClient!!.track("Test Event")
//
//
//
//        MainApplication.rudderClient!!.onIntegrationReady(
//            "App Center",
//            NativeCallBack("App Center")
//        );
//
//        MainApplication.rudderClient!!.onIntegrationReady(
//            "Custom Factory",
//            NativeCallBack("Custom Factory")
//        );
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

internal class NativeCallBack(private val integrationName: String) : RudderClient.Callback {
    override fun onReady(instance: Any) {
        println("Call back of integration : " + integrationName + " is called");
    }
}