package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.processNewContext
import com.rudderstack.core.Analytics
import com.rudderstack.models.createContext
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasEntry
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ResetImplementationPluginTest{
    @Test
    fun testResetImplementationPlugin(){
        val analytics = generateTestAnalytics(jsonAdapter = mock())
        val resetImplementationPlugin = ResetImplementationPlugin()
        resetImplementationPlugin.setup(analytics)
        //given
        ContextState.update(createContext(
            traits = mapOf("name" to "Debanjan", "email" to "debanjan@rudderstack.com"),
            externalIds = listOf(mapOf("id1" to "v1"), mapOf("id2" to "v2")),
            customContextMap = mapOf("customContext1" to mapOf("key1" to "value1", "key2" to "value2"))
        ))
        //when
        resetImplementationPlugin.reset()
        //then
        assertThat(ContextState.value, allOf(
            hasEntry("traits", emptyMap<String, Any>()),
            hasEntry("externalId", emptyList<Map<String, String>>()),
            hasEntry("customContextMap",
                mapOf("customContext1" to mapOf("key1" to "value1", "key2" to "value2"))),
            ))
        analytics.shutdown()
    }
}