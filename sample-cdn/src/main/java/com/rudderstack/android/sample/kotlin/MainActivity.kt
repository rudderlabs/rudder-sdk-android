package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderOption
import com.rudderstack.android.sdk.core.RudderTraits

class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            MainApplication.rudderClient!!.identify(
                "test_user_id",
                RudderTraits()
                    .putFirstName("Test First Name")
                    .putEmail("random2@gmail.com"),
                RudderOption()
                    .putExternalId("brazeExternalId", "434dfbfb-c5bf-46d0-a47e-1336fcea264c")
            )

            MainApplication.rudderClient!!.track("Test Event")
            MainApplication.rudderClient!!.track("Test Event 1")
            MainApplication.rudderClient!!.track("Test Event 2")
            MainApplication.rudderClient!!.track("Test Event 3")
            MainApplication.rudderClient!!.track("Test Event 4")
            MainApplication.rudderClient!!.track("Test Event 5")

        }, 10000)
    }

//    override fun onResume() {
//        super.onResume()
//        Adjust.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Adjust.onPause()
//    }
}
