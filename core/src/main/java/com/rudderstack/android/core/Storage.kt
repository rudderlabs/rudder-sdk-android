/*
 * Creator: Debanjan Chatterjee on 30/12/21, 1:28 PM Last modified: 30/12/21, 1:28 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.android.core

import com.rudderstack.android.models.Message

/**
 * Requires specific implementation of how storage should be handled.
 * Can be customised according to requirements.
 */
interface Storage {
    /**
     * Platform specific implementation for saving [Message]
     *
     * @param messages A single or multiple messages to be saved
     */
    fun saveMessage(vararg messages: Message)

    /**
     * Delete Messages, preferably when no longer required
     *
     * @param messages [Message] objects ready to be removed from storage
     */
    fun deleteMessages(messages: List<Message>)
    //get messages not yet done

//    fun getMessages() : List<Message>

    /**
     * Platform specific implementation of caching context. This can be done locally too.
     *
     * @param context A map representing the context. Refer to [Message]
     */
    fun cacheContext(context : Map<String, String>)

    /**
     * Retrieve the cached context
     */
    val context : Map<String, String>

    /**
     * Platform specific implementation of saving opt out choice.
     *
     * @param optOut Save opt out state
     */
    fun saveOptOut(optOut : Boolean)

    /**
     * Get opted out state
     */
    val isOptedOut : Boolean

    /**
     * Returns opt out time instant if any else -1L
     */
    val optOutTime : Long
    /**
     * Returns opt in time instant if any else -1L
     */
    val optInTime : Long
}