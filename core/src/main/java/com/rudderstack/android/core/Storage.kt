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
 *
 * Since we intend to sync the database at regular intervals, hence data volumes handled by storage
 * is pretty low.
 * That is the reason are avoiding any selection arguments.
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

    /**
     * Add a data change listener that calls back when any changes to data is made.
     *
     * @param listener callback for the changed data
     */
    fun addDataChangeListener(listener : (data : List<Message>) -> Unit)

    fun removeDataChangeListener(listener : (data : List<Message>) -> Unit)
    /**
     * Asynchronous method to get the entire data present
     *
     * @param callback returns the list of [Message]
     */
    fun getData(callback : (List<Message>) -> Unit)
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
     * [DestinationPlugin]s in general start up asynchronously, which means messages sent
     * prior to their initialization might get dropped. Thus to counter that, [Message] objects
     * received prior to all Destination Plugins initialization are stored in startup queue and
     * replayed back.
     * Here developers can provide their custom implementation of the startup queue.
     * @param message [Message] objects that are being sent to destination plugins prior to their
     * startup
     */
    fun saveStartupMessageInQueue(message: Message)

    /**
     * @see saveStartupMessageInQueue
     * get the messages that were posted before all device mode destinations initialized.
     */
    val startupQueue : List<Message>

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