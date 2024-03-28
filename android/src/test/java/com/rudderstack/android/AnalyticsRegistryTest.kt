package com.rudderstack.android

import com.rudderstack.core.Analytics
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class AnalyticsRegistryTest {

    private val writeKey = "writeKey"
    private val analytics = mockk<Analytics>()

    @Before
    fun setUp() {
        AnalyticsRegistry.clear()
    }

    @Test
    fun `when register is called with a write key and analytics instance, then the instance should be registered`() {
        AnalyticsRegistry.register(writeKey, analytics)

        val result = AnalyticsRegistry.getInstance(writeKey)
        assert(result == analytics)
    }

    @Test
    fun `when registering multiple analytics instances with different write keys, then all instances should be registered`() {
        val writeKey2 = "writeKey2"
        val analytics2 = mockk<Analytics>()

        AnalyticsRegistry.register(writeKey, analytics)
        AnalyticsRegistry.register(writeKey2, analytics2)

        val result1 = AnalyticsRegistry.getInstance(writeKey)
        val result2 = AnalyticsRegistry.getInstance(writeKey2)

        assert(result1 == analytics)
        assert(result2 == analytics2)
    }

    @Test
    fun `given analytics instance already registered with the writeKey, when register is called with same write key and a new analytics instance, then the instance should not be registered`() {
        AnalyticsRegistry.register(writeKey, analytics)

        val newAnalytics = mockk<Analytics>()
        AnalyticsRegistry.register(writeKey, newAnalytics)

        val result = AnalyticsRegistry.getInstance(writeKey)
        assert(result == analytics)
    }

    @Test
    fun `given analytics instance with the write key exists, when getInstance is called with that write key, then the Analytics instance should be returned`() {
        AnalyticsRegistry.register(writeKey, analytics)

        val result = AnalyticsRegistry.getInstance(writeKey)
        assert(result == analytics)
    }

    @Test
    fun `given analytics instance with the writeKey doesn't exists, when getInstance is called with that unregistered writeKey, then null should be returned`() {
        AnalyticsRegistry.register(writeKey, analytics)

        val result = AnalyticsRegistry.getInstance("unRegisteredWriteKey")
        assert(result == null)
    }
}
