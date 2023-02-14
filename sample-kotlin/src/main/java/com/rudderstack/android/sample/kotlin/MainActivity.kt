package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderTraits



class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        MainApplication.rudderClient!!.identify("testUserId1", RudderTraits().putName("Test User"), null)
        MainApplication.rudderClient!!.track("Test Event")
        MainApplication.rudderClient!!.screen("Main Screen")
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