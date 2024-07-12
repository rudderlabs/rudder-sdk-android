package com.rudderstack.android.plugins

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.internal.plugins.ExtractStatePlugin
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Analytics
import com.rudderstack.core.Logger
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.models.AliasMessage
import com.rudderstack.core.models.IdentifyMessage
import com.rudderstack.core.models.TrackMessage
import com.rudderstack.core.models.traits
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class ExtractStatePluginTest {

    @Mock
    private lateinit var chain: Plugin.Chain
    private lateinit var plugin: ExtractStatePlugin
    private lateinit var analytics: Analytics

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this);
        analytics = generateTestAnalytics(
            ConfigurationAndroid(
                application = ApplicationProvider.getApplicationContext(),
                jsonAdapter = mock<JsonAdapter>(),
                anonymousId = "anonymousId",
                shouldVerifySdk = false,
                logLevel = Logger.LogLevel.DEBUG
            ), storage = mock<AndroidStorage>()
        )
        plugin = ExtractStatePlugin()
        plugin.setup(analytics)
    }

    @After
    fun destroy() {
        plugin.onShutDown()
        analytics.shutdown()
    }

    @Test
    fun `intercept should proceed if message is not IdentifyMessage or AliasMessage`() {
        val message = TrackMessage.create("ev", RudderUtils.timeStamp)
        `when`(chain.message()).thenReturn(message)
        plugin.intercept(chain)
        verify(chain).proceed(message)
    }

    @Test
    fun `intercept should proceed with modified message for IdentifyMessage with anonymousId`() {
        val identifyMessage = IdentifyMessage.create(
            traits = mapOf("anonymousId" to null, "userId" to "userId"),
            timestamp = RudderUtils.timeStamp
        )
        `when`(chain.message()).thenReturn(identifyMessage)

        plugin.intercept(chain)
        val messageCaptor = argumentCaptor<IdentifyMessage>()
        verify(chain).proceed(messageCaptor.capture())
        val capturedMessage = messageCaptor.lastValue
        MatcherAssert.assertThat(capturedMessage.context?.traits?.get("anonymousId"), Matchers.notNullValue())
    }

    @Test
    fun `intercept should update context with new userId for AliasMessage`() {
        val aliasMessage = AliasMessage.create(RudderUtils.timeStamp, userId = "newUserId")
        `when`(chain.message()).thenReturn(aliasMessage)
        plugin.intercept(chain)
        val messageCaptor = argumentCaptor<AliasMessage>()

        verify(chain).proceed(messageCaptor.capture())
        val capturedMessage = messageCaptor.lastValue
        assertEquals("newUserId", capturedMessage.userId)
    }

    /*   @Test
       fun `getUserId should return userId from context`() {
           `when`(message.context).thenReturn(context)
           `when`(context["user_id"]).thenReturn("userId")

           val userId = plugin.getUserId(message)
           assertEquals("userId", userId)
       }

       @Test
       fun `getUserId should return userId from context traits`() {
           `when`(message.context).thenReturn(context)
           `when`(context.traits).thenReturn(mapOf("user_id" to "userId"))

           val userId = plugin.getUserId(message)
           assertEquals("userId", userId)
       }

       @Test
       fun `getUserId should return userId from message`() {
           `when`(message.context).thenReturn(null)
           `when`(message.userId).thenReturn("userId")

           val userId = plugin.getUserId(message)
           assertEquals("userId", userId)
       }

       @Test
       fun `appendContext should merge context`() {
           val contextState = mock(ContextState::class.java)
           `when`(analytics.contextState).thenReturn(contextState)
           `when`(contextState.value).thenReturn(context)
           val newContext = mock(MessageContext::class.java)
           `when`(context optAddContext context).thenReturn(newContext)

           plugin.appendContext(context)

           verify(analytics).processNewContext(newContext)
       }

       @Test
       fun `replaceContext should replace context`() {
           plugin.replaceContext(context)

           verify(analytics).processNewContext(context)
       }

       @Test
       fun `updateNewAndPrevUserIdInContext should update context with new userId`() {
           val newContext = mock(MessageContext::class.java)
           `when`(context.updateWith(any())).thenReturn(newContext)

           val updatedContext = plugin.updateNewAndPrevUserIdInContext("newUserId", context)

           assertEquals(newContext, updatedContext)
       }
   */
    /* @Test
     fun `intercept should handle IdentifyMessage with same userId`() {
         val identifyMessage = mock(IdentifyMessage::class.java)
         `when`(identifyMessage.context).thenReturn(context)
         `when`(context.traits).thenReturn(mapOf("userId" to "userId"))
         `when`(analytics.currentConfigurationAndroid?.userId).thenReturn("userId")
         `when`(chain.message()).thenReturn(identifyMessage)
         `when`(context.updateWith(any())).thenReturn(context)

         plugin.intercept(chain)

         verify(chain).proceed(identifyMessage)
     }

     @Test
     fun `intercept should handle IdentifyMessage with different userId`() {
         val identifyMessage = mock(IdentifyMessage::class.java)
         `when`(identifyMessage.context).thenReturn(context)
         `when`(context.traits).thenReturn(mapOf("userId" to "newUserId"))
         `when`(analytics.currentConfigurationAndroid?.userId).thenReturn("oldUserId")
         `when`(chain.message()).thenReturn(identifyMessage)
         `when`(context.updateWith(any())).thenReturn(context)

         plugin.intercept(chain)

         verify(chain).proceed(messageCaptor.capture())
         val capturedMessage = messageCaptor.value as IdentifyMessage
         assertEquals("newUserId", capturedMessage.userId)
     }*/
}
