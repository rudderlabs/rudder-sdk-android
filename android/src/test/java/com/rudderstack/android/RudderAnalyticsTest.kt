package com.rudderstack.android

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.RudderAnalytics.Companion.getInstance
import com.rudderstack.core.Analytics
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class RudderAnalyticsTest {
    val writeKey = "writeKey"

    @Test
    fun `when writeKey and configuration is passed, then getInstance should return Analytics instance`() {
        val analytics = getInstance(
            writeKey, ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                JacksonAdapter()
            )
        )

        MatcherAssert.assertThat(analytics, Matchers.isA(Analytics::class.java))
    }

    @Test
    fun `given that the SDK supports a singleton instance, when an attempt is made to create multiple instance with the different writeKey, then both instances should remain the same`() {
        val writeKey2 = "writeKey2"
        val analytics = getInstance(
            writeKey, ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                JacksonAdapter()
            )
        )

        val analytics2 = getInstance(
            writeKey2, ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                JacksonAdapter()
            )
        )

        MatcherAssert.assertThat(analytics, Matchers.isA(Analytics::class.java))
        MatcherAssert.assertThat(analytics2, Matchers.isA(Analytics::class.java))
        assert(analytics == analytics2)
    }

    @Test
    fun `given that the SDK supports a singleton instance, when an attempt is made to create multiple instance with the same writeKey, then both instances should remain the same`() {
        val analytics = getInstance(
            writeKey, ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                JacksonAdapter()
            )
        )

        val analytics2 = getInstance(
            writeKey, ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                JacksonAdapter()
            )
        )

        MatcherAssert.assertThat(analytics, Matchers.isA(Analytics::class.java))
        MatcherAssert.assertThat(analytics2, Matchers.isA(Analytics::class.java))
        assert(analytics == analytics2)
    }
}