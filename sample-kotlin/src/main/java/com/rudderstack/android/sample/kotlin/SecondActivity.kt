package com.rudderstack.android.sample.kotlin

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sample.kotlin.MainApplication.Companion.tlsBackport

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            tlsBackport(this)

        Thread {
            Log.e("Debug", "Second Activity onStart")
            for (i in 1..10) {
                println(
                    "Second Activity {$i} and process ${
                        MainApplication.getProcessName(
                            application
                        )
                    }"
                )
                MainApplication.rudderClient!!.track(
                    "Second Activity {$i} and process ${
                        MainApplication.getProcessName(
                            application
                        )
                    }"
                )
            }
        }.start()
    }
}