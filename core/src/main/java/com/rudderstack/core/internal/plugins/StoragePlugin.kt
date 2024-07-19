package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message

/**
 * Adds [Message] to repository for further processing.
 * Used for cloud mode destinations.
 * Saves user related data in case of identify messages and
 *
 */
internal class StoragePlugin : Plugin {

    override lateinit var analytics: Analytics

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()
        analytics.storage.saveMessage(message.copy())
        return chain.proceed(message)
    }

}
