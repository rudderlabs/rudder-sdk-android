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
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
        analytics = createAnalyticsInstance()
        `when`(androidStorage.v1VersionName).thenReturn("1.0")
        `when`(androidStorage.v1AdvertisingId).thenReturn("v1AdId")


        plugin = ReinstatePlugin()
        plugin.setup(analytics)
    }

    private fun createAnalyticsInstance() = generateTestAnalytics(
        configurationAndroid, storage = androidStorage, configDownloadService = mockControlPlane
    )

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
        Mockito.verify(androidStorage, times(1)).migrateV1StorageToV2(any())


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

    /** _analytics?.setUserIdFromV1()
    _analytics?.migrateAnonymousIdFromV1()
    _analytics?.migrateOptOutFromV1()
    _analytics?.migrateContextFromV1()
    _analytics?.migrateV1AdvertisingId()
    _analytics?.initializeSessionManagement(
    _analytics?.androidStorage?.v1SessionId,
    _analytics?.androidStorage?.v1LastActiveTimestamp
    )
    _analytics?.migrateV1Build()
    _analytics?.migrateV1Version()*/
    @Test
    fun `test anonymous id should be migrated if v1 data available and v2 unavailable`(){
        busyWait(100)
        Mockito.verify(androidStorage, times(1)).v1AnonymousId
        Mockito.verify(androidStorage, times(1)).resetV1AnonymousId()
    }
    @Test
    fun `test advertising id should be migrated if v1 data available and v2 unavailable`(){
        busyWait(100)
        Mockito.verify(androidStorage, times(1)).v1AdvertisingId
        Mockito.verify(androidStorage, times(1)).saveAdvertisingId(eq("v1AdId"))
        Mockito.verify(androidStorage, times(1)).resetV1AdvertisingId()
    }
    @Test
    fun `test session id should be migrated if v1 data available and v2 unavailable`(){
        busyWait(100)
        Mockito.verify(androidStorage, times(1)).v1SessionId
        Mockito.verify(androidStorage, times(1)).resetV1SessionId()
        Mockito.verify(androidStorage, times(1)).resetV1SessionLastActiveTimestamp()
    }
    @Test
    fun `test build should be migrated if v1 data available and v2 unavailable`(){
        busyWait(100)
        Mockito.verify(androidStorage, times(1)).v1Build
        Mockito.verify(androidStorage, times(1)).resetV1Build()
        Mockito.verify(androidStorage, times(1)).setBuild(any())
    }
    @Test
    fun `test version should be migrated if v1 data available and v2 unavailable`(){
        busyWait(100)

        Mockito.verify(androidStorage, times(1)).v1VersionName
        Mockito.verify(androidStorage, times(1)).setVersionName(eq("1.0"))
        Mockito.verify(androidStorage, times(1)).resetV1Version()
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
