package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.Storage
import com.rudderstack.models.Message
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

class CoreInputsPluginTest{
    private lateinit var analytics: Analytics
    private lateinit var storage: Storage

    @Before
    fun setup(){
        analytics = generateTestAnalytics(mock<Configuration>(), storage = storage)
    }
    @Test
    fun `intercept method proceeds without modification when storage is null`() {
        // Arrange
        val mockChain = mock<Plugin.Chain>()
        val mockMessage = TrackMessage.create("ev_name", RudderUtils.timeStamp)
        `when`(mockChain.message()).thenReturn(mockMessage)

        // Act
        val result = CoreInputsPlugin.intercept(mockChain)

        // Assert
        assertThat(result, `is`(mockMessage))
        verify(mockChain).proceed(mockMessage)
    }
    @After
    fun shutdown(){
        CoreInputsPlugin.onShutDown()
        storage.shutdown()
        analytics.shutdown()
    }

    @Test
    fun `intercept method adds library context to message context when storage is not null`() {
        // Arrange
        val mockChain = mock<Plugin.Chain>()
        val mockMessage = TrackMessage.create("ev_name", RudderUtils.timeStamp)
        `when`(mockChain.message()).thenReturn(mockMessage)
        val existingContext = mapOf("existingKey" to "existingValue")
        `when`(mockMessage.context).thenReturn(existingContext)
        `when`(mockChain.message()).thenReturn(mockMessage)
        `when`(storage.libraryName).thenReturn("MyLibrary")
        `when`(storage.libraryVersion).thenReturn("1.0")
        CoreInputsPlugin.setup(analytics)
        // Act
        val result = CoreInputsPlugin.intercept(mockChain)

        // Assert
        val expectedContext = existingContext + mapOf("library" to mapOf("name" to "MyLibrary", "version" to "1.0"))
        assertThat(result.context, `is`(equalTo(expectedContext)))
        verify(mockChain).proceed(mockMessage.copy(context = expectedContext))
    }
    @Test
    fun `intercept method adds library context to message context when storage is not null context null`() {
        // Arrange
        val mockChain = mock<Plugin.Chain>()
        val mockMessage = TrackMessage.create("ev_name", RudderUtils.timeStamp)
        `when`(mockChain.message()).thenReturn(mockMessage)
        val existingContext = null
        `when`(mockMessage.context).thenReturn(existingContext)
        `when`(mockChain.message()).thenReturn(mockMessage)
        `when`(storage.libraryName).thenReturn("MyLibrary")
        `when`(storage.libraryVersion).thenReturn("1.0")
        CoreInputsPlugin.setup(analytics)
        // Act
        val result = CoreInputsPlugin.intercept(mockChain)

        // Assert
        val expectedContext = mapOf("library" to mapOf("name" to "MyLibrary", "version" to "1.0"))
        assertThat(result.context, `is`(equalTo(expectedContext)))
        verify(mockChain).proceed(mockMessage.copy(context = expectedContext))
    }
}