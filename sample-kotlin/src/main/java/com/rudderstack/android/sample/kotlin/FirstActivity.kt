package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sample.kotlin.MainApplication.Companion.tlsBackport
import kotlinx.android.synthetic.main.activity_first.*

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
}