/*
 * Creator: Debanjan Chatterjee on 06/12/23, 1:02 pm Last modified: 06/12/23, 1:02 pm
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

package com.rudderstack.testcommon

import com.rudderstack.core.Analytics
import com.rudderstack.core.Storage
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig

class VerificationStorage : Storage {

    private var storageQ = mutableListOf<Message>()
    private var _serverConfig : RudderServerConfig? = null
    override fun setStorageCapacity(storageCapacity: Int) {
        /* No-op . */
    }

    override fun setMaxFetchLimit(limit: Int) {
        /* No-op . */
    }

    override fun saveMessage(vararg messages: Message) {

    storageQ.addAll(messages)
    }

    override fun setBackpressureStrategy(strategy: Storage.BackPressureStrategy) {
        /* No-op . */
    }

    override fun deleteMessages(messages: List<Message>) {
        // does not support async delete
        deleteMessagesSync(messages)
    }

    override fun addMessageDataListener(listener: Storage.DataListener) {
        /* No-op . */
    }

    override fun removeMessageDataListener(listener: Storage.DataListener) {
        /* No-op . */
    }

    override fun getData(offset: Int, callback: (List<Message>) -> Unit) {
        callback.invoke(storageQ.toList())
    }

    override fun getCount(callback: (Long) -> Unit) {
        callback.invoke(storageQ.size.toLong())
    }

    override fun getDataSync(offset: Int): List<Message> {
        return storageQ.toList()
    }

    override fun saveServerConfig(serverConfig: RudderServerConfig) {
        /* No-op . */
    }

    override val serverConfig: RudderServerConfig?
        get() = _serverConfig

    override fun saveOptOut(optOut: Boolean) {
        /* No-op . */
    }

    override fun saveStartupMessageInQueue(message: Message) {
        /* No-op . */
    }

    override fun clearStartupQueue() {
        /* No-op . */
    }

    override fun shutdown() {
        /* No-op . */
    }

    override fun clearStorage() {
        storageQ.clear()
    }

    override fun deleteMessagesSync(messages: List<Message>) {
        storageQ -= messages.toSet()
    }

    override val startupQueue: List<Message>
        get() = /* No-op . */ listOf()
    override val isOptedOut: Boolean
        get() = false
    override val optOutTime: Long
        get() = 0L
    override val optInTime: Long
        get() = 0L
    override val libraryName: String
        get() = "Rudder-Test-Library"
    override val libraryVersion: String
        get() = 1.0.toString()
    override val libraryPlatform: String
        get() = "Android"
    override val libraryOsVersion: String
        get() = "13"

    override fun setup(analytics: Analytics) {
        /* No-op . */
    }

}