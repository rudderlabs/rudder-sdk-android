package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.RudderUtils.MAX_EVENT_SIZE
import com.rudderstack.core.RudderUtils.getUTF8Length
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.models.Message
import com.rudderstack.models.TrackMessage
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

class EventSizeFilterPluginTest {
    @get:Rule
    val mockkRule = MockKRule(this)
    private val eventSizeFilterPlugin = EventSizeFilterPlugin()
    private val currentConfiguration = Configuration( isOptOut = false)
    private val jsonAdapter = GsonAdapter()
    @Test
    fun `given event size does not exceed the maximum size, then the next plugin in the chain should be called`() {
        var isCalled = false
        val testPlugin = Plugin {
            isCalled = true
            it.proceed(it.message())
        }
        val message = getMessageUnderMaxSize()
        val eventSizeFilterTestChain = CentralPluginChain(
            message, listOf(
                eventSizeFilterPlugin, testPlugin
            ), originalMessage = message
        )
        eventSizeFilterPlugin.updateConfiguration(currentConfiguration)

        val returnedMsg = eventSizeFilterTestChain.proceed(message)
        assert(returnedMsg == message)
        assert(isCalled)
    }

    @Test
    fun `given event size exceeds the maximum size, then the next plugin in the chain should not be called`() {
        var isCalled = false
        val testPlugin = Plugin {
            isCalled = true
            it.proceed(it.message())
        }
        val message = getMessageOverMaxSize()
        val eventSizeFilterTestChain = CentralPluginChain(
            message,
            listOf(eventSizeFilterPlugin, testPlugin),
            originalMessage = message
        )
        val analytics = mockk<Analytics>()
        every { analytics.jsonAdapter } returns jsonAdapter

        eventSizeFilterPlugin.setup(analytics)
        eventSizeFilterPlugin.updateConfiguration(currentConfiguration)

        val returnedMsg = eventSizeFilterTestChain.proceed(message)
        assert(returnedMsg == message)
        assert(!isCalled)
    }

    private fun getMessageUnderMaxSize(): Message {
        return TrackMessage.create(
            "ev-1", RudderUtils.timeStamp, traits = mapOf(
                "age" to 31, "office" to "Rudderstack"
            ), externalIds = listOf(
                mapOf("some_id" to "s_id"),
                mapOf("amp_id" to "amp_id"),
            ), customContextMap = null
        ).also { message ->
            val messageJSON = jsonAdapter.writeToJson(message)
            val messageSize = messageJSON.toString().getUTF8Length()
            assert(messageSize < MAX_EVENT_SIZE)
        }
    }

    private fun getMessageOverMaxSize(): Message {
        fun generateDataOfSize(msgSize: Int): String {
            return CharArray(msgSize).apply { fill('a') }.joinToString("")
        }

        val properties = mutableMapOf<String, Any>()
        properties["property"] = generateDataOfSize(1024 * 33)

        return TrackMessage.create(
            "ev-1", RudderUtils.timeStamp, properties = properties
        ).also { message ->
            val messageJSON = jsonAdapter.writeToJson(message)
            val messageSize = messageJSON.toString().getUTF8Length()
            assert(messageSize > MAX_EVENT_SIZE)
        }
    }
}
