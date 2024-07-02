package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message

/**
 * If opted out, msg won't go forward, will return from here.
 *
 */
internal class GDPRPlugin : Plugin {

    private var _analytics: Analytics? = null
    override fun intercept(chain: Plugin.Chain): Message {
        val isOptOut = _analytics?.storage?.isOptedOut ?: false
        return if (isOptOut) {
            chain.message()
        } else {
            chain.proceed(chain.message())
        }
    }

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
    }
}
