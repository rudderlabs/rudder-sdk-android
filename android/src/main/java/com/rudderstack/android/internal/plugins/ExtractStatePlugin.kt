/*
 * Creator: Debanjan Chatterjee on 29/11/23, 5:37 pm Last modified: 21/11/23, 5:14 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin

import com.rudderstack.core.optAdd
import com.rudderstack.models.AliasMessage
import com.rudderstack.models.IdentifyMessage
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext
import com.rudderstack.models.createContext
import com.rudderstack.models.traits
import com.rudderstack.models.updateWith

/**
 * Mutates the system state, if required, based on the Event.
 * In case of [IdentifyMessage], it is expected to save the traits provided.
 */
internal class ExtractStatePlugin: Plugin {

    private var _analytics: Analytics? = null
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
                        val prevId = _analytics?.currentConfigurationAndroid?.let {
                            it.userId ?: it.anonymousId
                        } ?: ""
                        message.updateNewAndPrevUserId(
                            prevId, newUserId, it
                        )

                    }

                    else -> message
                }.also {
                    newUserId?.let { id ->
                        _analytics?.applyConfiguration {
                            if (this is ConfigurationAndroid) copy(userId = id)
                            else this
                        }
                    }
                }
            }.also { msg ->
                msg.context?.let {
                    _analytics?.currentConfiguration?.storage?.cacheContext(it)
                    ContextState.update(it)
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
             ?: (it.traits?.get(KeyConstants.CONTEXT_ID_KEY)))?.toString()
        }
    }

    private fun AliasMessage.updateNewAndPrevUserId(
        previousId: String, newUserId: String?, messageContext: MessageContext
    ): AliasMessage {
        val newTraits = newUserId?.let { newId ->
            mapOf(
                KeyConstants.CONTEXT_ID_KEY to newId, KeyConstants.CONTEXT_USER_ID_KEY to newId
            ) optAdd messageContext.traits
        }
        //also in case of alias, user id in context should also change, given it's
        // present there
        return messageContext.updateWith(
            traits = newTraits
        ).let {
            this.copy(context = it, userId = newUserId, previousId = previousId)
        }
    }

    object KeyConstants {
        const val CONTEXT_USER_ID_KEY = "user_id"
        const val CONTEXT_USER_ID_KEY_ALIAS = "userId"
        const val CONTEXT_ID_KEY = "id"
        const val CONTEXT_EXTERNAL_ID_KEY = "externalIds"
    }

    override fun reset() {
        super.reset()
        ContextState.update()
    }
}