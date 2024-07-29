package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.Storage
import com.rudderstack.core.models.Message

private const val LIBRARY_KEY = "library"

/**
 * Plugin to add the library details to context object of payload.
 */
class CoreInputsPlugin : Plugin {
    override lateinit var analytics: Analytics

    private val Storage.libraryContextPair
        get() = LIBRARY_KEY to mapOf("name" to libraryName, "version" to libraryVersion)

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()
        val context = analytics.storage.let { storage ->
            message.context?.let { it + storage.libraryContextPair } ?: mapOf(storage.libraryContextPair)
        }
        return chain.proceed(message.copy(context = context))
    }

}
