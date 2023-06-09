package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        findViewById<Button>(R.id.navigate_to_second).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }
}