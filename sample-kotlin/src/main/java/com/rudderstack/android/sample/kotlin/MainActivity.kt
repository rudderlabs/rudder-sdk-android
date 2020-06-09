package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rudderClient = MainApplication.rudderClient
        val sqLiteHelper = MainApplication.sqLiteHelper

        Thread(Runnable {
            // simulate load
            for (index in 1..100) {
                sqLiteHelper!!.saveEvent("dummy_event_${index}")
                rudderClient!!.track("dummy_event_${index}")
                Thread.sleep(10)
            }
        }).start()

//        rudderClient!!.identify(
//            "test_user_id",
//            RudderTraits()
//                .putAddress(
//                    RudderTraits.Address().putCity("kolkata").putCountry("india")
//                        .putStreet("new alipore")
//                )
//                .putAge("24")
//                .putName("Ruchira"),
//            null
//        )
//
//        rudderClient.alias("test_new_id")
//
//        rudderClient.group(
//            "sample_group_id",
//            RudderTraits().putAge("24")
//                .putName("Test Group Name")
//                .putPhone("1234567891")
//        );
    }
}
