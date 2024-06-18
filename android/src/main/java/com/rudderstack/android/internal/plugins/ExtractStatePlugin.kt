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

import com.rudderstack.android.utilities.contextState
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.android.utilities.processNewContext
import com.rudderstack.android.utilities.setUserId
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.optAdd
import com.rudderstack.models.AliasMessage
import com.rudderstack.models.IdentifyMessage
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext
import com.rudderstack.models.optAddContext
import com.rudderstack.models.traits
import com.rudderstack.models.updateWith

/**
 * Mutates the system state, if required, based on the Event.
 * In case of [IdentifyMessage], it is expected to save the traits provided.
 */
internal class ExtractStatePlugin : Plugin {

    private var _analytics: Analytics? = null
    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
    }

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()

        if (!(message is IdentifyMessage || message is AliasMessage)) {
            return chain.proceed(message)
        }
        // alias message can change user id permanently
        //save and update traits
        //update userId
        //save and update external ids
        (message.context ?: return message).let{
            if(it.traits?.get("anonymousId") == null)
                _analytics?.currentConfigurationAndroid?.anonymousId.let { anonId ->
                    it.updateWith(traits = it.traits optAdd ("anonymousId" to anonId))
                }else it
        }.let {

            //alias and identify messages are expected to contain user id.
            //We check in context as well as context.traits with either keys "userId" and "id"
            //user id can be retrieved if put directly in context or context.traits with the
            //aforementioned ids
            val newUserId = getUserId(message)

            _analytics?.logger?.debug(log = "New user id detected: $newUserId")
            val prevId = _analytics?.currentConfigurationAndroid?.let {
                it.userId ?: it.anonymousId
            } ?: ""
            // in case of identify, the stored traits (if any) are replaced by the ones provided
            // if user id is different. else traits are added to it
            val msg = when (message) {
                is AliasMessage -> {
                    // in case of alias, we change the user id in traits
                    newUserId?.let { newId-> updateNewAndPrevUserIdInContext(
                        newId, it
                    )}?.let {
                        replaceContext(it)
                        message.copy(context = it, userId = newUserId, previousId = prevId)
                    }
                }
                is IdentifyMessage -> {
                    val updatedContext = if(newUserId != prevId) {
                        it
                    } else {
                        appendContextForIdentify(it)
                    }
                    replaceContext(updatedContext)
                    message.copy(context = updatedContext)
                }
                else -> {
                    message
                }
            }?:message
            msg.also {
                newUserId?.let { id ->
                    _analytics?.setUserId(id)
                }
            }
            return chain.proceed(msg)

        }
    }

    private fun appendContextForIdentify(messageContext: MessageContext) : MessageContext{
       return _analytics?.contextState?.value?.let {
           messageContext optAddContext it
       }?: messageContext
    }

    private fun replaceContext(messageContext: MessageContext) {
        _analytics?.processNewContext(messageContext)
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
        return  message.context?.let {
            (it[KeyConstants.CONTEXT_USER_ID_KEY]
             ?: (it.traits?.get(KeyConstants.CONTEXT_USER_ID_KEY))
             ?: (it[KeyConstants.CONTEXT_USER_ID_KEY_ALIAS])
             ?: (it.traits?.get(KeyConstants.CONTEXT_USER_ID_KEY_ALIAS))
             ?: (it[KeyConstants.CONTEXT_ID_KEY])
             ?: (it.traits?.get(KeyConstants.CONTEXT_ID_KEY)))?.toString()
        }?:message.userId
    }

    private fun updateNewAndPrevUserIdInContext(
        newUserId: String, messageContext: MessageContext
    ): MessageContext {
        val newTraits =
            messageContext.traits optAdd  mapOf(
                KeyConstants.CONTEXT_ID_KEY to newUserId, KeyConstants.CONTEXT_USER_ID_KEY to newUserId
            )

        //also in case of alias, user id in context should also change, given it's
        // present there
        return messageContext.updateWith(
            traits = newTraits
        )
    }

    object KeyConstants {
        const val CONTEXT_USER_ID_KEY = "user_id"
        const val CONTEXT_USER_ID_KEY_ALIAS = "userId"
        const val CONTEXT_ID_KEY = "id"
    }

}