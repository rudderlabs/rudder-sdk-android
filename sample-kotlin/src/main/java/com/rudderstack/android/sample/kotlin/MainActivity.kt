package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
<<<<<<< HEAD
import com.rudderstack.android.sdk.core.RudderMessageBuilder
import com.rudderstack.android.sdk.core.RudderOption
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits
import com.rudderstack.android.sdk.core.ecomm.*
=======
>>>>>>> origin/master


class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rudderClient = MainApplication.rudderClient
        var input = mapOf("props" to "value")

        rudderClient!!.track("Example Track Event");
        rudderClient!!.track("Example Track Message");
        rudderClient!!.track(
            "Example Track Event 2",
            RudderProperty().putValue("prop_1", "Example track property 1")
        );
        rudderClient!!.track(
            "Example Track Event 3",
            RudderProperty().putValue("prop_2", "Example track property 2"),
            RudderOption().setIntegration("All", true).setIntegration("Bugsnag", false)
        )
        rudderClient!!.track(
            "Example Track Event 4",
            RudderProperty().putValue("prop_3", "Example track property 3"),
            RudderOption().setIntegration("Adjust", true)
        )
        rudderClient!!.track(
            "Example Track Event 5",
            RudderProperty().putValue("prop_3", "Example track property 4"),
            RudderOption().setIntegration("All", true).setIntegrationOptions(
                "Bugnsag",
                input
            ).putContext("Bugsnag", "On")
        )

        rudderClient!!.identify(
            "test_user_id"
        );

        rudderClient.alias(
            "test_new_id", RudderOption().setIntegration("abcxyz", true)
                .setIntegration("All", true)
        );


        rudderClient.group(
            "sample_group_id",
            RudderTraits().putAge("24")
                .putName("Test Group Name")
                .putPhone("1234567891"),
            RudderOption().setIntegration("Bugsnag", true)
                .setIntegration("All", true)
                .putContext("Adjust1", true)

        )

    }
}
