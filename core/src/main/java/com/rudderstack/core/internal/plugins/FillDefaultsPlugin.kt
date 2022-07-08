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

package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Logger
import com.rudderstack.core.Plugin
import com.rudderstack.core.Settings
import com.rudderstack.core.State
import com.rudderstack.core.internal.MissingPropertiesException
import com.rudderstack.core.internal.minusWrtKeys
import com.rudderstack.models.*

/**
 * Fill the defaults for a [Message]
 * In case a message contains traits, external ids, custom contexts, this will override
 * the values in storage for the same. That means if message contains traits
 * {a:b, c:d} and saved traits contain {c:e, g:h, i:j}, the resultant will be
 * that of the message, i.e {a:b, c:d}. This is applicable to traits, external ids and custom contexts
 * The default context will be added irrespectively.
 * In case default context contains externalIds/traits/custom contexts that are common with
 * message external ids/traits/custom contexts respectively, the values will be amalgamated with
 * preference given to those belonging to message, in case keys match.
 *
 * @property defaultContext Generally a common context is expected, that remains same for all messages
 * @property settingsState
 * @property contextState
 */
internal class FillDefaultsPlugin(
    private val defaultContext: MessageContext, private val settingsState: State<Settings>,
    private val contextState: State<MessageContext>,
    private val logger : Logger
) : Plugin {

    /**
     * Fill default details for [Message]
     * If message contains context, this will replace the ones present
     * @throws [MissingPropertiesException] if neither of userId or anonymous id is present
     */
    @Throws(MissingPropertiesException::class)
    private inline fun <reified T : Message> T.withDefaults(): T {
        val anonId = this.anonymousId ?: settingsState.value?.anonymousId
        val userId = this.userId ?: settingsState.value?.userId
        if (anonId == null && userId == null) {
            val ex = MissingPropertiesException("Either Anonymous Id or User Id must be present");
            logger.error(log = "Missing both anonymous Id and user Id. Use settings to update " +
                    "anonymous id in Analytics constructor", throwable = ex)
            throw ex
        }
        //copying top level context to message context
        return (this.copy(
            context =  (
                    // in case of alias we purposefully remove traits from context
                    contextState.value?.let {
                        if(this is AliasMessage && this.userId != settingsState.value?.userId) it.updateWith(traits = mapOf()) else it
                    } selectiveReplace
                    context) optAdd  defaultContext,
            anonymousId = anonId, userId = userId
        ) as T)
    }
    private infix fun MessageContext?.selectiveReplace(context: MessageContext?) : MessageContext?{
        if(this == null) return context else if (context == null) return this


        return this.updateWith(context)
    }

    private infix fun MessageContext?.optAdd(context: MessageContext?) : MessageContext?{
        //this gets priority
        if(this == null) return context else if (context == null) return this
            val newTraits = context.traits?.let {
                (it - (this.traits?.keys?: setOf()).toSet()) optAdd this.traits
            }?: traits
            val newCustomContexts = context.customContexts?.let {
                (it - (this.customContexts?.keys?: setOf()).toSet()) optAdd this.customContexts
            }?: customContexts
            val newExternalIds = context.externalIds?.let {
                (it minusWrtKeys (this.externalIds?: listOf())) + it
            }?: externalIds

        createContext(newTraits, newExternalIds, newCustomContexts).let {
            //add the extra info from both contexts
            val extraThisContext = this - it.keys
            val extraOperandContext = context - it.keys
            return it + extraThisContext + extraOperandContext
        }
    }

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message().withDefaults()
        return chain.proceed(message)
    }
}