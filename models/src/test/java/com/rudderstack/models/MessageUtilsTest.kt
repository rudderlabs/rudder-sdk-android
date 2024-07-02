package com.rudderstack.models

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MessageUtilsTest {
    @Test
    fun `given both the contexts are null, when optAddContext is called, then it should return null`() {
        val context1: MessageContext? = null
        val context2: MessageContext? = null

        val result = context1 optAddContext context2

        assertNull(result)
    }

    @Test
    fun `given first context is null, when optAddContext is called, then it should return second context`() {
        val context1: MessageContext? = null
        val context2: MessageContext = mapOf("key" to "value")

        val result = context1 optAddContext context2

        assertEquals(context2, result)
    }

    @Test
    fun `given second context is null, when optAddContext is called, then it should return first context`() {
        val context1: MessageContext = mapOf("key" to "value")
        val context2: MessageContext? = null

        val result = context1 optAdd context2

        assertEquals(context1, result)
    }

    @Test
    fun `given there are overlapping keys in both contexts, when optAddContext is called, then it should return the merged context while prioritizing the key-value pair from the first context`() {
        val context1: MessageContext = createContext(
            mapOf("trait1" to "value1", "common" to "value1")
        )
        val context2: MessageContext = createContext(
            mapOf("trait2" to "value2", "common" to "value2")
        )

        val result = context1 optAddContext context2

        assertThat(
            result?.traits, allOf(
                hasEntry("trait1", "value1"),
                hasEntry("trait2", "value2"),
                hasEntry("common", "value1"),
            )
        )
    }

    @Test
    fun `given there are no overlapping keys in both contexts, when optAddContext is called, then it should return the merged context`() {
        val context1: MessageContext = mapOf(
            Constants.CUSTOM_CONTEXT_MAP_ID to mapOf("context1" to "value1")
        )
        val context2: MessageContext = mapOf(
            Constants.CUSTOM_CONTEXT_MAP_ID to mapOf("context2" to "value2")
        )

        val result = context1 optAddContext context2

        assertThat(
            result?.customContexts, allOf(
                hasEntry("context1", "value1"),
                hasEntry("context2", "value2"),
            )
        )
    }

    @Test
    fun `given there are overlapping externalIds in both contexts, when optAddContext is called, then it should return the merged context while prioritizing the key-value pair from the first context`() {
        val currentEventContext: MessageContext = createContext(
            externalIds = listOf(
                mapOf("type" to "brazeExternalID", "id" to "braze-67890-override"),
                mapOf("type" to "amplitudeExternalID", "id" to "amp-5678-override"),
                mapOf("type" to "firebaseExternalID", "id" to "fire-67890"),
            )
        )
        val savedContext: MessageContext = createContext(
            externalIds = listOf(
                mapOf("type" to "brazeExternalID", "id" to "braze-1234"),
                mapOf("type" to "amplitudeExternalID", "id" to "amp-5678"),
                mapOf("type" to "adobeExternalID", "id" to "fire-67890"),
            )
        )

        val result = currentEventContext optAddContext savedContext

        assertThat(
            result?.externalIds,
            hasItems(
                mapOf("type" to "brazeExternalID", "id" to "braze-67890-override"),
                mapOf("type" to "amplitudeExternalID", "id" to "amp-5678-override"),
                mapOf("type" to "adobeExternalID", "id" to "fire-67890"),
                mapOf("type" to "firebaseExternalID", "id" to "fire-67890"),
            )
        )
    }

    @Test
    fun `given there are overlapping extra keys in both contexts, when optAddContext is called, then it should return the merged context while prioritizing the key-value pair from the first context`() {
        val context1: MessageContext = mapOf("extra1" to "value1", "common" to "value1")
        val context2: MessageContext = mapOf("extra2" to "value2", "common" to "value2")

        val result = context1 optAddContext context2

        assertThat(
            result, allOf(
                hasEntry("extra1", "value1"),
                hasEntry("extra2", "value2"),
                hasEntry("common", "value1"),
            )
        )
    }
}
