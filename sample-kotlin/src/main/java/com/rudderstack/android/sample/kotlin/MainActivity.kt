package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.rudderstack.android.sdk.core.*
import java.lang.reflect.Field
import java.util.Date
import javax.net.ssl.SSLContext


class MainActivity : AppCompatActivity() {
    private var userCount = 1
    private var eventCount = 1
    private var screenCount = 1
    private var groupCount = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            tlsBackport()

        findViewById<Button>(R.id.identify).setOnClickListener {
            MainApplication.rudderClient!!.identify(
                "user$userCount",
                RudderTraits().putEmail("user$userCount@gmail.com").putName("Mr. User$userCount"),
                null
            )
            userCount++;
        }

        findViewById<Button>(R.id.track).setOnClickListener {
            val props = RudderProperty()
            props.put("Test Track Key $eventCount", "Test Track Value $eventCount")
            MainApplication.rudderClient!!.track(
                "Test Event $eventCount",
                props,
                null
            )
            eventCount++;
        }

        findViewById<Button>(R.id.screen).setOnClickListener {
            val props = RudderProperty()
            props.put("Test Screen Key $screenCount", "Test Screen Value $screenCount")
            MainApplication.rudderClient!!.screen(
                "Test Screen $screenCount",
                props,
                null
            )
            screenCount++;
        }

        findViewById<Button>(R.id.group).setOnClickListener {
            MainApplication.rudderClient!!.group(
                "Group $groupCount",
                RudderTraits().put("group id $groupCount", "group value $groupCount")
            )
            groupCount++;
        }

        findViewById<Button>(R.id.alias).setOnClickListener {
            MainApplication.rudderClient!!.alias("new user $userCount");
        }

        findViewById<Button>(R.id.error).setOnClickListener {
            val rudderReporterField = MainApplication.rudderClient?.javaClass?.getDeclaredField("rudderReporter")
            rudderReporterField?.isAccessible = true
            val rudderReporter = rudderReporterField?.get(null)
            val errorMethod = rudderReporter?.javaClass?.getDeclaredMethod("getErrorClient")
            val errorClient = errorMethod?.invoke(rudderReporter)
            val notifyErrorMethod = errorClient?.javaClass?.getDeclaredMethod("notify", Throwable::class.java)
            val error = Exception("Test Error-" + Date())
            notifyErrorMethod?.invoke(errorClient, error)
        }
        findViewById<Button>(R.id.crash).setOnClickListener {
            throw RuntimeException("Test Crash")
        }

        findViewById<Button>(R.id.resetBtn).setOnClickListener {
            MainApplication.rudderClient!!.reset()
        }

        findViewById<Button>(R.id.nextPage).setOnClickListener {
            startActivity(Intent(this, FirstActivity::class.java))
        }

        findViewById<Button>(R.id.userSessionPage).setOnClickListener {
            startActivity(Intent(this, UserSessionActivity::class.java))
        }

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

    fun onFlush(view: View) {
        RudderClient.getInstance()!!.flush()
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