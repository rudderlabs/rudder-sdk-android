package com.rudderstack.android.utilities

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.AnalyticsRegistry
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.RudderAnalytics.Companion.getInstance
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class AnalyticsUtilTest {

    @Before
    fun setUp() {
        AnalyticsRegistry.clear()
    }

    @Test
    fun `given writeKey and configuration are passed, when anonymousId id is set, then assert that configuration has this anonymousId set as a property`() {
        val analytics = getInstance(
            "testKey", ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                JacksonAdapter()
            )
        )

        analytics.setAnonymousId("anon_id")
        MatcherAssert.assertThat(
            analytics.currentConfigurationAndroid, allOf(
                Matchers.isA(ConfigurationAndroid::class.java),
                Matchers.hasProperty("anonymousId", Matchers.equalTo("anon_id"))
            )
        )
    }
}
