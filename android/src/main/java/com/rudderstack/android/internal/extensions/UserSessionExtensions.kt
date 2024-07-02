package com.rudderstack.android.internal.extensions

import com.rudderstack.core.models.MessageContext
import com.rudderstack.core.with

private const val CONTEXT_SESSION_ID_KEY = "sessionId"
private const val CONTEXT_SESSION_START_KEY = "sessionStart"

internal fun MessageContext.withSessionId(sessionId: String): MessageContext {
    return this.with(CONTEXT_SESSION_ID_KEY to sessionId)
}

internal fun MessageContext.withSessionStart(sessionStart: Boolean): MessageContext {
    return this.with(CONTEXT_SESSION_START_KEY to sessionStart)
}

internal fun MessageContext.removeSessionContext(): MessageContext {
    return this.minus(listOf(CONTEXT_SESSION_ID_KEY, CONTEXT_SESSION_START_KEY))
}

internal val MessageContext.sessionId: String?
    get() = this[CONTEXT_SESSION_ID_KEY] as? String
internal val MessageContext.sessionStart: Boolean?
    get() = this[CONTEXT_SESSION_START_KEY] as? Boolean
