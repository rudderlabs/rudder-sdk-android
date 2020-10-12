package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderMessageBuilder
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.ecomm.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val rudderClient = MainApplication.rudderClient
//
//        val properties: MutableMap<String, Any> = mutableMapOf()
//        properties["test_key_1"] = "test_value_1"
//
//        val childProperties: MutableMap<String, String> = HashMap()
//        childProperties["test_child_key_1"] = "test_child_value_1"
//        properties["test_key_2"] = childProperties
//        properties["category"] = "test_category"
//
//        rudderClient!!.track("Sample Track Event", RudderProperty().putValue(properties))
//        rudderClient.identify("test_user_id")
    }
}
