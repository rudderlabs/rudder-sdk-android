package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sdk.core.RudderMessageBuilder
import com.rudderlabs.android.sdk.core.RudderTraits
import com.rudderlabs.android.sdk.core.TrackPropertyBuilder

class MainActivity : AppCompatActivity() {
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        btn.setOnClickListener {
//            count += 1
//            textView.text = "Count: $count"
//        }
//
//        rst.setOnClickListener {
//            count = 0
//            textView.text = "Count: "
//        }

        Handler().postDelayed(this::sendEvents, 2000)
    }

    private fun sendEvents() {
        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("level_up")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )

        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("daily_rewards_claim")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )

        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("revenue")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )

        val traits: RudderTraits = RudderTraits().putAddress(
            RudderTraits.Address()
                .putCity("some_city")
                .putCountry("some_country")
                .putPostalCode("123456")
                .putState("some_state")
                .putStreet("some_street")
        ).put("userId", "some_user_id")
        MainApplication.rudderClient.identify(
            "some_user_id",
            traits,
            null
        )

        val address = RudderTraits.Address()
        address.putCity("east greenwich")
        address.putState("California")
        address.putCountry("USA")
        address.putStreet("19123 forest lane")

        val rudderTraits = RudderTraits()
        rudderTraits.putAddress(address)

        rudderTraits.putEmail("peter.gibbons@initech.com")
        MainApplication.rudderClient.identify(rudderTraits)
    }
}
