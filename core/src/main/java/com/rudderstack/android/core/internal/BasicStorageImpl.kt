/*
 * Creator: Debanjan Chatterjee on 30/12/21, 6:25 PM Last modified: 30/12/21, 6:25 PM
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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.Logger
import com.rudderstack.android.core.Settings
import com.rudderstack.android.core.State
import com.rudderstack.android.core.Storage
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.IdentifyTraits
import com.rudderstack.android.models.Message
import com.rudderstack.android.models.RudderServerConfig
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.log

internal class BasicStorageImpl(
    private val queue: Queue<Message> = LinkedList(),
    private val logger: Logger
) : Storage {
    private var cachedContext: Map<String,Any> = mapOf()
    private var dataChangeListeners = listOf<(List<Message>) -> Unit>()
    private var _isOptOut = false
    private var _optOutTime = -1L
    private var _optInTime = -1L

    private var _serverConfig : RudderServerConfig? = null
    private var _traits : IdentifyTraits? = null
    private var _externalIds : List<Map<String, String>>? = null
    private var _anonymousId : String? = null

    /**
     * This queue holds the messages that are generated prior to destinations waking up
     */
    private val startupQ = LinkedList<Message>()

    private val serverConfigFile = File("/temp/rudder-analytics/server_config")

    override fun saveMessage(vararg messages: Message) {
        if(queue.size >= Storage.MAX_STORAGE_CAPACITY){
            logger.warn(log = "Max storage capacity reached, dropping events")
        }else {
            queue.addAll(messages.take(Storage.MAX_STORAGE_CAPACITY - queue.size))
        }
        onDataChange()
    }

    override fun deleteMessages(messages: List<Message>) {
        queue.removeAll(messages)
        onDataChange()
    }

    override fun addDataChangeListener(listener: (data: List<Message>) -> Unit) {
        dataChangeListeners = dataChangeListeners + listener
    }

    override fun removeDataChangeListener(listener: (data: List<Message>) -> Unit) {
        dataChangeListeners = dataChangeListeners - listener
    }

    override fun getData(callback: (List<Message>) -> Unit) {
        callback.invoke(queue.take(Storage.MAX_FETCH_LIMIT).toList())
    }



    override fun cacheContext(context: Map<String, Any>) {
        cachedContext = context
    }

    override val context: Map<String, Any>
        get() = cachedContext

    override fun saveServerConfig(serverConfig: RudderServerConfig) {
        try {
            if (!serverConfigFile.exists()) {
                serverConfigFile.parentFile.mkdirs()
                serverConfigFile.createNewFile()
            }
            val fos = FileOutputStream(serverConfigFile)
            val oos = ObjectOutputStream(fos)

            oos.writeObject(serverConfig)
            oos.flush()
            fos.close()

        }catch (ex : Exception){
            logger.error(log = "Server Config cannot be saved", throwable = ex)
        }
    }

    override fun saveOptOut(optOut: Boolean) {
         _isOptOut = optOut
        if(optOut){
            _optOutTime = System.currentTimeMillis()
        }else
            _optInTime = System.currentTimeMillis()
    }

    override fun saveTraits(traits: IdentifyTraits) {
        this._traits = traits
    }

    override fun saveExternalIds(externalIds: List<Map<String, String>>) {
        _externalIds = externalIds
    }

    override fun clearExternalIds() {
        _externalIds = listOf()
    }

    override fun saveAnonymousId(anonymousId: String) {
        _anonymousId = anonymousId
    }

    override fun saveStartupMessageInQueue(message: Message) {
        startupQ.add(message)
    }

    override fun clearStartupQueue() {
        startupQ.clear()
    }

    override fun shutdown() {
        queue.clear()
    }


    override val serverConfig: RudderServerConfig?
        get() = _serverConfig

    override val startupQueue: List<Message>
        get() = startupQ

    override val isOptedOut: Boolean
        get() = _isOptOut
    override val optOutTime: Long
        get() = _optOutTime
    override val optInTime: Long
        get() = _optInTime
    override val traits: IdentifyTraits?
        get() = _traits
    override val externalIds: List<Map<String, String>>?
        get() = _externalIds
    override val anonymousId: String?
        get() = _anonymousId

    private fun onDataChange(){
        val msgs = queue.take(Storage.MAX_FETCH_LIMIT).toList()
        dataChangeListeners.forEach {
            it.invoke(msgs)
        }
    }
}