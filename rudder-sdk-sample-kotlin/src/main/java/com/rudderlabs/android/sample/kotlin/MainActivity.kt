package com.rudderlabs.android.sample.kotlin

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

        trackBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener

            rudderClient.track("some_test_event")
        }

        screenBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener

            rudderClient.screen(this.localClassName)
        }

        identifyBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener

            val traits: RudderTraits = RudderTraits()
                .putAddress(
                    RudderTraits.Address()
                        .putCity("some_city")
                        .putCountry("some_country")
                        .putPostalCode("123456")
                        .putState("some_state")
                        .putStreet("some_street")
                )
                .put("userId", userId.text.toString())
                .put("some_test_key", "some_test_value")

            rudderClient.identify(
                "some_user_id",
                traits,
                null
            )
        }

        resetBtn.setOnClickListener {
            if (rudderClient == null) return@setOnClickListener

            rudderClient.reset()
        }
    }

//    private fun sendEvents() {
//        MainApplication.rudderClient.track(
//            RudderMessageBuilder()
//                .setEventName("level_up")
//                .setProperty(
//                    TrackPropertyBuilder()
//                        .setCategory("test_category")
//                        .build()
//                )
//                .setUserId("test_user_id")
//        )
//
//        MainApplication.rudderClient.track(
//            RudderMessageBuilder()
//                .setEventName("daily_rewards_claim")
//                .setProperty(
//                    TrackPropertyBuilder()
//                        .setCategory("test_category")
//                        .build()
//                )
//                .setUserId("test_user_id")
//        )
//
//        MainApplication.rudderClient.track(
//            RudderMessageBuilder()
//                .setEventName("revenue")
//                .setProperty(
//                    TrackPropertyBuilder()
//                        .setCategory("test_category")
//                        .build()
//                )
//                .setUserId("test_user_id")
//        )
//
//
//    }
}
