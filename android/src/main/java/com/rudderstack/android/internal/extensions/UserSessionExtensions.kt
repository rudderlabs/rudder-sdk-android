/*
 * Creator: Debanjan Chatterjee on 08/01/24, 5:29 pm Last modified: 08/01/24, 5:29 pm
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

package com.rudderstack.android.internal.extensions

import com.rudderstack.core.with
import com.rudderstack.models.MessageContext
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
