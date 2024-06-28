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
import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.internal.extensions.withSessionId
import com.rudderstack.android.internal.extensions.withSessionStart
import com.rudderstack.android.utilities.androidStorage
import com.rudderstack.android.utilities.defaultLastActiveTimestamp
import com.rudderstack.android.utilities.resetSession
import com.rudderstack.android.utilities.startAutoSessionIfNeeded
import com.rudderstack.android.utilities.updateSessionEnd
import com.rudderstack.android.utilities.userSessionState
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.models.Message
import com.rudderstack.models.android.UserSession

internal class SessionPlugin : Plugin, LifecycleListenerPlugin {

    private var _analytics: Analytics? = null
    private var currentConfiguration: ConfigurationAndroid? = null
    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
    }

    override fun updateConfiguration(configuration: Configuration) {
        if (configuration !is ConfigurationAndroid) return
        if ((currentConfiguration?.trackAutoSession
                ?: _analytics?.androidStorage?.trackAutoSession) == configuration.trackAutoSession
        ) return
        _analytics?.androidStorage?.setTrackAutoSession(configuration.trackAutoSession)
        if (!configuration.trackAutoSession) {
            _analytics?.updateSessionEnd()
            return
        }
        _analytics?.startAutoSessionIfNeeded()
    }

    override fun intercept(chain: Plugin.Chain): Message {
        //apply session id and session start
        // update last active timestamp
        // if difference between two events is more than session timeout, refresh session
        val message = chain.message()
        val newMsg = _analytics?.userSessionState?.value?.takeIf { it.isActive }?.let {
            val msg = updateWithSession(message, it)
            if(it.sessionStart)
                it.markSessionAsStarted()
            msg
        } ?: message

        return chain.proceed(newMsg)
    }

    private fun UserSession.markSessionAsStarted() {
        val updatedSession = copy(
            sessionStart = false
        )
        _analytics?.userSessionState?.update(updatedSession)
    }

    private fun updateWithSession(
        message: Message, it: UserSession
    ): Message {
        val context = message.context
        var newContext = context?.withSessionId(it.sessionId)
        if (it.sessionStart) newContext = newContext?.withSessionStart(true)

        return message.copy(newContext)
    }

    override fun onAppForegrounded() {
        _analytics?.startAutoSessionIfNeeded()
    }

    override fun onAppBackgrounded() {
        _analytics?.userSessionState?.value?.updateSessionStateWithLatestTimestamp()
    }

    private fun UserSession.updateSessionStateWithLatestTimestamp() {
        val updatedSession = copy(
            lastActiveTimestamp = defaultLastActiveTimestamp, sessionStart = false
        )
        _analytics?.userSessionState?.update(updatedSession)
    }


    override fun reset() {
        _analytics?.resetSession()
    }

}