package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderMessageBuilder
import com.rudderstack.android.sdk.core.RudderTraits
import com.rudderstack.android.sdk.core.ecomm.*


class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rudderClient = MainApplication.rudderClient
        rudderClient!!.identify(
            "test_user_id_1",
            RudderTraits()
                .putAddress(
                    RudderTraits.Address().putCity("kolkata").putCountry("india")
                        .putStreet("new alipore")
                )
                .putAge("24")
                .putName("Ruchira"),
            null
        )
        rudderClient.track("Tracking for test_user_id_1")
       //  rudderClient!!.reset();
        rudderClient!!.identify(
            "test_user_id",
            RudderTraits()
                .putAddress(
                    RudderTraits.Address().putCity("kolkata").putCountry("india")
                        .putStreet("new alipore")
                )
                .putAge("24")
                .putName("Ruchira"),
            null
        )

        rudderClient.track("Tracking for test_user_id")
        rudderClient.track("Tracking for test_user_id")
        rudderClient.track("Tracking for test_user_id")

        rudderClient!!.identify(
            "test_user_id_1",
            RudderTraits()
                .putAddress(
                    RudderTraits.Address().putCity("kolkata").putCountry("india")
                        .putStreet("new alipore")
                )
                .putAge("24")
                .putName("Ruchira"),
            null
        )
        rudderClient.track("Tracking for test_user_id_1")
    }
}
