package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderOption
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits
import java.lang.annotation.Native


class MainActivity : AppCompatActivity() {
    private var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         val option = RudderOption()
                    .putExternalId("brazeExternalId", "some_external_id_1")
                    .putExternalId("braze_id", "some_braze_id_2")
                    .putIntegration("GA", true).putIntegration("Amplitude", true)
                    .putCustomContext(
                        "customContext", mapOf(
                            "version" to "1.0.0",
                            "language" to "kotlin"
                        )
                    )
            MainApplication.rudderClient!!.identify(
                "userId",
                RudderTraits().putFirstName("Test First Name"),
                option
            )
            MainApplication.rudderClient!!.reset()
            val props = RudderProperty()
            props.put("Name", "John")
            props.put("city", "NYC")
            MainApplication.rudderClient!!.track("test event john", props, option)

            MainApplication.rudderClient!!.track("Test Event")

            MainApplication.rudderClient!!.onIntegrationReady("App Center", NativeCallBack("App Center"));

        MainApplication.rudderClient!!.onIntegrationReady("Custom Factory", NativeCallBack("Custom Factory"));


       
    }
}

internal class NativeCallBack(private val integrationName: String) : RudderClient.Callback {
    override fun onReady(instance: Any) {
        println("Call back of integration : "+integrationName +" is called");
    }
}