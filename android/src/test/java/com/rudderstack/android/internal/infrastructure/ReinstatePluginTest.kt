package com.rudderstack.android.internal.infrastructure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.android.utils.TestExecutor
import com.rudderstack.android.androidStorage
import com.rudderstack.android.internal.plugins.ReinstatePlugin
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class ReinstatePluginTest {
    private lateinit var analytics: Analytics
    private lateinit var config: RudderServerConfig
    private lateinit var androidStorage: AndroidStorage
    private lateinit var mockControlPlane: ConfigDownloadService

    private lateinit var configurationAndroid: ConfigurationAndroid
    private lateinit var plugin: ReinstatePlugin
    private lateinit var dummyMessage: Message

    @Before
    fun setUp() {
        androidStorage = mock<AndroidStorage>()
        mockControlPlane = mock<ConfigDownloadService>()
        whenever(mockControlPlane.download(any())).then {

            it.getArgument<(success: kotlin.Boolean, com.rudderstack.models.RudderServerConfig?, lastErrorMsg: kotlin.String?) -> kotlin.Unit>(
                0
            ).invoke(
                true, config, null
            )
        }
        dummyMessage = TrackMessage.create("dummyEvent", RudderUtils.timeStamp)
        config = RudderServerConfig(
            source = RudderServerConfig.RudderServerConfigSource(
                "testSourceId", isSourceEnabled = true
            )
        )
        configurationAndroid = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(), mock<JsonAdapter>()
        )
        analytics = generateTestAnalytics(
            configurationAndroid, storage = androidStorage, configDownloadService = mockControlPlane
        )


        plugin = ReinstatePlugin()
        plugin.setup(analytics)
    }

    @After
    fun tearDown() {
        plugin.shutdown()
        analytics.shutdown()
    }

    @Test
    fun `test migration should be called if v1 data available and v2 unavailable`(){
        whenever(androidStorage.anonymousId).thenReturn(null)
        whenever(androidStorage.userId).thenReturn(null)
    }
    fun `test v1userId should be called if v1 data available and v2 unavailable`(){

    }
    @Test
    fun `test v1AnonymousId should be called if v1 data available and v2 unavailable`(){

    }
    @Test
    fun `test v1OptOut should be called if v1 data available and v2 unavailable`(){

    }
    @Test
    fun `test migration should not be called if v1 data unavailable`(){

    }
    @Test
    fun `test migration should not be called if v2 data available`(){

    }
}
