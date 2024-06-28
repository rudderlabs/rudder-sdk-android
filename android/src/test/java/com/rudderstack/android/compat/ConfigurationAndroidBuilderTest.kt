package com.rudderstack.android.compat

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.core.RudderLogger
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class ConfigurationAndroidBuilderTest {

    @Test
    fun `when logLevel DEBUG is passed, then assert that configuration has this logLevel set as a property`() {
        val configurationAndroidBuilder =
            ConfigurationAndroidBuilder(ApplicationProvider.getApplicationContext() as Application)
                .withDataPlaneUrl("https://rudderstack.com")
                .withLogLevel(RudderLogger.LogLevel.DEBUG)
                .build()

        assertEquals(configurationAndroidBuilder.rudderLogger.level, RudderLogger.LogLevel.DEBUG)
    }

    @Test
    fun `when no logLevel is passed, then assert that configuration has logLevel set to NONE`() {
        val configurationAndroidBuilder =
            ConfigurationAndroidBuilder(ApplicationProvider.getApplicationContext() as Application)
                .withDataPlaneUrl("https://rudderstack.com")
                .build()

        assertEquals(configurationAndroidBuilder.rudderLogger.level, RudderLogger.LogLevel.NONE)
    }
}
