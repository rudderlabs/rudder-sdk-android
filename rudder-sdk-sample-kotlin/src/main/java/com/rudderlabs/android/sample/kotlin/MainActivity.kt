package com.rudderlabs.android.sample.kotlin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sdk.core.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rudderClient = MainApplication.rudderClient
//        rudderClient?.let {
//            // track call without traits
//            it.track("some_test_event")
//            // screen call without traits
//            it.screen(this.localClassName)
//            val traits: RudderTraits = RudderTraits()
//                .putAddress(
//                    RudderTraits.Address()
//                        .putCity("some_city")
//                        .putCountry("some_country")
//                        .putPostalCode("123456")
//                        .putState("some_state")
//                        .putStreet("some_street")
//                )
//                .put("userId", userId.text.toString())
//                .put("some_test_key", "some_test_value")
//            // identify call
//            it.identify("some_user_id", traits, null)
//            // track call with traits
//            it.track("some_test_event_identified")
//        }

        navigate_to_first.setOnClickListener {
            startActivity(Intent(this, FirstActivity::class.java))
        }

        trackBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener
        }

        screenBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener
        }

        identifyBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener
        }

        resetBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener
            rudderClient.reset()
        }
    }
}
