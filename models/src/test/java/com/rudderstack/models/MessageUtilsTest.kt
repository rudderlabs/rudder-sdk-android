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
    fun `optAdd with both contexts null`() {
        val context1: MessageContext? = null
        val context2: MessageContext? = null

        val result = context1 optAddContext context2

        assertNull(result)
    }

    @Test
    fun `optAdd with first context null`() {
        val context1: MessageContext? = null
        val context2: MessageContext = mapOf("key" to "value")

        val result = context1 optAddContext context2

        assertEquals(context2, result)
    }

    @Test
    fun `optAdd with second context null`() {
        val context1: MessageContext = mapOf("key" to "value")
        val context2: MessageContext? = null

        val result = context1 optAdd context2

        assertEquals(context1, result)
    }

    @Test
    fun `optAdd with overlapping traits`() {
        val context1: MessageContext = createContext(
            mapOf("trait1" to "value1", "common" to "value1")
        )
        val context2: MessageContext = createContext(
            mapOf("trait2" to "value2", "common" to "value2")
        )
        val result = context1 optAddContext context2
        println(result)
        assertThat(
            result?.traits, allOf(
                hasEntry("trait1", "value1"),
                hasEntry("trait2", "value2"),
                hasEntry("common", "value1"),
            )
        )
    }

    @Test
    fun `optAdd with non-overlapping customContexts`() {
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
    fun `optAdd with overlapping externalIds`() {
        val savedContext: MessageContext = createContext(
            externalIds = listOf(
                mapOf("type" to "brazeExternalID", "id" to "braze-1234"),
                mapOf("type" to "amplitudeExternalID", "id" to "amp-5678"),
                mapOf("type" to "adobeExternalID", "id" to "fire-67890"),
            )
        )
        val currentEventContext: MessageContext = createContext(
            externalIds = listOf(
                mapOf("type" to "brazeExternalID", "id" to "braze-67890-override"),
                mapOf("type" to "amplitudeExternalID", "id" to "amp-5678-override"),
                mapOf("type" to "firebaseExternalID", "id" to "fire-67890"),
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
    fun `optAdd with extra keys`() {
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
