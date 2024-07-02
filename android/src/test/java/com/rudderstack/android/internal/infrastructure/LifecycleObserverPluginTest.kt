package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.android.utils.busyWait
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.models.ScreenMessage
import com.rudderstack.models.TrackMessage
import com.rudderstack.models.TrackProperties
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.assertArgument
import com.vagabond.testcommon.generateTestAnalytics
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestSuite
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.isA
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

abstract class LifecycleObserverPluginTest {
    private lateinit var analytics: Analytics
    private lateinit var mockStorage: AndroidStorage
    private lateinit var mockConfigurationAndroid: ConfigurationAndroid
    private lateinit var mockControlPlane : ConfigDownloadService
    abstract val jsonAdapter: JsonAdapter

    @Before
    fun setup() {
        mockConfigurationAndroid = mock<ConfigurationAndroid>()
        val listeners = mutableListOf<ConfigDownloadService.Listener>()
        mockControlPlane = mock()
        whenever(mockControlPlane.addListener(any<ConfigDownloadService.Listener>(), any<Int>())).then {
            listeners += it.getArgument<ConfigDownloadService.Listener>(0)
            Unit
        }
        whenever(mockControlPlane.download(any())).then {
            val cb = it.getArgument<(success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit>(0)
            listeners.forEach { it.onDownloaded(true) }
            cb(true, RudderServerConfig(), null)
        }
        whenever(mockConfigurationAndroid.jsonAdapter).thenReturn(jsonAdapter)
        whenever(mockConfigurationAndroid.trackLifecycleEvents).thenReturn(true)
        whenever(mockConfigurationAndroid.recordScreenViews).thenReturn(true)
        whenever(mockConfigurationAndroid.analyticsExecutor).thenReturn(TestExecutor())
        whenever(mockConfigurationAndroid.shouldVerifySdk).thenReturn(false)
        whenever(mockConfigurationAndroid.logger).thenReturn(KotlinLogger())
        whenever(mockConfigurationAndroid.copy()).thenReturn(mockConfigurationAndroid)
        mockStorage = mock<AndroidStorage>()
        whenever(mockStorage.versionName).thenReturn("1.0")
        analytics = generateTestAnalytics(mockConfigurationAndroid, storage = mockStorage, configDownloadService = mockControlPlane)
    }

    @After
    fun shutdown() {
        analytics.shutdown()
    }

    @Test
    fun `when app is backgrounded, then flush is called`() {
        val analytics = mockk<Analytics>(relaxed = true)
        val lifecycleObserverPlugin = LifecycleObserverPlugin()
        lifecycleObserverPlugin.setup(analytics)

        lifecycleObserverPlugin.onAppBackgrounded()

        verify { analytics.flush() }
        lifecycleObserverPlugin.onShutDown()
    }

    @Test
    fun `test when app foregrounded first time from_background should be false and contain version`() {
        val plugin = LifecycleObserverPlugin()
        plugin.setup(analytics)
        plugin.onAppForegrounded()
        busyWait(100)
        analytics.assertArgument { input, _ ->
            assertThat(
                input, allOf(
                    isA(TrackMessage::class.java),
                    hasProperty("eventName", `is`(EVENT_NAME_APPLICATION_OPENED)),
                    hasProperty(
                        "properties", allOf<TrackProperties>(
                            aMapWithSize(2),
                            hasEntry("from_background", false),
                            hasEntry("version", "1.0"),
                        )
                    )
                )
            )
        }
        plugin.onShutDown()

    }

    @Test
    fun `test when app foregrounded second time from_background should be true and not contain version`() {
        val plugin = LifecycleObserverPlugin()
        plugin.setup(analytics)
        plugin.onAppForegrounded()
        busyWait(100)
        plugin.onAppForegrounded()
        analytics.assertArgument { input, _ ->
            assertThat(
                input, allOf(
                    isA(TrackMessage::class.java),
                    hasProperty("eventName", `is`(EVENT_NAME_APPLICATION_OPENED)),
                    hasProperty(
                        "properties", allOf<TrackProperties>(
                            aMapWithSize(1),
                            hasEntry("from_background", true),
                            not(hasEntry("version", "1.0")),
                        )
                    )
                )
            )
        }
        plugin.onShutDown()
    }

    @Test
    fun `test when app backgrounded event name is Application Backgrounded`() {
        val plugin = LifecycleObserverPlugin()
        plugin.setup(analytics)
        plugin.onAppBackgrounded()
        busyWait(100)
        analytics.assertArgument { input, _ ->
            assertThat(
                input, allOf(
                    isA(TrackMessage::class.java),
                    hasProperty("eventName", `is`(EVENT_NAME_APPLICATION_STOPPED))
                )
            )
        }
        plugin.onShutDown()
    }
    @Test
    fun `test when elapsed time more than 90 minutes update source config is called`() {
        var timeNow = 90*60*1000L
        val getTime = {
            timeNow.also {
                timeNow += it // 90 minutes passed by
            }
        }
        val plugin = LifecycleObserverPlugin(getTime)
        plugin.setup(analytics)
        plugin.onAppForegrounded()
        whenever(mockConfigurationAndroid.shouldVerifySdk).thenReturn(true)
        plugin.onAppForegrounded() // after 90 minutes stimulated
        org.mockito.kotlin.verify(mockControlPlane).download(any())
        plugin.onShutDown()

    }

    @Test
    fun `given automatic screen event is enabled, when automatic screen event is made, then screen event containing default properties is sent`() {
        val plugin = LifecycleObserverPlugin()
        plugin.setup(analytics)

        plugin.onActivityStarted("MainActivity")

        analytics.assertArgument { input, _ ->
            assertThat(
                input, allOf(
                    isA(ScreenMessage::class.java),
                    hasProperty("eventName", `is`("MainActivity")),
                    hasProperty(
                        "properties", allOf<TrackProperties>(
                            aMapWithSize(2),
                            hasEntry("automatic", true),
                            hasEntry("name", "MainActivity"),
                        )
                    )
                )
            )
        }

        plugin.onShutDown()
    }
}

class GsonLifecycleObserverPluginTest : LifecycleObserverPluginTest() {
    override val jsonAdapter: JsonAdapter
        get() = GsonAdapter()

}

class JacksonLifecycleObserverPluginTest : LifecycleObserverPluginTest() {
    override val jsonAdapter: JsonAdapter
        get() = JacksonAdapter()

}
//not supported yet
/*class MoshiLifecycleObserverPluginTest : LifecycleObserverPluginTest() {
    override val jsonAdapter: JsonAdapter
        get() = MoshiAdapter()

}*/

@RunWith(Suite::class)
@Suite.SuiteClasses(
    GsonLifecycleObserverPluginTest::class,
    JacksonLifecycleObserverPluginTest::class,
//    MoshiLifecycleObserverPluginTest::class
)
class LifecycleObserverPluginTestSuite : TestSuite() {}

