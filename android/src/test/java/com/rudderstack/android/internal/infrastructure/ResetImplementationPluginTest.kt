package com.rudderstack.android.internal.infrastructure

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.android.contextState
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.holder.associateState
import com.rudderstack.models.createContext
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasEntry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(
    RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class ResetImplementationPluginTest{

    private lateinit var analytics: Analytics
    @Before
    fun setup(){
        analytics = generateTestAnalytics(Configuration(jsonAdapter = mock (),),
            storage = AndroidStorageImpl(ApplicationProvider.getApplicationContext(),
                writeKey = "test_writeKey",
                storageExecutor = TestExecutor()
            ))
        analytics.associateState(ContextState())
    }
    @After
    fun tearDown(){
        analytics.shutdown()
    }
    @Test
    fun testResetImplementationPlugin(){
        val resetImplementationPlugin = ResetImplementationPlugin()
        resetImplementationPlugin.setup(analytics)
        //given
        analytics.contextState?.update(createContext(
            traits = mapOf("name" to "Debanjan", "email" to "debanjan@rudderstack.com"),
            externalIds = listOf(mapOf("id1" to "v1"), mapOf("id2" to "v2")),
            customContextMap = mapOf("customContext1" to mapOf("key1" to "value1", "key2" to "value2"))
        ))
        //when
        resetImplementationPlugin.reset()
        //then
        assertThat(analytics.contextState?.value, allOf(
            hasEntry("traits", emptyMap<String, Any>()),
            hasEntry("externalId", emptyList<Map<String, String>>()),
            hasEntry("customContextMap",
                mapOf("customContext1" to mapOf("key1" to "value1", "key2" to "value2"))),
            ))
        analytics.shutdown()
    }
}
