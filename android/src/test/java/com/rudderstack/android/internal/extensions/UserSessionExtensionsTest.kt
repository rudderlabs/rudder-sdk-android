package com.rudderstack.android.internal.extensions
import com.rudderstack.core.with
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.sameInstance


private const val CONTEXT_SESSION_ID_KEY = "sessionId"
private const val CONTEXT_SESSION_START_KEY = "sessionStart"
class UserSessionExtensionTest {



    @Test
    fun `withSessionId should add sessionId to MessageContext`() {
        // Arrange
        val originalContext = mapOf("key" to "value")

        // Act
        val updatedContext = originalContext.withSessionId(123)
        println(updatedContext)
        // Assert
        assertThat(updatedContext, hasEntry(CONTEXT_SESSION_ID_KEY, 123L))
        assertThat(originalContext, not(sameInstance(updatedContext)))
    }

    @Test
    fun `withSessionStart should add sessionStart to MessageContext`() {
        // Arrange
        val originalContext = mapOf("key" to "value")

        // Act
        val updatedContext = originalContext.withSessionStart(true)

        // Assert
        assertThat(updatedContext, hasEntry(CONTEXT_SESSION_START_KEY, true))
        assertThat(originalContext, not(sameInstance(updatedContext)))
    }

    @Test
    fun `removeSessionContext should remove sessionId and sessionStart from MessageContext`() {
        // Arrange
        val originalContext = mapOf(
            CONTEXT_SESSION_ID_KEY to 123,
            CONTEXT_SESSION_START_KEY to true,
            "key" to "value"
        )

        // Act
        val updatedContext = originalContext.removeSessionContext()

        // Assert
        assertThat(updatedContext, not(hasKey(CONTEXT_SESSION_ID_KEY)))
        assertThat(updatedContext, not(hasKey(CONTEXT_SESSION_START_KEY)))
        assertThat(originalContext, not(sameInstance(updatedContext)))
    }

    @Test
    fun `removeSessionContext should handle empty MessageContext`() {
        // Arrange
        val originalContext = emptyMap<String, Any?>()

        // Act
        val updatedContext = originalContext.removeSessionContext()

        // Assert
        assertThat(updatedContext, equalTo(originalContext))
    }

    @Test
    fun `sessionId should return the correct value from MessageContext`() {
        // Arrange
        val context = mapOf(CONTEXT_SESSION_ID_KEY to 123L)

        // Act
        val result = context.sessionId

        // Assert
        assertThat(result, `is`(123L))
    }

    @Test
    fun `sessionId should return null if key is not present in MessageContext`() {
        // Arrange
        val context = mapOf("otherKey" to "value")

        // Act
        val result = context.sessionId

        // Assert
        assertThat(result, nullValue())
    }

    @Test
    fun `sessionStart should return the correct value from MessageContext`() {
        // Arrange
        val context = mapOf(CONTEXT_SESSION_START_KEY to true)

        // Act
        val result = context.sessionStart

        // Assert
        assertThat(result, equalTo(true))
    }

    @Test
    fun `sessionStart should return null if key is not present in MessageContext`() {
        // Arrange
        val context = mapOf("otherKey" to "value")

        // Act
        val result = context.sessionStart

        // Assert
        assertThat(result, nullValue())
    }
}
