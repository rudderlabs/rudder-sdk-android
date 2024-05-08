package com.rudderstack.android.internal.infrastructure

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.core.Analytics
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class AppInstallUpdateTrackerPluginTest {

    private lateinit var appInstallUpdateTrackerPlugin: AppInstallUpdateTrackerPlugin
    private val jsonAdapter: JsonAdapter = GsonAdapter()
    private val application: Application = ApplicationProvider.getApplicationContext()
    private lateinit var analytics: Analytics
    private lateinit var storage: AndroidStorage

    @Before
    fun setup() {
        appInstallUpdateTrackerPlugin = AppInstallUpdateTrackerPlugin()
    }

    @After
    fun destroy() {
        val storage = analytics.storage as AndroidStorageImpl
        storage.clearStorage()
        analytics.shutdown()
    }

    /**
     * Helper function to generate an instance of the analytics object with the given configuration.
     */
    private fun generateTestAnalytics(trackLifecycleEvents: Boolean = true): Analytics {
        storage = AndroidStorageImpl(
            ApplicationProvider.getApplicationContext(),
            false,
            writeKey = "test_writeKey",
            storageExecutor = TestExecutor(),
        )
        val mockConfig = ConfigurationAndroid(
            application, jsonAdapter,
            shouldVerifySdk = false,
            analyticsExecutor = TestExecutor(),
            trackLifecycleEvents = trackLifecycleEvents,
        )
        return generateTestAnalytics(mockConfig, storage = this.storage)
    }

    /**
     * Helper function to simulate app restart by clearing the storage and shutting down the analytics instance.
     */
    private fun simulateAppRestart() {
        analytics.storage.clearStorage()
        analytics.shutdown()
    }

    @Test
    fun `when lifecycle is enabled at the first app install, then Application Installed event should be made`() {
        analytics = generateTestAnalytics()
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.allOf(
                Matchers.iterableWithSize(1), Matchers.everyItem(
                    Matchers.allOf(
                        Matchers.isA(TrackMessage::class.java),
                        Matchers.hasProperty(
                            "eventName", Matchers.equalTo("Application Installed")
                        ),
                        Matchers.hasProperty(
                            "properties", Matchers.allOf(
                                Matchers.hasEntry("version", "1.0.0"),
                                Matchers.hasEntry("build", 1.0)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `when application is updated, then Application Updated should be made`() {
        analytics = generateTestAnalytics()
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics()
        setCurrentVersionNameAndCode("1.0.1", 2)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.allOf(
                Matchers.hasItems(
                    Matchers.allOf(
                        Matchers.isA(TrackMessage::class.java),
                        Matchers.hasProperty(
                            "eventName", Matchers.equalTo("Application Updated")
                        ),
                        Matchers.hasProperty(
                            "properties", Matchers.allOf(
                                Matchers.hasEntry("previous_version", "1.0.0"),
                                Matchers.hasEntry("previous_build", 1.0),
                                Matchers.hasEntry("version", "1.0.1"),
                                Matchers.hasEntry("build", 2.0)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `when lifecycle is disabled at the first app install, then Application Installed shouldn't be made`() {
        analytics = generateTestAnalytics(trackLifecycleEvents = false)
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.empty()
        )
    }

    @Test
    fun `given lifecycle was disabled at the first app install, when app is launched again with lifecycle enabled, then Application Installed shouldn't be made`() {
        analytics = generateTestAnalytics(trackLifecycleEvents = false)
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics()

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.empty()
        )
    }

    @Test
    fun `given lifecycle was enabled at the first app install, when app is launched again with lifecycle disabled, then Application Installed shouldn't be made`() {
        analytics = generateTestAnalytics()
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics(trackLifecycleEvents = false)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.empty()
        )
    }

    @Test
    fun `given lifecycle was disabled at the first app install, when app is launched again with lifecycle disabled, then Application Installed shouldn't be made`() {
        analytics = generateTestAnalytics(trackLifecycleEvents = false)
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics(trackLifecycleEvents = false)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.empty()
        )
    }

    @Test
    fun `when application is updated with lifecycle disabled, then Application Updated shouldn't be made`() {
        analytics = generateTestAnalytics()
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics(trackLifecycleEvents = false)
        setCurrentVersionNameAndCode("1.0.0", 2)
        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.empty()
        )
    }

    @Test
    fun `given application is already updated, when app is launched again with lifecycle disabled, then Application Updated shouldn't be made`() {
        analytics = generateTestAnalytics()
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics()
        setCurrentVersionNameAndCode("1.0.0", 2)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics(trackLifecycleEvents = false)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.empty()
        )
    }

    @Test
    fun `when version name is changed, then Application Update should not be made`() {
        analytics = generateTestAnalytics()
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.0", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics()
        setCurrentVersionNameAndCode("1.0.1", 1)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.empty()
        )
    }

    @Test
    fun `given first time application is updated with lifecycle tracking disabled, when app is updated again with lifecycle tracking enabled, then Application Updated should be made with correct properties`() {
        analytics = generateTestAnalytics()
        setDefaultVersionNameAndCode()
        setCurrentVersionNameAndCode("1.0.1", 1)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics(false)
        setCurrentVersionNameAndCode("1.0.2", 2)
        appInstallUpdateTrackerPlugin.setup(analytics)
        simulateAppRestart()
        analytics = generateTestAnalytics()
        setCurrentVersionNameAndCode("1.0.3", 3)
        appInstallUpdateTrackerPlugin.setup(analytics)

        appInstallUpdateTrackerPlugin.setup(analytics)

        val saved = analytics.storage.getDataSync()
        MatcherAssert.assertThat(
            saved, Matchers.allOf(
                Matchers.hasItems(
                    Matchers.allOf(
                        Matchers.isA(TrackMessage::class.java),
                        Matchers.hasProperty(
                            "eventName", Matchers.equalTo("Application Updated")
                        ),
                        Matchers.hasProperty(
                            "properties", Matchers.allOf(
                                Matchers.hasEntry("previous_version", "1.0.2"),
                                Matchers.hasEntry("previous_build", 2.0),
                                Matchers.hasEntry("version", "1.0.3"),
                                Matchers.hasEntry("build", 3.0)
                            )
                        )
                    )
                )
            )
        )
    }

    /**
     * Helper function to set the version name and build in the Robolectric Application object.
     */
    private fun setCurrentVersionNameAndCode(versionName: String, build: Long) {
        shadowOf(application.packageManager).getInternalMutablePackageInfo(application.packageName).versionName = versionName
        shadowOf(application.packageManager).getInternalMutablePackageInfo(application.packageName).longVersionCode = build
    }

    /**
     * Helper function to set the default version name and build in the storage object.
     */
    private fun setDefaultVersionNameAndCode() {
        val storage = analytics.storage as AndroidStorageImpl
        storage.setVersionName("")
        storage.setBuild(-1)
    }
}
