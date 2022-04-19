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
import com.rudderstack.android.core.internal.KeyConstants
import com.rudderstack.android.core.internal.KeyConstants.CONTEXT_EXTERNAL_ID_KEY
import com.rudderstack.android.core.internal.ifNotNull
import com.rudderstack.android.core.internal.minusWrtKeys
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
        var newContext: MessageContext?

        if (message is IdentifyMessage || message is AliasMessage) {
            // alias message can change user id permanently
            //save and update traits
            //update userId
            //save and update external ids
            newContext = message.context?.let {

                //alias and identify messages are expected to contain user id.
                //We check in context as well as context.traits with either keys "userId" and "id"
                //user id can be retrieved if put directly in context or context.traits with the
                //aforementioned ids
                val newUserId = getUserId(it)

                // in case of identify, the stored traits (if any) are replaced by the ones provided
                if (message is IdentifyMessage) {
                    it.traits ifNotNull storage::saveTraits

                } else if (message is AliasMessage) {
                    // in case of alias, we change the user id in traits

                    newUserId?.let { newId ->
                        mapOf(
                            KeyConstants.CONTEXT_ID_KEY to
                                    newId,
                            KeyConstants.CONTEXT_USER_ID_KEY to
                                    newId
                        ) optAdd it.traits
                    } ifNotNull storage::saveTraits

                }
                newUserId?.let { id ->
                    settingsState.update(SettingsState.value?.copy(userId = id))
                }
                //also in case of alias, user id in context should also change, given it's
                // present there
                if (message is AliasMessage) {
                    it optAdd (it[KeyConstants.CONTEXT_USER_ID_KEY])?.let {
                        mapOf(KeyConstants.CONTEXT_USER_ID_KEY to it)
                    }
                    it optAdd (it[KeyConstants.CONTEXT_ID_KEY])?.let {
                        mapOf(KeyConstants.CONTEXT_ID_KEY to it)
                    }

                } else
                    it
            }
            //save and update external ids if available
            if (message is IdentifyMessage) {
                //external ids can be present in both message or options.
                //save concatenated external ids, if present with both message and options
                val messageExternalIds = message.context?.externalIds
                val updatedExternalIds: List<Map<String, String>>? =
                    if (options.externalIds.isEmpty()) {
                        if (messageExternalIds != null) {
                            storage.saveExternalIds(messageExternalIds)

                            message.context?.externalIds
                        } else null
                    } else {
                        if (messageExternalIds == null) {
                            options.externalIds
                        } else {//preference is given to external ids in Message
                            val extraIdsInOptions =
                                options.externalIds minusWrtKeys messageExternalIds
                            //adding these to messageExternalIds gives our required ids
                            messageExternalIds + extraIdsInOptions
                        }
                    }
                updatedExternalIds ifNotNull storage::saveExternalIds
                newContext = updatedExternalIds?.let {
                    mapOf(CONTEXT_EXTERNAL_ID_KEY to it) optAdd newContext
                } ?: newContext
            }
            newContext?.apply {
                contextState.update(this optAdd contextState.value)
            }

        }

        return chain.proceed(message)
    }

    /**
     * Checks in the order
     * "user_id" key at root
     * "user_id" key at context.traits
     * "userId" key at root
     * "userId" key at context.traits
     * "id" key at root
     * "id" key at context.traits
     *
     *
     */
    private fun getUserId(it: MessageContext): String? {
        return (it.getOrDefault(KeyConstants.CONTEXT_USER_ID_KEY, null)
            ?: (it.traits?.getOrDefault(KeyConstants.CONTEXT_USER_ID_KEY, null))
            ?: (it.getOrDefault(KeyConstants.CONTEXT_USER_ID_KEY_ALIAS, null))
            ?: (it.traits?.getOrDefault(KeyConstants.CONTEXT_USER_ID_KEY_ALIAS, null))
            ?: (it.getOrDefault(KeyConstants.CONTEXT_ID_KEY, null))
            ?: (it.traits?.getOrDefault(KeyConstants.CONTEXT_ID_KEY, null))
                ) as? String?
    }

}