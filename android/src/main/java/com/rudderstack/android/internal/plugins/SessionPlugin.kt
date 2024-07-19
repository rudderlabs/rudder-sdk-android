package com.rudderstack.android.internal.plugins

import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.internal.extensions.withSessionId
import com.rudderstack.android.internal.extensions.withSessionStart
import com.rudderstack.android.models.UserSession
import com.rudderstack.android.utilities.defaultLastActiveTimestamp
import com.rudderstack.android.utilities.resetSession
import com.rudderstack.android.utilities.startSessionIfNeeded
import com.rudderstack.android.utilities.updateSessionEnd
import com.rudderstack.android.utilities.userSessionState
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message

internal class SessionPlugin : Plugin {

    override lateinit var analytics: Analytics

    private var currentConfiguration: ConfigurationAndroid? = null

    override fun updateConfiguration(configuration: Configuration) {
        if (configuration !is ConfigurationAndroid) return
        if (currentConfiguration?.trackAutoSession == configuration.trackAutoSession
            && currentConfiguration?.trackLifecycleEvents == configuration.trackLifecycleEvents
        ) return
        if (!configuration.trackAutoSession || !configuration.trackLifecycleEvents) {
            analytics.updateSessionEnd()
            return
        }
        analytics.startSessionIfNeeded()
    }


    override fun intercept(chain: Plugin.Chain): Message {
        //apply session id and session start
        // update last active timestamp
        // if difference between two events is more than session timeout, refresh session
        val message = chain.message()
        analytics.startSessionIfNeeded()
        val newMsg = analytics.userSessionState?.value?.takeIf { it.isActive }?.let {
            updateWithSession(message, it)
        } ?: message

        return chain.proceed(newMsg)
    }

    private fun updateWithSession(
        message: Message, it: UserSession
    ): Message {
        val context = message.context
        var newContext = context?.withSessionId(it.sessionId.toString())
        if (it.sessionStart) newContext = newContext?.withSessionStart(true)

        updateSessionState(it)
        return message.copy(newContext)
    }

    private fun updateSessionState(it: UserSession) {
        val updatedSession = it.copy(
            lastActiveTimestamp = defaultLastActiveTimestamp, sessionStart = false
        )
        analytics.userSessionState?.update(updatedSession)
    }


    override fun reset() {
        analytics.resetSession()
    }

}
