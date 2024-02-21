package com.rudderstack.android.internal.plugins

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.android.utils.TestExecutor
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
    private lateinit var chain: Plugin.Chain
    private lateinit var config: RudderServerConfig
    private lateinit var androidStorage: AndroidStorage
    private lateinit var mockControlPlane: ConfigDownloadService

    //    private lateinit var context: Context
    private lateinit var configurationAndroid: ConfigurationAndroid
    private lateinit var plugin: ReinstatePlugin
    private lateinit var dummyMessage: Message

    @Before
    fun setUp() {
        androidStorage = AndroidStorageImpl(
            ApplicationProvider.getApplicationContext(),
            instanceName = "testInstance",
            storageExecutor = TestExecutor()
        )
        mockControlPlane = mock<ConfigDownloadService>()
        whenever(mockControlPlane.download(any())).then {

            it.getArgument<(success: kotlin.Boolean, com.rudderstack.models.RudderServerConfig?, lastErrorMsg: kotlin.String?) -> kotlin.Unit>(
                0
            ).invoke(
                true, config, null
            )
        }
        dummyMessage = TrackMessage.create("dummyEvent", RudderUtils.timeStamp)
        chain = mock<Plugin.Chain>()
        config = RudderServerConfig(
            source = RudderServerConfig.RudderServerConfigSource(
                "testSourceId", isSourceEnabled = true
            )
        )
//        context = mock(Context::class.java)
        configurationAndroid = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(), mock<JsonAdapter>()
        )
        analytics = generateTestAnalytics(
            configurationAndroid, storage = androidStorage, configDownloadService = mockControlPlane
        )

        `when`(chain.originalMessage).thenReturn(dummyMessage)
        `when`(chain.message()).thenReturn(dummyMessage)
        `when`(chain.proceed(any<Message>())).thenReturn(dummyMessage)

        plugin = ReinstatePlugin()
        plugin.setup(analytics)
    }

    @After
    fun tearDown() {
        plugin.onShutDown()
        analytics.shutdown()
    }

    @Test
    fun `intercept should return original message when not reinstated`() {
        val interceptedMessage = plugin.intercept(chain)
        assertEquals(chain.originalMessage, interceptedMessage)
    }

    //    @Test
//    fun `post reinstate messages should not change when `
    @Test
    fun `intercept should process stacked messages when reinstated`() {
        plugin.intercept(chain)
        plugin.updateConfiguration(configurationAndroid)
        plugin.updateRudderServerConfig(config)

        verify(chain, times(1)).proceed(dummyMessage)
    }

    @Test
    fun `intercept should proceed if should verify sdk is false`() {
        val configCopy = configurationAndroid.copy(shouldVerifySdk = false)
        analytics.shutdown()
        analytics = generateTestAnalytics(configCopy, storage = androidStorage)
        plugin.updateConfiguration(configCopy)
        plugin.intercept(chain)
        verify(chain, times(1)).proceed(dummyMessage)
    }

    @Test
    fun `if v2 anonymous id is available there should be no call to v1UserId`() {
        plugin.onShutDown()
        analytics.shutdown()
        plugin = ReinstatePlugin()
        val storage = mock<AndroidStorage>()
        whenever(storage.anonymousId).thenReturn("testAnonymousId")
        analytics = generateTestAnalytics(
            configurationAndroid, storage = storage, configDownloadService = mockControlPlane
        )
        plugin.setup(analytics)
        plugin.updateConfiguration(configurationAndroid)
        plugin.updateRudderServerConfig(config)
        verify(storage, atLeast(1)).anonymousId
        verify(storage, never()).v1AnonymousId
        verify(storage, never()).v1Traits
    }

    @Test
    fun `if v2 user id  is available there should be no call to v1UserId`() {
        plugin.onShutDown()
        analytics.shutdown()
        plugin = ReinstatePlugin()
        val storage = mock<AndroidStorage>()
        whenever(storage.userId).thenReturn("userId")
        analytics = generateTestAnalytics(
            configurationAndroid, storage = storage, configDownloadService = mockControlPlane
        )
        plugin.setup(analytics)
        plugin.updateConfiguration(configurationAndroid)
        plugin.updateRudderServerConfig(config)
        verify(storage, atLeast(1)).anonymousId
        verify(storage, never()).v1AnonymousId
        verify(storage, never()).v1Traits
    }

    @Test
    fun `test ordering of messages pre and post server config update`() {
        val message1 = TrackMessage.create("message1", RudderUtils.timeStamp)
        val message2 = TrackMessage.create("message2", RudderUtils.timeStamp)
        val message3 = TrackMessage.create("message3", RudderUtils.timeStamp)
        val chain1 = mock<Plugin.Chain>()
        whenever(chain1.originalMessage).thenReturn(message1)
        whenever(chain1.message()).thenReturn(message1)
        whenever(chain1.proceed(any())).thenReturn(message1)
        val chain2 = mock<Plugin.Chain>()
        whenever(chain2.originalMessage).thenReturn(message2)
        whenever(chain2.message()).thenReturn(message2)
        whenever(chain2.proceed(any())).thenReturn(message2)
        val chain3 = mock<Plugin.Chain>()
        whenever(chain3.originalMessage).thenReturn(message3)
        whenever(chain3.message()).thenReturn(message3)
        whenever(chain3.proceed(any())).thenReturn(message3)

        val inOrder = inOrder(chain1, chain2, chain3)
        plugin.updateConfiguration(configurationAndroid)
        plugin.intercept(chain1)
        plugin.intercept(chain2)
        plugin.updateRudderServerConfig(config)
        plugin.intercept(chain3)
        val message1Captor = argumentCaptor<Message>()
        val message2Captor = argumentCaptor<Message>()
        val message3Captor = argumentCaptor<Message>()
        inOrder.verify(chain1).proceed(message1Captor.capture())
        inOrder.verify(chain2).proceed(message2Captor.capture())
        inOrder.verify(chain3).proceed(message3Captor.capture())

        MatcherAssert.assertThat(
            message1Captor.firstValue, hasProperty(
                "eventName", `is`("message1")
            )
        )
        MatcherAssert.assertThat(
            message2Captor.firstValue,
            hasProperty("eventName", `is`("message2"))
        )
        MatcherAssert.assertThat(
            message3Captor.firstValue,
            hasProperty("eventName", `is`("message3"))
        )
    }

    /**
    val message4 = TrackMessage.create("message4", RudderUtils.timeStamp)
    val message5 = TrackMessage.create("message5", RudderUtils.timeStamp)
    val message6 = TrackMessage.create("message6", RudderUtils.timeStamp)
    val message7 = TrackMessage.create("message7", RudderUtils.timeStamp)
    val message8 = TrackMessage.create("message8", RudderUtils.timeStamp)
    val message9 = TrackMessage.create("message9", RudderUtils.timeStamp)
    val message10 = TrackMessage.create("message10", RudderUtils.timeStamp)
    val message11 = TrackMessage.create("message11", RudderUtils.timeStamp)
    val message12 = TrackMessage.create("message12", RudderUtils.timeStamp)
    val message13 = TrackMessage.create("message13", RudderUtils.timeStamp)
    val message14 = TrackMessage.create("message14", RudderUtils.timeStamp)
    val message15 = TrackMessage.create("message15", RudderUtils.timeStamp)
    val message16 = TrackMessage.create("message16", RudderUtils.timeStamp)
    val message17 = TrackMessage.create("message17", RudderUtils.timeStamp)
    val message18 = TrackMessage.create("message18", RudderUtils.timeStamp)
    val message19 = TrackMessage.create("message19", RudderUtils.timeStamp)
    val message20 = TrackMessage.create("message20", RudderUtils.timeStamp)
    val message21 = TrackMessage.create("message21", RudderUtils.timeStamp)
    val message22 = TrackMessage.create("message22", RudderUtils.timeStamp)
    val message23 = TrackMessage.create("message23", RudderUtils.timeStamp)
    val message24 = TrackMessage.create("message24", RudderUtils.timeStamp)
    val message25 = TrackMessage.create("message25", RudderUtils.timeStamp)
    val message26 = TrackMessage.create("message26", RudderUtils.timeStamp)
    val message27 = TrackMessage.create("message27", RudderUtils.timeStamp)
     */
}