package com.rudderstack.core

import junit.framework.TestCase.assertEquals
import org.junit.Test

class RudderOptionTest {
    @Test
    fun `given all the externalIds are of different type, when externalIds are passed, they should be added to the list`() {
        val rudderOption = RudderOption().apply {
            putExternalId("brazeExternalID", "12345")
            putExternalId("amplitudeExternalID", "67890")
        }

        val expectedExternalIds = listOf(
            mapOf("type" to "brazeExternalID", "id" to "12345"),
            mapOf("type" to "amplitudeExternalID", "id" to "67890"),
        )

        assertEquals(expectedExternalIds, rudderOption.externalIds)
    }

    @Test
    fun `given few externalIds have same type but different Ids, when externalIds are passed, they should be merged in the list`() {
        val rudderOption = RudderOption().apply {
            putExternalId("brazeExternalID", "12345")
            putExternalId("brazeExternalID", "67890")
            putExternalId("amplitudeExternalID", "67890")
        }

        val expectedExternalIds = listOf(
            mapOf("type" to "brazeExternalID", "id" to "67890"),
            mapOf("type" to "amplitudeExternalID", "id" to "67890"),
        )

        assertEquals(expectedExternalIds, rudderOption.externalIds)
    }

    @Test
    fun `given few externalIds have same type and Ids, when externalIds are passed, they should be merged in the list`() {
        val rudderOption = RudderOption().apply {
            putExternalId("brazeExternalID", "12345")
            putExternalId("brazeExternalID", "12345")
        }

        val expectedExternalIds = listOf(
            mapOf("type" to "brazeExternalID", "id" to "12345"),
        )

        assertEquals(expectedExternalIds, rudderOption.externalIds)
    }
}
