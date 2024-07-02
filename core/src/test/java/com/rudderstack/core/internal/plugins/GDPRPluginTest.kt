package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.core.models.TrackMessage
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test

class GDPRPluginTest {
    private val gdprPlugin = GDPRPlugin()
    private val message = TrackMessage.create(
        "ev-1", RudderUtils.timeStamp,
        traits = mapOf(
            "age" to 31,
            "office" to "Rudderstack"
        ),
        externalIds = listOf(
            mapOf("some_id" to "s_id"),
            mapOf("amp_id" to "amp_id"),
        ),
        customContextMap = null
    )

    @Test
    fun `test gdpr with opt out`() {
        val analytics = mockk<Analytics>(relaxed = true)
        every { analytics.storage.isOptedOut } returns true
        val testPluginForOptOut = Plugin {
            //should not be called
            assert(false)
            it.proceed(it.message())
        }
        val optOutTestChain = CentralPluginChain(
            message, listOf(gdprPlugin, testPluginForOptOut), originalMessage = message
        )
        //opted out
        gdprPlugin.setup(analytics)
        //check for opt out
        val returnedMsg = optOutTestChain.proceed(message)
        assertThat(returnedMsg, Matchers.`is`(returnedMsg))
    }

    @Test
    fun `test gdpr with opt in`() {
        val analytics = mockk<Analytics>(relaxed = true)
        every { analytics.storage.isOptedOut } returns false

        var isCalled = false
        val testPluginForOptIn = Plugin {
            //should be called
            isCalled = true
            it.proceed(it.message())
        }
        val optInTestChain = CentralPluginChain(
            message, listOf(gdprPlugin, testPluginForOptIn),
            originalMessage = message
        )
        //opted out
        gdprPlugin.setup(analytics)
        //check for opt out
        val returnedMsg = optInTestChain.proceed(message)
        assertThat(returnedMsg, Matchers.`is`(returnedMsg))
        assertThat(isCalled, Matchers.`is`(true))
    }
}
