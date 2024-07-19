package com.rudderstack.android.plugins

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.internal.plugins.FillDefaultsPlugin
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.core.Analytics
import com.rudderstack.core.Logger
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.core.models.TrackMessage
import com.rudderstack.core.models.createContext
import com.rudderstack.core.models.customContexts
import com.rudderstack.core.models.externalIds
import com.rudderstack.core.models.traits
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.assertArgument
import com.vagabond.testcommon.generateTestAnalytics
import com.vagabond.testcommon.testPlugin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class FillDefaultsPluginTest {

    private lateinit var analytics: Analytics
    lateinit var mockConfig: ConfigurationAndroid
    private val fillDefaultsPlugin = FillDefaultsPlugin()
    private val jsonAdapter: JsonAdapter = GsonAdapter()
    private lateinit var storage: AndroidStorage

    @Before
    fun setup() {
        storage = AndroidStorageImpl(
            getApplicationContext(),
            false,
            writeKey = "test_writeKey",
            storageExecutor = TestExecutor(),
        )
        mockConfig = ConfigurationAndroid(
            application = getApplicationContext(),
            jsonAdapter = jsonAdapter,
            anonymousId = "anon_id",
            shouldVerifySdk = false,
            analyticsExecutor = TestExecutor(),
            logLevel = Logger.LogLevel.DEBUG,
        )
        analytics = generateTestAnalytics(mockConfig, storage = storage)
        analytics.associateState(ContextState())
        fillDefaultsPlugin.setup(analytics)
        fillDefaultsPlugin.updateConfiguration(mockConfig)
    }

    @After
    fun destroy() {
        analytics.storage.clearStorage()
        analytics.shutdown()
    }

    /**
     * We intend to test if data is filled in properly
     *
     */
    @Test
    fun `test insertion of defaults`() {
        analytics.retrieveState<ContextState>()?.update(
            createContext(
                traits = mapOf(
                    "name" to "some_name", "age" to 24
                ), externalIds = listOf(
                    mapOf("braze_id" to "b_id"),
                    mapOf("amp_id" to "a_id"),
                ), customContextMap = mapOf(
                    "custom_name" to "c_name"
                )
            )
        )
        val message = TrackMessage.create(
            "ev-1", RudderUtils.timeStamp, traits = mapOf(
                "age" to 31, "office" to "Rudderstack"
            ), externalIds = listOf(
                mapOf("some_id" to "s_id"),
                mapOf("amp_id" to "amp_id"),
            ), customContextMap = null
        )

        analytics.testPlugin(fillDefaultsPlugin)
        analytics.track(message)
        analytics.assertArgument { input, output ->
            //check for expected values
            assertThat(output?.anonymousId, allOf(notNullValue(), `is`("anon_id")))
            //message context to override
            assertThat(
                output?.context?.traits, allOf(
                    notNullValue(),
                    aMapWithSize(2),
                    hasEntry("age", 31.0),
                    hasEntry("office", "Rudderstack"),
                )
            )
            assertThat(
                output?.context?.customContexts, allOf(
                    notNullValue(),
                    aMapWithSize(1),
                    hasEntry("custom_name", "c_name"),
                )
            )
            // track messages shouldn't contain external ids sent inside it.
            // but it should have the context values
            assertThat(
                output?.context?.externalIds,
                containsInAnyOrder(
                    mapOf("braze_id" to "b_id"),
                    mapOf("amp_id" to "a_id")
                )

            )
        }
    }
}
