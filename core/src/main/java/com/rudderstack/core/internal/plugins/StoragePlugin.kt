package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message
import java.lang.ref.WeakReference

/**
 * Adds [Message] to repository for further processing.
 * Used for cloud mode destinations.
 * Saves user related data in case of identify messages and
 *
 */
internal class StoragePlugin : Plugin {
    private var _analytics: WeakReference<Analytics?> = WeakReference(null)
    override fun setup(analytics: Analytics) {
        _analytics = WeakReference(analytics)
    }

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()
        _analytics.get()?.storage?.saveMessage(message.copy())

        return chain.proceed(message)
    }

    override fun onShutDown() {
        _analytics = WeakReference(null)
    }
}
