package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sample.kotlin.MainApplication.Companion.tlsBackport
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            tlsBackport(this)

        navigate_to_first.setOnClickListener {
            startActivity(Intent(this, FirstActivity::class.java))
        }

        Thread {
            Log.e("Debug", "MainActivity onStart")
            for (i in 1..10) {
                println("Main Activity {$i} and process ${MainApplication.getProcessName(application)}")
                MainApplication.rudderClient!!.track(
                    "Main Activity {$i} and process ${
                        MainApplication.getProcessName(
                            application
                        )
                    }"
                )
            }
        }.start()
    }
}
