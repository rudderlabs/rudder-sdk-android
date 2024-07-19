package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.RudderUtils.MAX_EVENT_SIZE
import com.rudderstack.core.RudderUtils.getUTF8Length
import com.rudderstack.core.internal.CentralPluginChain
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.TrackMessage
import com.rudderstack.gsonrudderadapter.GsonAdapter
import org.junit.Test

class EventSizeFilterPluginTest {

    private val eventSizeFilterPlugin = EventSizeFilterPlugin()
    private val currentConfiguration = Configuration(jsonAdapter = GsonAdapter())

    @Test
    fun `given event size does not exceed the maximum size, then the next plugin in the chain should be called`() {
        var isCalled = false
        val testPlugin = object : Plugin {
            override lateinit var analytics: Analytics
            override fun intercept(chain: Plugin.Chain): Message {
                isCalled = true
                return chain.proceed(chain.message())
            }
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
        val testPlugin = object : Plugin {
            override lateinit var analytics: Analytics
            override fun intercept(chain: Plugin.Chain): Message {
                isCalled = true
                return chain.proceed(chain.message())
            }
        }
        val message = getMessageOverMaxSize()
        val eventSizeFilterTestChain = CentralPluginChain(
            message,
            listOf(eventSizeFilterPlugin, testPlugin),
            originalMessage = message
        )
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
            val messageJSON = currentConfiguration.jsonAdapter.writeToJson(message)
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
            val messageJSON = currentConfiguration.jsonAdapter.writeToJson(message)
            val messageSize = messageJSON.toString().getUTF8Length()
            assert(messageSize > MAX_EVENT_SIZE)
        }
    }
}
