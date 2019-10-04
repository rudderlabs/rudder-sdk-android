package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sdk.core.RudderElementBuilder
import com.rudderlabs.android.sdk.core.TrackPropertyBuilder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener {
            MainApplication.rudderClient.track(
                RudderElementBuilder()
                    .setEventName("level_up")
                    .setProperty(
                        TrackPropertyBuilder()
                            .setCategory("test_category")
                            .build()
                    )
                    .setUserId("test_user_id")
            )

            MainApplication.rudderClient.track(
                RudderElementBuilder()
                    .setEventName("daily_rewards_claim")
                    .setProperty(
                        TrackPropertyBuilder()
                            .setCategory("test_category")
                            .build()
                    )
                    .setUserId("test_user_id")
            )

            MainApplication.rudderClient.track(
                RudderElementBuilder()
                    .setEventName("revenue")
                    .setProperty(
                        TrackPropertyBuilder()
                            .setCategory("test_category")
                            .build()
                    )
                    .setUserId("test_user_id")
            )
            count += 1
            textView.text = "Count: $count"
        }

        rst.setOnClickListener {
            count = 0
            textView.text = "Count: "
        }
    }
}
