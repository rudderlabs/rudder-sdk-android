package com.rudderstack.android.internal.plugins

import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.utilities.defaultLastActiveTimestamp
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.removeState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.models.Message
import com.rudderstack.models.TrackMessage
import com.rudderstack.models.android.UserSession
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SessionPluginTest {
    //    protected abstract val jsonAdapter: JsonAdapter
//    private lateinit var analytics: Analytics
    private lateinit var sessionPlugin: SessionPlugin
    private lateinit var analytics: Analytics

    @Before
    fun setUp() {
        analytics = generateTestAnalytics(mock<JsonAdapter>())
        sessionPlugin = SessionPlugin()
        sessionPlugin.setup(analytics)
        analytics.associateState(UserSessionState())
    }
    @After
    fun tearDown() {
        sessionPlugin.onShutDown()
        analytics.removeState<UserSessionState>()
        analytics.shutdown()
    }
    private val userSessionState
        get() = analytics.retrieveState<UserSessionState>()
    @Test
    fun `test intercept with valid session and null context sessionStart true`() {
        val timestamp = RudderUtils.timeStamp
        val message = TrackMessage.create("testEvent", timestamp)
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        val sessionTimestamp = defaultLastActiveTimestamp
        val sessionId = 1234567890L
        userSessionState?.update(
            UserSession(
                sessionId = sessionId,
                sessionStart = true,
                isActive = true,
                lastActiveTimestamp = sessionTimestamp
            )
        )
        sessionPlugin.intercept(mockChain)

        val updatedMessageCapture = argumentCaptor<Message>()
        verify(mockChain).proceed(updatedMessageCapture.capture())

        val updatedMessage = updatedMessageCapture.firstValue
        MatcherAssert.assertThat(
            updatedMessage, allOf(
                notNullValue(), hasProperty("timestamp", notNullValue()), hasProperty(
                    "context", allOf(
                        notNullValue(), Matchers.aMapWithSize<String, Any?>(5),
                        hasEntry("sessionId", sessionId.toString()),
                        hasEntry("sessionStart", true),
                    )
                )
            )
        )
        MatcherAssert.assertThat(
            userSessionState?.value, allOf(
                notNullValue(), hasProperty(
                    "sessionStart", `is`(false)
                ), hasProperty("sessionId", `is`(sessionId))
            )
        )
    }

    @Test
    fun `test intercept with valid session and null context sessionStart false`() {
        val timestamp = RudderUtils.timeStamp
        val message = TrackMessage.create("testEvent", timestamp)
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        val sessionTimestamp = defaultLastActiveTimestamp
        val sessionId = 1234567890L
        userSessionState?.update(
            UserSession(
                sessionId = sessionId,
                sessionStart = false,
                isActive = true,
                lastActiveTimestamp = sessionTimestamp
            )
        )
        sessionPlugin.intercept(mockChain)

        val updatedMessageCapture = argumentCaptor<Message>()
        verify(mockChain).proceed(updatedMessageCapture.capture())

        val updatedMessage = updatedMessageCapture.firstValue
        MatcherAssert.assertThat(
            updatedMessage, allOf(
                notNullValue(), hasProperty("timestamp", notNullValue()), hasProperty(
                    "context", allOf(
                        notNullValue(), Matchers.aMapWithSize<String, Any?>(4),
                        hasEntry("sessionId", sessionId.toString()),
                        not(hasKey("sessionStart")),
                    )
                )
            )
        )
        MatcherAssert.assertThat(
            userSessionState?.value, allOf(
                notNullValue(), hasProperty(
                    "sessionStart", `is`(false)
                ), hasProperty("sessionId", `is`(sessionId))
            )
        )
    }
    @Test
    fun `test intercept with valid session and valid context sessionStart false`() {
        val timestamp = RudderUtils.timeStamp
        val message = TrackMessage.create("testEvent", timestamp,
            traits = mapOf("trait1" to "value1"), externalIds = listOf(mapOf("externalId1" to
                    "value1")), customContextMap = mapOf("customContext1" to "value1"))
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        val sessionTimestamp = defaultLastActiveTimestamp
        val sessionId = 1234567890L
        userSessionState?.update(
            UserSession(
                sessionId = sessionId,
                sessionStart = false,
                isActive = true,
                lastActiveTimestamp = sessionTimestamp
            )
        )
        sessionPlugin.intercept(mockChain)

        val updatedMessageCapture = argumentCaptor<Message>()
        verify(mockChain).proceed(updatedMessageCapture.capture())

        val updatedMessage = updatedMessageCapture.firstValue
        MatcherAssert.assertThat(
            updatedMessage, allOf(
                notNullValue(), hasProperty("timestamp", notNullValue()),
                hasProperty(
                    "context", allOf(
                        notNullValue(), Matchers.aMapWithSize<String, Any?>(4),
                        hasEntry("traits", mapOf("trait1" to "value1")),
                        hasEntry("externalId", listOf(mapOf("externalId1" to "value1"))),
                        hasEntry("customContextMap", mapOf("customContext1" to "value1")),
                        hasEntry("sessionId", sessionId.toString()),
                        not(hasKey("sessionStart")),
                    )
                )
            )
        )
        MatcherAssert.assertThat(
            userSessionState?.value, allOf(
                notNullValue(), hasProperty(
                    "sessionStart", `is`(false)
                ), hasProperty("sessionId", `is`(sessionId))
            )
        )
    }
    @Test
    fun `test intercept with valid session and valid context sessionStart true`() {
        val timestamp = RudderUtils.timeStamp
        val message = TrackMessage.create("testEvent", timestamp,
            traits = mapOf("trait1" to "value1"), externalIds = listOf(mapOf("externalId1" to
                    "value1")), customContextMap = mapOf("customContext1" to "value1"))
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        val sessionTimestamp = defaultLastActiveTimestamp
        val sessionId = 1234567890L
        userSessionState?.update(
            UserSession(
                sessionId = sessionId,
                sessionStart = true,
                isActive = true,
                lastActiveTimestamp = sessionTimestamp
            )
        )
        sessionPlugin.intercept(mockChain)

        val updatedMessageCapture = argumentCaptor<Message>()
        verify(mockChain).proceed(updatedMessageCapture.capture())

        val updatedMessage = updatedMessageCapture.firstValue
        MatcherAssert.assertThat(
            updatedMessage, allOf(
                notNullValue(), hasProperty("timestamp", notNullValue()),
                hasProperty(
                    "context", allOf(
                        notNullValue(), Matchers.aMapWithSize<String, Any?>(5),
                        hasEntry("traits", mapOf("trait1" to "value1")),
                        hasEntry("externalId", listOf(mapOf("externalId1" to "value1"))),
                        hasEntry("customContextMap", mapOf("customContext1" to "value1")),
                        hasEntry("sessionId", sessionId.toString()),
                        hasEntry("sessionStart", true),
                    )
                )
            )
        )
        MatcherAssert.assertThat(
            userSessionState?.value, allOf(
                notNullValue(), hasProperty(
                    "sessionStart", `is`(false)
                ), hasProperty("sessionId", `is`(sessionId))
            )
        )
    }
    @Test
    fun `test intercept with inactive session and null context`() {
        val timestamp = RudderUtils.timeStamp
        val message = TrackMessage.create("testEvent", timestamp)
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        val sessionTimestamp = defaultLastActiveTimestamp
        val sessionId = 1234567890L
        userSessionState?.update(
            UserSession(
                sessionId = sessionId,
                sessionStart = true,
                isActive = false,
                lastActiveTimestamp = sessionTimestamp
            )
        )
        sessionPlugin.intercept(mockChain)

        val updatedMessageCapture = argumentCaptor<Message>()
        verify(mockChain).proceed(updatedMessageCapture.capture())

        val updatedMessage = updatedMessageCapture.firstValue
        MatcherAssert.assertThat(
            updatedMessage, allOf(
                notNullValue(), hasProperty("timestamp", notNullValue()), hasProperty(
                    "context", allOf(
                        notNullValue(), Matchers.aMapWithSize<String, Any?>(3),
                        not(hasKey("sessionId")),
                        not(hasKey("sessionStart")),
                    )
                )
            )
        )
        MatcherAssert.assertThat(
            userSessionState?.value, allOf(
                notNullValue(), hasProperty(
                    "sessionStart", `is`(true)
                ), hasProperty("sessionId", `is`(sessionId)),
                hasProperty("active", `is`(false))
            )
        )
    }
    @Test
    fun `test intercept with inactive session and valid context`() {
        val timestamp = RudderUtils.timeStamp
        val message = TrackMessage.create("testEvent", timestamp,
            traits = mapOf("trait1" to "value1"), externalIds = listOf(mapOf("externalId1" to
                    "value1")), customContextMap = mapOf("customContext1" to "value1"))
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        userSessionState?.update(
            UserSession(
                sessionId = -1L,
                sessionStart = false,
                isActive = false,
                lastActiveTimestamp = -1L
            )
        )
        sessionPlugin.intercept(mockChain)

        val updatedMessageCapture = argumentCaptor<Message>()
        verify(mockChain).proceed(updatedMessageCapture.capture())

        val updatedMessage = updatedMessageCapture.firstValue
        MatcherAssert.assertThat(
            updatedMessage, allOf(
                notNullValue(), hasProperty("timestamp", notNullValue()),
                hasProperty(
                    "context", allOf(
                        notNullValue(), Matchers.aMapWithSize<String, Any?>(3),
                        hasEntry("traits", mapOf("trait1" to "value1")),
                        hasEntry("externalId", listOf(mapOf("externalId1" to "value1"))),
                        hasEntry("customContextMap", mapOf("customContext1" to "value1")),
                        not(hasKey("sessionId")),
                        not(hasKey("sessionStart")),
                    )
                )
            )
        )
        MatcherAssert.assertThat(
            userSessionState?.value, allOf(
                notNullValue(), hasProperty(
                    "sessionStart", `is`(false)
                ), hasProperty("sessionId", `is`(-1L))
            )
        )
    }
}