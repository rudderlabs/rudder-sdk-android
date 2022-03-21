package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.integrations.appcenter.AppcenterIntegrationFactory
import com.rudderstack.android.sample.kotlin.MainApplication.Companion.tlsBackport
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

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
    }
}
