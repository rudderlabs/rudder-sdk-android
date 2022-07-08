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

package com.rudderstack.core

import com.rudderstack.models.IdentifyTraits
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext
import com.rudderstack.models.RudderServerConfig

/**
 * Do not call these methods directly
 * These are meant to be called from implementation of [Controller]
 * Requires specific implementation of how storage should be handled.
 * Can be customised according to requirements.
 * For custom modifications over queue based implementation one can extend [BasicStorageImpl]
 * Since we intend to sync the database at regular intervals, hence data volumes handled by storage
 * is pretty low.
 * That is the reason are avoiding any selection arguments.
 */
interface Storage {
    companion object {
        const val MAX_STORAGE_CAPACITY = 2_000
        const val MAX_FETCH_LIMIT = 5_00
    }

    /**
     * The max number of events that can be stored.
     * Defaults to [MAX_STORAGE_CAPACITY]
     * If storage overflows beyond this, data retention should be based on [BackPressureStrategy]
     * @see setBackpressureStrategy
     * @param storageCapacity
     */
    fun setStorageCapacity(storageCapacity: Int = MAX_STORAGE_CAPACITY)

    /**
     * The max number of [Message] that can be fetched at one go.
     * Applies to [getData] and [DataListener.onDataChange]
     * @param limit The number of messages to be set as limit, defaults to [MAX_FETCH_LIMIT]
     */
    fun setMaxFetchLimit(limit: Int = MAX_FETCH_LIMIT)

    /**
     * Platform specific implementation for saving [Message]
     * Is called from the same executor the plugins are processed on.
     *
     * @param messages A single or multiple messages to be saved
     */
    fun saveMessage(vararg messages: Message)

    /**
     * Default back pressure strategy is [BackPressureStrategy.Drop]
     *
     * @param strategy [BackPressureStrategy] for queueing [Message]
     */
    fun setBackpressureStrategy(strategy: BackPressureStrategy)

    /**
     * Delete Messages, preferably when no longer required
     *
     * @param messages [Message] objects ready to be removed from storage
     */
    fun deleteMessages(messages: List<Message>)

    /**
     * Add a data change listener that calls back when any changes to data is made.
     * @see DataListener
     *
     * @param listener callback for the changed data
     */
    fun addDataListener(listener: DataListener)

    fun removeDataListener(listener: DataListener)

    /**
     * Asynchronous method to get the entire data present to a maximum of fetch limit
     * @see setMaxFetchLimit
     *
     * @param offset offset the fetch by the given amount, i.e elements from offset to size-1
     * @param callback returns the list of [Message]
     */
    fun getData(offset: Int = 0, callback: (List<Message>) -> Unit)

    /**
     * Data count, analogous to count(*)
     *
     * @param callback
     */
    fun getCount(callback : (Long) -> Unit)

    /**
     * synchronous method to get the entire data present to a maximum of fetch limit
     * @see setMaxFetchLimit
     * @param offset offset the fetch by the given amount, i.e elements from offset to size-1
     *
     * @return the list of messages.
     */
    fun getDataSync(offset: Int = 0): List<Message>

   /**
     * Platform specific implementation of caching context. This can be done locally too.
     *
     * @param context A map representing the context. Refer to [Message]
     */
    fun cacheContext(context: MessageContext)


    /**
     * Retrieve the cached context
     */
    val context: MessageContext?

    /**
     * CPU extensive operation, better to offload to a different thread
     *
     * @param serverConfig SDK initialization data [RudderServerConfig]
     */
    fun saveServerConfig(serverConfig: RudderServerConfig)

    /**
     * CPU intensive operation. Might involve file access.
     */
    val serverConfig: RudderServerConfig?

    /**
     * Platform specific implementation of saving opt out choice.
     *
     * @param optOut Save opt out state
     */
    fun saveOptOut(optOut: Boolean)

    /**
     * Traits from identify call are persisted
     *
     * @param traits A map of user traits sent through Identify call
     */
//    fun saveTraits(traits: IdentifyTraits)

    /**
     * External Ids from identify call are persisted. Subsequent identify calls will replace the data
     *
     * @param externalIds Ids specifically targeted for different destinations.
     */
//    fun saveExternalIds(externalIds: List<Map<String, String>>)

    /**
     * clear all external ids present
     *
     */
//    fun clearExternalIds()

    /**
     * Persist anonymous Id for the app Instance.
     * This is generated by default but can be set externally too.
     *
     * @param anonymousId String that's unique for this app instance
     */
    fun saveAnonymousId(anonymousId: String)

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
     * Clear startup queue
     *
     */
    fun clearStartupQueue()

    /**
     * Shutdown the process
     *
     */
    fun shutdown()

    /**
     * Clears any and every persistent storage.
     * Depending on the implementation one can clear the database or
     * remove preferences
     *
     */
    fun clearStorage()

    /**
     * @see saveStartupMessageInQueue
     * get the messages that were posted before all device mode destinations initialized.
     */
    val startupQueue: List<Message>

    /**
     * Get opted out state
     */
    val isOptedOut: Boolean

    /**
     * Returns opt out time instant if any else -1L
     */
    val optOutTime: Long

    /**
     * Returns opt in time instant if any else -1L
     */
    val optInTime: Long

    /**
     * Returns traits
     */
//    val traits: IdentifyTraits?

    /**
     * external ids persisted through identify calls.
     * Any external ids passed in other events will be merged with this.
     */
//    val externalIds: List<Map<String, String>>?

    /**
     * Anonymous id associated to the app instance
     */
    val anonymousId: String?

    val libraryName : String
    val libraryVersion : String
    val libraryPlatform : String
    val libraryOsVersion : String

    /**
     * Data Listener for data change and on data dropped.
     * Data Change listener is called whenever there's a change in data count
     *
     *
     */
    interface DataListener {
        /**
         * Called whenever there's a change in data count
         *
         */
        fun onDataChange()

        /**
         * Called when data is dropped to adhere to the backpressure strategy.
         * @see [BackPressureStrategy]
         * @see setBackpressureStrategy
         *
         * @param messages List of messages that have been dropped
         * @param error [Throwable] to enhance on the reason
         */
        fun onDataDropped(messages: List<Message>, error: Throwable)
    }

    /**
     * Back pressure strategy for handling message queue
     *
     */
    enum class BackPressureStrategy {
        /**
         * Messages are dropped in case queue is full
         *
         */
        Drop,

        /**
         * Keeps only the most recent item, oldest ones are dropped
         *
         */
        Latest
    }
}