/*
 * Creator: Debanjan Chatterjee on 08/01/24, 7:03 pm Last modified: 08/01/24, 7:03 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.internal.plugins

import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.internal.extensions.withSessionId
import com.rudderstack.android.internal.extensions.withSessionStart
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.utilities.defaultLastActiveTimestamp
import com.rudderstack.android.utilities.resetSession
import com.rudderstack.android.utilities.startSessionIfNeeded
import com.rudderstack.android.utilities.updateSessionEnd
import com.rudderstack.android.utilities.userSessionState
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.models.Message
import com.rudderstack.models.android.UserSession

internal class SessionPlugin : Plugin {

    private var _analytics: Analytics? = null
    private var currentConfiguration: ConfigurationAndroid? = null
    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
    }

    override fun updateConfiguration(configuration: Configuration) {
        if (configuration !is ConfigurationAndroid) return
        if (currentConfiguration?.trackAutoSession == configuration.trackAutoSession
            && currentConfiguration?.trackLifecycleEvents == configuration.trackLifecycleEvents) return
        if( !configuration.trackAutoSession || !configuration.trackLifecycleEvents) {
            _analytics?.updateSessionEnd()
            return
        }
        _analytics?.startSessionIfNeeded()
    }

    override fun intercept(chain: Plugin.Chain): Message {
        //apply session id and session start
        // update last active timestamp
        // if difference between two events is more than session timeout, refresh session
        val message = chain.message()
        _analytics?.startSessionIfNeeded()
        val newMsg = _analytics?.userSessionState?.value?.takeIf { it.isActive }?.let {
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
        _analytics?.userSessionState?.update(updatedSession)
    }


    override fun reset() {
        _analytics?.resetSession()
    }

}