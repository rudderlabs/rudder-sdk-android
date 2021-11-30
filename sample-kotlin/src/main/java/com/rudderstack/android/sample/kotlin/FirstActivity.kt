package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        navigate_to_second.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("Debug","FirstActivity onStart")
        Thread {
            for (i in 1..10000) {
                println("Event from First Activity {$i}")
                MainApplication.rudderClient!!.track("Event from First Activity {$i}")
            }
        }.start()
    }
}