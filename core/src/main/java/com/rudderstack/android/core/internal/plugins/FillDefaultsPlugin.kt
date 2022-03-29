/*
 * Creator: Debanjan Chatterjee on 26/03/22, 12:12 AM Last modified: 26/03/22, 12:12 AM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

package com.rudderstack.android.core.internal.plugins

import com.rudderstack.android.core.Plugin
import com.rudderstack.android.core.Settings
import com.rudderstack.android.core.State
import com.rudderstack.android.core.internal.MissingPropertiesException
import com.rudderstack.android.core.internal.optAdd
import com.rudderstack.android.core.internal.states.ContextState
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.IdentifyMessage
import com.rudderstack.android.models.Message
import com.rudderstack.android.models.MessageContext
import com.rudderstack.android.models.TrackMessage
import com.rudderstack.android.rudderjsonadapter.JsonAdapter

internal class FillDefaultsPlugin(
    private val defaultContext: Map<String, Any>, private val settingsState: State<Settings>,
    private val contextState: State<MessageContext>
) : Plugin {

    /**
     * Fill default details for [TrackMessage]
     * @throws [MissingPropertiesException] if neither of userId or anonymous id is present
     */
    @Throws(MissingPropertiesException::class)
    fun <T : Message> T.withDefaults(): T {
        val anonId = this.anonymousId ?: settingsState.value?.anonymousId
        val userId = this.userId ?: SettingsState.value?.userId
        if (anonId == null && userId == null)
            throw MissingPropertiesException("Either Anonymous Id or User Id must be present");
        //copying top level context to message context
        return this.copy(
            context = defaultContext optAdd (context ?: contextState.value),
            anonymousId = anonId, userId = userId
        ) as T
    }

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message().let {
            if (it is IdentifyMessage)
                it
            else it.withDefaults()
        }
        return chain.proceed(message)
    }
}