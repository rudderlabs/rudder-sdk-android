package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message

/**
 * If opted out, msg won't go forward, will return from here.
 *
 */
internal class GDPRPlugin : Plugin {

    override lateinit var analytics: Analytics

    override fun intercept(chain: Plugin.Chain): Message {
        return if (analytics.storage.isOptedOut) {
            chain.message()
        } else {
            chain.proceed(chain.message())
        }
    }
}
