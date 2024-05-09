package com.rudderstack.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.initialConfigurationAndroid
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.utils.busyWait
import com.rudderstack.core.Analytics
import com.rudderstack.core.Logger
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.removeState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.models.android.UserSession
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.allOf
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class SessionUtilsTest {
    private lateinit var analytics: Analytics
    private lateinit var mockStorage: AndroidStorage
    private val userSessionState: UserSessionState?
        get() = analytics.retrieveState()
    @Before
    fun setup() {
        mockStorage = mock<AndroidStorage>()
        analytics = generateTestAnalytics( mock(),
            mockConfiguration = ConfigurationAndroid(
                ApplicationProvider.getApplicationContext(),
                shouldVerifySdk = false,
                trackLifecycleEvents = true,
                trackAutoSession = true
            ),
            storage = mockStorage
        )
        analytics.associateState(UserSessionState())
    }

    @After
    fun teardown() {
        analytics.removeState<UserSessionState>()
        analytics.storage.clearStorage()
        analytics.shutdown()
    }

    @Test
    fun `test start session`() {
        analytics.startSession()
        val session = userSessionState?.value
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
            (analytics.currentConfigurationAndroid)?.trackAutoSession,
            `is`(false)
        )
        val session = userSessionState?.value
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
            application = ApplicationProvider.getApplicationContext(),
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true,
            logger = logger,
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
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, `is`(-1L))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(-1L))
        MatcherAssert.assertThat(session?.isActive, `is`(false))
        MatcherAssert.assertThat(session?.sessionStart, `is`(false))
    }

    @Test
    fun `test endSession`() {
        analytics.startSession()

        val session = userSessionState?.value
        //check session is started
        MatcherAssert.assertThat(session?.sessionId, `is`(not(-1L)))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(not(-1L)))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
        MatcherAssert.assertThat(session?.sessionStart, `is`(true))
        // When
        analytics.endSession()
        // verify
        val sessionAfterEnd = userSessionState?.value
        MatcherAssert.assertThat(sessionAfterEnd?.sessionId, `is`(-1L))
        MatcherAssert.assertThat(sessionAfterEnd?.lastActiveTimestamp, `is`(-1L))
        MatcherAssert.assertThat(sessionAfterEnd?.isActive, `is`(false))
        MatcherAssert.assertThat(sessionAfterEnd?.sessionStart, `is`(false))
        //should update trackAutoSession to false
        MatcherAssert.assertThat(
            analytics.currentConfigurationAndroid?.trackAutoSession,
            `is`(false)
        )

    }

    @Test
    fun `test startSessionIfNeeded with a new session`() {
        // Given
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true
        )
        userSessionState?.update(UserSession())
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startAutoSessionIfNeeded()

        // Then
        // Verify that SessionState is updated
        val session = userSessionState?.value
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
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true,
            sessionTimeoutMillis = 10000L
        )
        val sessionId = 1234567890L
        val lastActiveTimestamp = defaultLastActiveTimestamp
        userSessionState?.update(
            UserSession(
                sessionId = sessionId, isActive = true, lastActiveTimestamp = lastActiveTimestamp
            )
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startAutoSessionIfNeeded()

        // Then
        // Verify that SessionState is not updated
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, `is`(sessionId))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(lastActiveTimestamp))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test startSessionIfNeeded with an expired session`() {
// Given
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true
        )
        val sessionId = 1234567890L
        val lastActiveTimestamp = System.currentTimeMillis() - mockConfig.sessionTimeoutMillis
        userSessionState?.update(
            UserSession(
                sessionId = sessionId, isActive = true, lastActiveTimestamp = lastActiveTimestamp
            )
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.startAutoSessionIfNeeded()

        // Then
        // Verify that SessionState is updated
        val session = userSessionState?.value
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
            shouldVerifySdk = false,

            trackAutoSession = true,
            sessionTimeoutMillis = 0L
        )
        val sessionId = 1234567890L
        val lastActiveTimestamp = System.currentTimeMillis()
        userSessionState?.update(
            UserSession(
                sessionId = sessionId, isActive = true, lastActiveTimestamp = lastActiveTimestamp
            )
        )
        analytics.applyConfiguration {
            mockConfig
        }
        busyWait(1)
        // When
        analytics.startAutoSessionIfNeeded()

        // Then
        // Verify that SessionState is updated
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, `is`(not(sessionId)))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, `is`(not(lastActiveTimestamp)))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
        MatcherAssert.assertThat(session?.sessionStart, `is`(true))
    }

    @Test
    fun `test initializeSessionManagement with a saved ongoing session`() {
        // Given
        val sessionId = 1234567890L
        val lastActiveTimestamp = defaultLastActiveTimestamp
        whenever(mockStorage.sessionId).thenReturn(sessionId)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(lastActiveTimestamp)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true,
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.initializeSessionManagement(mockStorage.sessionId, mockStorage.lastActiveTimestamp)
        busyWait(1)
        // Then
        // Verify that SessionState is not updated
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, `is`(sessionId))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, not(lastActiveTimestamp))// should
        // be updated
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test initializeSessionManagement with a saved but expired session`() {
        // Given
        val sessionId = 1234567890L
        val lastActiveTimestamp =
            defaultLastActiveTimestamp - ConfigurationAndroid.Defaults.SESSION_TIMEOUT - 1L //expired session
        whenever(mockStorage.sessionId).thenReturn(sessionId)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(lastActiveTimestamp)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true,
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.initializeSessionManagement(null, null)

        // Then
        // Verify that SessionState is updated
        val session = userSessionState?.value
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
        whenever(mockStorage.sessionId).thenReturn(null)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(null)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true,
        )
        analytics.applyConfiguration {
            mockConfig
        }
        // When
        analytics.initializeSessionManagement(null, null)
        // Verify that SessionState is updated
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, not(-1L))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, not(-1L))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test listenToSessionChanges with active session`() {
// Given
        whenever(mockStorage.sessionId).thenReturn(null)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(null)
        val mockConfig = ConfigurationAndroid(
            ApplicationProvider.getApplicationContext(),
            shouldVerifySdk = false,
            trackLifecycleEvents = true,
            trackAutoSession = true,
        )
        analytics.shutdown()
        analytics = generateTestAnalytics(
            mock<JsonAdapter>(),
            mockConfiguration = mockConfig,
            storage = mockStorage
        )
        // When
        analytics.initializeSessionManagement()
        val sessionId = 1234567890L
        val lastActiveTimestamp = defaultLastActiveTimestamp
        userSessionState?.update(
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

    @Test
    fun `test manual session continued across reinitialize if trackAutoSession is false`(){
        analytics.shutdown()
        // Given
        val sessionId = 1234567890L
        val sessionTimeout = 9999L
        val lastActiveTimestamp = defaultLastActiveTimestamp - 10000L // more than session timeout
        whenever(mockStorage.trackAutoSession).thenReturn(false)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(lastActiveTimestamp)
        whenever(mockStorage.sessionId).thenReturn(sessionId)
        val newConfig = ApplicationProvider.getApplicationContext<Application>()
            .initialConfigurationAndroid(mockStorage).copy(sessionTimeoutMillis = sessionTimeout,
                shouldVerifySdk = false)
        analytics = generateTestAnalytics(
            mock<JsonAdapter>(),
            mockConfiguration = newConfig,
            storage = mockStorage
        )
        // When
        analytics.initializeSessionManagement(mockStorage.sessionId, mockStorage.lastActiveTimestamp)
        // Verify that sessionState is updated
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, `is`(sessionId))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, not(lastActiveTimestamp)) //should change
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }
    @Test
    fun `test manual session continued if trackAutoSession is true and timeout not reached`(){
        analytics.shutdown()
        // Given
        val sessionId = 1234567890L
        val sessionTimeout = 9999L
        val lastActiveTimestamp = defaultLastActiveTimestamp  // more than session timeout
        whenever(mockStorage.trackAutoSession).thenReturn(false)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(lastActiveTimestamp)
        whenever(mockStorage.sessionId).thenReturn(sessionId)
        val newConfig = ApplicationProvider.getApplicationContext<Application>()
            .initialConfigurationAndroid(mockStorage).copy(sessionTimeoutMillis = sessionTimeout,
                trackAutoSession = true,
                trackLifecycleEvents = true,
                shouldVerifySdk = false)
        analytics = generateTestAnalytics(
            mock<JsonAdapter>(),
            mockConfiguration = newConfig,
            storage = mockStorage
        )
        busyWait(1)
        // When
        analytics.initializeSessionManagement(mockStorage.sessionId, mockStorage.lastActiveTimestamp)
        // Verify that sessionState is updated
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, `is`(sessionId))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, not(lastActiveTimestamp))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }

    @Test
    fun `test manual session discontinued if trackAutoSession is true and timeout reached`() {
        analytics.shutdown()
        // Given
        val sessionId = 1234567890L
        val sessionTimeout = 9999L
        val lastActiveTimestamp = defaultLastActiveTimestamp - 10000L // more than session timeout
        whenever(mockStorage.trackAutoSession).thenReturn(false)
        whenever(mockStorage.lastActiveTimestamp).thenReturn(lastActiveTimestamp)
        whenever(mockStorage.sessionId).thenReturn(sessionId)
        val newConfig = ApplicationProvider.getApplicationContext<Application>()
            .initialConfigurationAndroid(mockStorage).copy(
                sessionTimeoutMillis = sessionTimeout,
                trackLifecycleEvents = true,
                trackAutoSession = true,
                shouldVerifySdk = false
            )
        analytics = generateTestAnalytics(
            mock<JsonAdapter>(), mockConfiguration = newConfig, storage = mockStorage
        )
        // When
        analytics.initializeSessionManagement(
            mockStorage.sessionId,
            mockStorage.lastActiveTimestamp
        )
        // Verify that sessionState is updated
        val session = userSessionState?.value
        MatcherAssert.assertThat(session?.sessionId, not(sessionId))
        MatcherAssert.assertThat(session?.lastActiveTimestamp, not(lastActiveTimestamp))
        MatcherAssert.assertThat(session?.isActive, `is`(true))
    }


}