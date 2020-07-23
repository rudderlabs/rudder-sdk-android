package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed(Runnable {
            MainApplication.rudderClient!!.track("Test Event 1")
            MainApplication.rudderClient!!.track("Test Event 2")
            MainApplication.rudderClient!!.track("Test Event 3")
            MainApplication.rudderClient!!.track("Test Event 4")
        }, 2000)
    }
}
