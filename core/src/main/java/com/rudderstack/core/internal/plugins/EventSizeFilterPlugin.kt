package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils.MAX_EVENT_SIZE
import com.rudderstack.core.RudderUtils.getUTF8Length
import com.rudderstack.core.models.Message
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.util.concurrent.atomic.AtomicReference

/**
 * A plugin to filter out events that exceed the maximum size limit.
 */
class EventSizeFilterPlugin : Plugin {

    private val currentConfigurationAtomic = AtomicReference<Configuration?>()
    private val currentConfiguration
        get() = currentConfigurationAtomic.get()

    override fun updateConfiguration(configuration: Configuration) {
        currentConfigurationAtomic.set(configuration)
    }

    override fun intercept(chain: Plugin.Chain): Message {
        currentConfiguration?.let { config ->
            val messageJSON = chain.message().let {
                config.jsonAdapter.writeToJson(it, object : RudderTypeAdapter<Message>() {})
            }
            val messageSize = messageJSON.toString().getUTF8Length()
            if (messageSize > MAX_EVENT_SIZE) {
                config.logger.error(log = "Event size exceeds the maximum size of $MAX_EVENT_SIZE bytes. Dropping the event.")
                return chain.message()
            }
        }
        return chain.proceed(chain.message())
    }
}
