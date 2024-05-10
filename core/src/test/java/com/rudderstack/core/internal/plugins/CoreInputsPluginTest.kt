package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.Storage
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CoreInputsPluginTest{
    private lateinit var analytics: Analytics
    private lateinit var storage: Storage
    private lateinit var coreInputsPlugin: CoreInputsPlugin
    @Before
    fun setup(){
        coreInputsPlugin = CoreInputsPlugin()
        storage = mock<Storage>()
        whenever(storage.libraryName) doReturn "MyLibrary"
        whenever(storage.libraryVersion) doReturn "1.0"
//        `when`(storage.libraryName).thenReturn("MyLibrary")
//        `when`(storage.libraryVersion).thenReturn("1.0")
        analytics = generateTestAnalytics(mock<JsonAdapter>(),mock(), storage = storage)
    }
    @After
    fun shutdown(){
        coreInputsPlugin.onShutDown()
        storage.shutdown()
        analytics.shutdown()
    }
    @Test
    fun `intercept method proceeds without modification when storage is null`() {
        // Arrange
        val mockChain = mock<Plugin.Chain>()
        val mockMessage = TrackMessage.create("ev_name", RudderUtils.timeStamp)
        `when`(mockChain.message()).thenReturn(mockMessage)
        whenever(mockChain.proceed(any())) doAnswer{
            it.getArgument(0)
        }

        // Act
        val result = coreInputsPlugin.intercept(mockChain)

        // Assert
        assertThat(result, `is`(mockMessage))
        verify(mockChain).proceed(mockMessage)
    }


    @Test
    fun `intercept method adds library context to message context when storage is not null`() {
        // Arrange
        val mockChain = mock<Plugin.Chain>()
        val customContextMap = mapOf("existingKey" to "existingValue")
        val mockMessage = TrackMessage.create("ev_name", RudderUtils.timeStamp,
            customContextMap = customContextMap)
        `when`(mockChain.message()).thenReturn(mockMessage)
        whenever(mockChain.proceed(any())) doAnswer{
            it.getArgument(0)
        }
        coreInputsPlugin.setup(analytics)
        // Act
        val result = coreInputsPlugin.intercept(mockChain)

        // Assert
        val expectedContext = mapOf("customContextMap" to customContextMap) + mapOf("library" to mapOf("name" to "MyLibrary", "version" to "1.0"))
        assertThat(result.context?.filterValues { it != null }, `is`(equalTo(expectedContext)))
        verify(mockChain).proceed(mockMessage.copy(context = result.context))
    }
    @Test
    fun `intercept method adds library context to message context when storage is not null context null`() {
        // Arrange
        val mockChain = mock<Plugin.Chain>()
        val mockMessage = TrackMessage.create("ev_name", RudderUtils.timeStamp)
        `when`(mockChain.message()).thenReturn(mockMessage)
        whenever(mockChain.proceed(any())) doAnswer{
            it.getArgument(0)
        }
//        val existingContext = null
        /*`when`(mockMessage.context).thenReturn(existingContext)
        `when`(mockChain.message()).thenReturn(mockMessage)*/
        coreInputsPlugin.setup(analytics)
        // Act
        val result = coreInputsPlugin.intercept(mockChain)

        // Assert
        val expectedContext = mapOf("library" to mapOf("name" to "MyLibrary", "version" to "1.0"))
        assertThat(result.context?.filterValues { it != null }, `is`(equalTo(expectedContext)))
        verify(mockChain).proceed(mockMessage.copy(context = result.context))
    }
}