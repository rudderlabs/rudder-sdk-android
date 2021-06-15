package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderOption
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits


class MainActivity : AppCompatActivity() {
    private var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed(Runnable {
//            MainApplication.rudderClient!!.track("Test Event 1")
//            MainApplication.rudderClient!!.track("Test Event 2")
//            MainApplication.rudderClient!!.track("Test Event 3")
//            MainApplication.rudderClient!!.track("Test Event 4")

            val option = RudderOption()
                    .putExternalId("brazeExternalId", "some_external_id_1")
                    .putExternalId("braze_id", "some_braze_id_2")
                    .putIntegration("GA", true).putIntegration("Amplitude", true)
                    .putCustomContext("customContext", mapOf("version" to "1.0.0", "language" to "kotlin"))
            MainApplication.rudderClient!!.identify(
                    "userId",
                    RudderTraits().putFirstName("Test First Name"),
                    option
            )
            val props = RudderProperty()
            props.put("Name", "Desu")
            props.put("city", "hyderabad")
            MainApplication.rudderClient!!.track("test event desu", props, option)

            MainApplication.rudderClient!!.track("Test Event")
        }, 2000)
    }
}
