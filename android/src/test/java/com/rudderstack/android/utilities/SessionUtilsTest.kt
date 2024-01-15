package com.rudderstack.android.utilities

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Analytics
import com.rudderstack.core.Logger
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.android.UserSession
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class SessionUtilsTest {
    private lateinit var analytics: Analytics

    @Before
    fun setup() {
        analytics = generateTestAnalytics(
            mockConfiguration = ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                JacksonAdapter(),
                trackLifecycleEvents = true,
                trackAutoSession = true
            )
        )
    }

    @After
    fun teardown() {
        UserSessionState.update(UserSession())
        analytics.currentConfigurationAndroid?.storage?.clearStorage()
        analytics.shutdown()
    }

    @Test
    fun `test start session`() {
        analytics.startSession()
        val session = UserSessionState.value
        assertNotNull(session)
        assertTrue(session?.isActive == true)
        assertTrue(session?.sessionId != null)
        assertTrue(session?.sessionStart == true)
        assertTrue(session?.lastActiveTimestamp != null)
    }

    @Test
    fun `test start session with valid sessionId`() {
        val sessionId = 1234567890L
        analytics.startSession(sessionId)
        MatcherAssert.assertThat(
            (ConfigurationsState.value as ConfigurationAndroid).trackAutoSession,
            `is`(false)
        )
        val session = UserSessionState.value
        assertNotNull(session)
        assertTrue(session?.isActive == true)
        MatcherAssert.assertThat(session?.sessionId, `is`(sessionId))
        assertTrue(session?.sessionStart == true)
        assertTrue(session?.lastActiveTimestamp != null)
    }

    /*private val mockAndroidConfig
        get() = run {
            val mockConfig = mock<ConfigurationAndroid>()
            whenever(mockConfig.maxFlushInterval).thenReturn(10000L)
            whenever(mockConfig.flushQueueSize).thenReturn(1000)
            whenever(mockConfig.storage).thenReturn(AndroidStorageImpl(ApplicationProvider.getApplicationContext()))
            // mock copy method for ConfigurationAndroid with all arguments
//            whenever(mockConfig.copy(any(), any(), any(), any(), any(), any(), any(), any(), any
//                (), any(), any(), any(), any(), any(), any(), any(),
//                any(), any(), any(), any(), any(), any(),
//                )).then {
//                mockConfig
//            }

            mockConfig
        }*/
    @Test
    fun `test startSession with invalid sessionId`() {
        // Given
        val invalidSessionId = 123456789L
        val logger = mock<Logger>()

        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true,
            logger = logger,
            storage = mock<AndroidStorage>()
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startSession(invalidSessionId)

        // Then
        // Verify that logger.warn is called with the expected message
        verify(logger).warn(
            "Rudderstack User Session",
            "Invalid session id $invalidSessionId. Must be at least 10 digits"
        )
        //verify SessionState is not updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, `is`(-1L))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(-1L))
        MatcherAssert.assertThat(session?.isActive, `is`(false))
        MatcherAssert.assertThat(session?.sessionStart, `is`(false))
    }

    @Test
    fun `test endSession`() {
        analytics.startSession()

        val session = UserSessionState.value
        //check session is started
        MatcherAssert.assertThat(session?.sessionId, `is`(not(-1L)))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(not(-1L)))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
        MatcherAssert.assertThat(session?.sessionStart, `is`(true))
        // When
        analytics.endSession()
        // verify
        val sessionAfterEnd = UserSessionState.value
        MatcherAssert.assertThat(sessionAfterEnd?.sessionId, `is`(-1L))
        MatcherAssert.assertThat(sessionAfterEnd?.lastActiveTimestamp, `is`(-1L))
        MatcherAssert.assertThat(sessionAfterEnd?.isActive, `is`(false))
        MatcherAssert.assertThat(sessionAfterEnd?.sessionStart, `is`(false))
        //should update trackAutoSession to false
        MatcherAssert.assertThat(
            (ConfigurationsState.value as ConfigurationAndroid).trackAutoSession,
            `is`(false)
        )

    }

    @Test
    fun `test startSessionIfNeeded with a new session`() {
        // Given
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true
        )
        UserSessionState.update(UserSession())
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startSessionIfNeeded()

        // Then
        // Verify that SessionState is updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, `is`(not(-1L)))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(not(-1L)))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
        MatcherAssert.assertThat(session?.sessionStart, `is`(true))
    }

    @Test
    fun `test startSessionIfNeeded not updating session when session is ongoing`() {
        // Given
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true,
            sessionTimeoutMillis = 10000L
        )
        val sessionId = 1234567890L
        val lastActiveTimestamp = defaultLastActiveTimestamp
        UserSessionState.update(
            UserSession(
                sessionId = sessionId, isActive = true, lastActiveTimestamp = lastActiveTimestamp
            )
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startSessionIfNeeded()

        // Then
        // Verify that SessionState is not updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, `is`(sessionId))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(lastActiveTimestamp))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test startSessionIfNeeded with an expired session`() {
// Given
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true
        )
        val sessionId = 1234567890L
        val lastActiveTimestamp = System.currentTimeMillis() - mockConfig.sessionTimeoutMillis
        UserSessionState.update(
            UserSession(
                sessionId = sessionId, isActive = true, lastActiveTimestamp = lastActiveTimestamp
            )
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startSessionIfNeeded()

        // Then
        // Verify that SessionState is updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, `is`(not(sessionId)))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(not(lastActiveTimestamp)))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
        MatcherAssert.assertThat(session?.sessionStart, `is`(true))
    }

    @Test
    fun `test startSessionIfNeeded with an expired session and sessionTimeoutMillis is 0`() {
        // Given
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true,
            sessionTimeoutMillis = 0L
        )
        val sessionId = 1234567890L
        val lastActiveTimestamp = System.currentTimeMillis() - mockConfig.sessionTimeoutMillis
        UserSessionState.update(
            UserSession(
                sessionId = sessionId, isActive = true, lastActiveTimestamp = lastActiveTimestamp
            )
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startSessionIfNeeded()

        // Then
        // Verify that SessionState is updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, `is`(not(sessionId)))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(not(lastActiveTimestamp)))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
        MatcherAssert.assertThat(session?.sessionStart, `is`(true))
    }

    @Test
    fun `test initializeSessionManagement with a saved ongoing session`() {
        // Given
        val mockStorage = mock<AndroidStorage>()
        val sessionId = 1234567890L
        val lastActiveTimestamp = defaultLastActiveTimestamp
        whenever(mockStorage.sessionId).thenReturn(sessionId)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(lastActiveTimestamp)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true,
            storage = mockStorage
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.initializeSessionManagement()

        // Then
        // Verify that SessionState is not updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, `is`(sessionId))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(lastActiveTimestamp))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test initializeSessionManagement with a saved but expired session`() {
        // Given
        val mockStorage = mock<AndroidStorage>()
        val sessionId = 1234567890L
        val lastActiveTimestamp =
            defaultLastActiveTimestamp - ConfigurationAndroid.Defaults.SESSION_TIMEOUT - 1L //expired session
        whenever(mockStorage.sessionId).thenReturn(sessionId)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(lastActiveTimestamp)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true,
            storage = mockStorage
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.initializeSessionManagement()

        // Then
        // Verify that SessionState is updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, allOf(not(sessionId), not(-1L)))
        MatcherAssert.assertThat(
            session?.lastActiveTimestamp, allOf(
                not(lastActiveTimestamp), not(-1L)
            )
        )
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test initializeSessionManagement without saved session`() {
        // Given
        val mockStorage = mock<AndroidStorage>()
        whenever(mockStorage.sessionId).thenReturn(null)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(null)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true,
            storage = mockStorage
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.initializeSessionManagement()
        // Verify that SessionState is updated
        val session = UserSessionState.value
        MatcherAssert.assertThat(session?.sessionId, not(-1L))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, not(-1L))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test listenToSessionChanges with active session`() {
// Given
        val mockStorage = mock<AndroidStorage>()
        whenever(mockStorage.sessionId).thenReturn(null)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(null)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            JacksonAdapter(),
            trackLifecycleEvents = true,
            trackAutoSession = true,
            storage = mockStorage
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.initializeSessionManagement()
        val sessionId = 1234567890L
        val lastActiveTimestamp = defaultLastActiveTimestamp
        UserSessionState.update(
            UserSession(
                lastActiveTimestamp, sessionId, isActive = true, false
            )
        )
        // Verify that storage is updated
        val sessionIdCapturer = argumentCaptor<Long>()
        verify(mockStorage, atLeast(2)).setSessionId(sessionIdCapturer.capture()) // once on
        // session start, one for session update
        MatcherAssert.assertThat(sessionIdCapturer.allValues, hasItem(sessionId))
        val lastActiveTimestampCapturer = argumentCaptor<Long>()
        verify(mockStorage, atLeast(2)).saveLastActiveTimestamp(
            lastActiveTimestampCapturer.capture()
        ) //
        // once on
        // session start, one for session update
        MatcherAssert.assertThat(lastActiveTimestampCapturer.allValues, hasItem(lastActiveTimestamp))
    }

}