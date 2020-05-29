package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderMessageBuilder
import com.rudderstack.android.sdk.core.RudderOption
import com.rudderstack.android.sdk.core.RudderTraits
import com.rudderstack.android.sdk.core.ecomm.*


class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rudderClient = MainApplication.rudderClient
       var input = mapOf("Ruchira" to "Moitra")
        rudderClient!!.identify(
            "test_user_id",
            RudderTraits()
                .putAddress(
                    RudderTraits.Address().putCity("kolkata").putCountry("india")
                        .putStreet("new alipore")
                )
                .putAge("24")
                .putName("Ruchira"),
            RudderOption().setIntegrationOptions("GA",input)
        )

        rudderClient.alias("test_new_id",  RudderOption().setIntegration("GA",false))


        
        rudderClient.group(
            "sample_group_id",
            RudderTraits().putAge("24")
                .putName("Test Group Name")
                .putPhone("1234567891"),
            RudderOption().putContext("S3",false)
        );
    }
}
