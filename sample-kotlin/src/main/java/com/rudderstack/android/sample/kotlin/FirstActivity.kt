package com.rudderstack.android.sample.kotlin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

//        navigate_to_second.setOnClickListener {
//            startActivity(Intent(this, SecondActivity::class.java))
//        }
    }
}