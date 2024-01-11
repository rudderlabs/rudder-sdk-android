/*
 * Creator: Debanjan Chatterjee on 09/01/24, 7:14 pm Last modified: 09/01/24, 7:14 pm
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

package com.rudderstack.android.utilities

import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.core.Analytics
import com.rudderstack.models.android.UserSession
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow

private val defaultSessionId
    get() = TimeUnit.MILLISECONDS.toSeconds(
        System.currentTimeMillis()
    )
private const val SESSION_ID_MIN_LENGTH = 10
internal val defaultLastActiveTimestamp
    get() = System.currentTimeMillis()
@JvmOverloads
fun Analytics.startSession(
    sessionId: Long = defaultSessionId
) {
    if(!isSessionIdValid(sessionId)){
        currentConfiguration?.logger?.warn("Rudderstack User Session",
            "Invalid session id $sessionId. Must be at least 10 digits")
    }
    if (currentConfigurationAndroid?.trackAutoSession == true) {
        endSession()
        applyConfiguration {
            if (this is ConfigurationAndroid) copy(
                trackAutoSession = false
            )
            else this
        }
    }
    updateSessionStart(sessionId)
}

fun isSessionIdValid(sessionId: Long): Boolean {
    return sessionId/10.0.pow(SESSION_ID_MIN_LENGTH - 1) >= 1
}

fun Analytics.endSession() {
    applyConfiguration {
        if (this is ConfigurationAndroid) copy(
            trackAutoSession = false
        )
        else this
    }
    updateSessionEnd()
}



internal fun Analytics.startSessionIfNeeded() {
    if (currentConfigurationAndroid?.trackAutoSession != true || currentConfigurationAndroid?.trackLifecycleEvents != true) return

    val currentSession = UserSessionState.value
    if (currentSession == null) {
        updateSessionStart(defaultSessionId)
        return
    }
    if (!currentSession.isActive || currentSession.lastActiveTimestamp == -1L) {
        updateSessionStart(defaultSessionId)
        return
    }
    val timeDifference: Long = synchronized(this) {
        abs(System.currentTimeMillis() - currentSession.lastActiveTimestamp)
    }
    if (timeDifference > (currentConfigurationAndroid?.sessionTimeoutMillis ?: 0)) {
        refreshSessionUpdate()
    }
}
internal fun Analytics.initializeSessionManagement() {
    val savedSessionId = currentConfigurationAndroid?.storage?.sessionId
    val lastActiveTimestamp = currentConfigurationAndroid?.storage?.lastActiveTimestamp
    if (currentConfigurationAndroid?.trackAutoSession != true || currentConfigurationAndroid?.trackLifecycleEvents != true) {
        discardAnyPreviousSession(savedSessionId, lastActiveTimestamp)
        return
    }

    if (savedSessionId != null && lastActiveTimestamp != null) {
        UserSessionState.update(
            UserSession(
                sessionId = savedSessionId,
                isActive = true,
                lastActiveTimestamp = lastActiveTimestamp
            )
        )
    }
    startSessionIfNeeded()
    listenToSessionChanges()
}

fun Analytics.discardAnyPreviousSession(savedSessionId: Long?, lastActiveTimestamp: Long?) {
    if (savedSessionId != null && lastActiveTimestamp != null) {
        applySessionToStorage(UserSession())
    }
    updateSessionEnd()
}

private fun Analytics.listenToSessionChanges() {
    UserSessionState.subscribe{ newState, _ ->
        newState?.apply {
            applySessionToStorage( this)
        }
    }
}

private fun Analytics.applySessionToStorage(
    userSession: UserSession
) {
    if(userSession.isActive) {
        currentConfigurationAndroid?.storage?.setSessionId(userSession.sessionId)
        currentConfigurationAndroid?.storage?.saveLastActiveTimestamp(userSession.lastActiveTimestamp)
    }else{
        currentConfigurationAndroid?.storage?.clearSessionId()
        currentConfigurationAndroid?.storage?.clearLastActiveTimestamp()
    }
}

internal fun Analytics.shutdownSessionManagement() {
    UserSessionState.removeAllObservers()
}
internal fun Analytics.updateSessionStart(sessionId: Long) {
    UserSessionState.update(
        UserSession(
            sessionId = sessionId,
            isActive = true,
            lastActiveTimestamp = defaultLastActiveTimestamp,
            sessionStart = true
        )
    )
}
internal fun Analytics.resetSession(){
    updateSessionEnd()
    startSessionIfNeeded()
}
internal fun Analytics.updateSessionEnd() {
    UserSessionState.update(
        UserSession(sessionId = -1L, isActive = false, lastActiveTimestamp = -1L)
    )
}

internal fun Analytics.refreshSessionUpdate() {
    updateSessionEnd()
    updateSessionStart(defaultSessionId)
}
