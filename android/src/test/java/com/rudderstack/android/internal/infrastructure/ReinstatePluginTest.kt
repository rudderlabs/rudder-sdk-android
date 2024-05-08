package com.rudderstack.android.internal.infrastructure

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.android.utils.busyWait
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.saveObject
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
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
    private val RUDDER_SERVER_FILE_NAME_V1 = "RudderServerConfig"
    private val TEST_SOURCE_ID = "testSourceId"
    private val context
        get() = ApplicationProvider.getApplicationContext<Application>()
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
                TEST_SOURCE_ID, isSourceEnabled = true,
            )
        )
        configurationAndroid = ConfigurationAndroid(
            context, mock<JsonAdapter>(), shouldVerifySdk = true,
            analyticsExecutor = TestExecutor()
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
        whenever(androidStorage.v1Traits).thenReturn(mapOf())
        whenever(androidStorage.v1ExternalIds).thenReturn(listOf())
        whenever(androidStorage.v1OptOut).thenReturn(false)

        saveObject(TEST_SOURCE_ID, context, RUDDER_SERVER_FILE_NAME_V1, KotlinLogger)
        plugin.updateRudderServerConfig(config)

//        plugin.updateConfiguration(configurationAndroid)
        busyWait(100)
        Mockito.verify(androidStorage, times(1)).migrateV1StorageToV2Sync()


    }
    @Test
    fun `test v1Traits should be called if v1 data available and v2 unavailable`(){
        whenever(androidStorage.anonymousId).thenReturn(null)
        whenever(androidStorage.userId).thenReturn(null)
        whenever(androidStorage.v1Traits).thenReturn(mapOf("userId" to "uid"))
        whenever(androidStorage.v1ExternalIds).thenReturn(listOf())
        whenever(androidStorage.v1OptOut).thenReturn(false)

        saveObject(TEST_SOURCE_ID, context, RUDDER_SERVER_FILE_NAME_V1, KotlinLogger)
        plugin.updateRudderServerConfig(config)

//        plugin.updateConfiguration(configurationAndroid)
        busyWait(100)
        Mockito.verify(androidStorage, times(2)).v1Traits
    }
    @Test
    fun `test v1OptOut should be called if v1 data available and v2 unavailable`(){
        whenever(androidStorage.anonymousId).thenReturn(null)
        whenever(androidStorage.userId).thenReturn(null)
        whenever(androidStorage.v1Traits).thenReturn(mapOf("userId" to "uid"))
        whenever(androidStorage.v1ExternalIds).thenReturn(listOf())
        whenever(androidStorage.v1OptOut).thenReturn(false)

        saveObject(TEST_SOURCE_ID, context, RUDDER_SERVER_FILE_NAME_V1, KotlinLogger)
        plugin.updateRudderServerConfig(config)

//        plugin.updateConfiguration(configurationAndroid)
        busyWait(100)
        Mockito.verify(androidStorage, times(1)).v1OptOut
    }
    @Test
    fun `test migration should not be called if v1 data unavailable`(){
        whenever(androidStorage.anonymousId).thenReturn(null)
        whenever(androidStorage.userId).thenReturn(null)
        whenever(androidStorage.v1Traits).thenReturn(mapOf())
        whenever(androidStorage.v1ExternalIds).thenReturn(listOf())
        whenever(androidStorage.v1OptOut).thenReturn(false)

        plugin.updateRudderServerConfig(config)

//        plugin.updateConfiguration(configurationAndroid)
        busyWait(100)
        Mockito.verify(androidStorage, never()).migrateV1StorageToV2Sync()
    }
    @Test
    fun `test migration should not be called if v2 data available`(){
        whenever(androidStorage.anonymousId).thenReturn("anonId")
        whenever(androidStorage.userId).thenReturn("userId")
        whenever(androidStorage.v1Traits).thenReturn(mapOf())
        whenever(androidStorage.v1ExternalIds).thenReturn(listOf())
        whenever(androidStorage.v1OptOut).thenReturn(false)
        saveObject(TEST_SOURCE_ID, context, RUDDER_SERVER_FILE_NAME_V1, KotlinLogger)
        plugin.updateRudderServerConfig(config)

//        plugin.updateConfiguration(configurationAndroid)
        busyWait(100)
        Mockito.verify(androidStorage, never()).migrateV1StorageToV2Sync()
    }
}
