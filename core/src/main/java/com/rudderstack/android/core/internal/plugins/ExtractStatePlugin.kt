/*
 * Creator: Debanjan Chatterjee on 25/03/22, 1:29 PM Last modified: 25/03/22, 1:29 PM
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

import com.rudderstack.android.core.*
import com.rudderstack.android.core.State
import com.rudderstack.android.core.internal.KeyConstants
import com.rudderstack.android.core.internal.ifNotNull
import com.rudderstack.android.core.internal.optAdd
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.*

/**
 * Mutates the system state, if required, based on the Event.
 * In case of [IdentifyMessage], it is expected to save the traits provided.
 */
internal class ExtractStatePlugin(
    private val contextState:
    State<MessageContext>,
    private val settingsState: State<Settings>,
    private val options: RudderOptions,
    private val storage: Storage
) : Plugin {
    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()
        var newContext : MessageContext? = null
        if (message is IdentifyMessage) {
            //save and update traits
            //update userId
            //save and update external ids
            message.context?.let {
                newContext = it
                contextState.update(newContext)
                (it.getOrDefault(KeyConstants.CONTEXT_USER_ID_KEY, null))?.let { id ->
                    settingsState.update(SettingsState.value?.copy(userId = id as String))
                } ?: (it.getOrDefault(KeyConstants.CONTEXT_ID_KEY, null))?.let { id ->
                    settingsState.update(SettingsState.value?.copy(userId = id as String))
                }
                it.traits ifNotNull storage::saveTraits
                it.externalIds ifNotNull storage::saveExternalIds
            }
        }
        //save and update external ids if available
        if(!options.externalIds.isNullOrEmpty()){
            newContext = mapOf("externalIds" to options.externalIds) optAdd newContext
        }

        newContext?.apply {
            contextState.update(this optAdd contextState.value)
        }
        return chain.proceed(message)
    }

}