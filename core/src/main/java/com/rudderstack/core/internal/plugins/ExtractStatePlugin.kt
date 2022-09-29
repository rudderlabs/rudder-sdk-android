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

package com.rudderstack.core.internal.plugins

import com.rudderstack.core.*
import com.rudderstack.core.State
import com.rudderstack.core.internal.KeyConstants
import com.rudderstack.core.internal.optAdd
import com.rudderstack.core.internal.states.SettingsState
import com.rudderstack.models.*

/**
 * Mutates the system state, if required, based on the Event.
 * In case of [IdentifyMessage], it is expected to save the traits provided.
 */
internal class ExtractStatePlugin(
    private val contextState:
    State<MessageContext>,
    private val settingsState: State<Settings>,
    private val storage: Storage
) : Plugin {

    private var _analytics : Analytics? = null
    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
    }
    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()
//        var newContext: MessageContext?

        val newMsg = if (message is IdentifyMessage || message is AliasMessage) {
            // alias message can change user id permanently
            //save and update traits
            //update userId
            //save and update external ids
            (message.context ?: createContext()).let {

                //alias and identify messages are expected to contain user id.
                //We check in context as well as context.traits with either keys "userId" and "id"
                //user id can be retrieved if put directly in context or context.traits with the
                //aforementioned ids
                val newUserId = getUserId(message)

                _analytics?.logger?.debug(log = "New user id detected: $newUserId")
                // in case of identify, the stored traits (if any) are replaced by the ones provided
                when (message) {
                    is AliasMessage -> {
                        // in case of alias, we change the user id in traits
                        val prevId = settingsState.value?.let { it.userId ?: it.anonymousId } ?: ""
                        message.updateNewAndPrevUserId(
                            prevId,
                            newUserId, it
                        )

                    }
                    else -> message
                }.also {
                    newUserId?.let { id ->
                        settingsState.update(SettingsState.value?.copy(userId = id))
                    }
                }
            }.also { msg ->
                msg.context?.let {
                    storage.cacheContext(it)
                    contextState.update(it)
                }

            }
        } else message

        return chain.proceed(newMsg)
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
    private fun getUserId(message: Message): String? {
        return message.userId ?: message.context?.let {
            (it[KeyConstants.CONTEXT_USER_ID_KEY]
                ?: (it.traits?.get(KeyConstants.CONTEXT_USER_ID_KEY))
                ?: (it[KeyConstants.CONTEXT_USER_ID_KEY_ALIAS])
                ?: (it.traits?.get(KeyConstants.CONTEXT_USER_ID_KEY_ALIAS))
                ?: (it[KeyConstants.CONTEXT_ID_KEY])
                ?: (it.traits?.get(KeyConstants.CONTEXT_ID_KEY))) as? String?
        }
    }

    private fun AliasMessage.updateNewAndPrevUserId(
        previousId: String,
        newUserId: String?,
        messageContext: MessageContext
    ): AliasMessage {
        val newTraits = newUserId?.let { newId ->
            mapOf(
                KeyConstants.CONTEXT_ID_KEY to
                        newId,
                KeyConstants.CONTEXT_USER_ID_KEY to
                        newId
            ) optAdd messageContext.traits
        }
        //also in case of alias, user id in context should also change, given it's
        // present there
        return messageContext.updateWith(
            traits =
            newTraits
        ).let {
            this.copy(context = it, userId = newUserId, previousId = previousId)
        }
    }


}