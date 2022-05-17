/*
 * Creator: Debanjan Chatterjee on 28/04/22, 12:26 AM Last modified: 28/04/22, 12:26 AM
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

package com.rudderstack.android.storage

import android.content.Context
import com.rudderstack.android.repository.Dao
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.core.Storage
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.rudderjsonadapter.JsonAdapter

internal class AndroidStorage(private val androidContext: Context,
                              private val jsonAdapter: JsonAdapter,
                              private val enableMultiProcess: Boolean) : Storage {
    private val dbName = "db_${androidContext.packageName}"

    companion object{
        private const val DB_VERSION = 1
    }

    private val messageDao: Dao<MessageEntity>

    private var _storageCapacity: Int = Storage.MAX_STORAGE_CAPACITY //default 2_000
    private var _maxFetchLimit: Int = Storage.MAX_FETCH_LIMIT
    init {
       RudderDatabase.init(androidContext, dbName, RudderEntityFactory(jsonAdapter),
           DB_VERSION
       )
        messageDao = RudderDatabase.getDao(MessageEntity::class.java)
        messageDao.addDataChangeListener(MessageDataListener)
    }

    override fun setStorageCapacity(storageCapacity: Int) {
        _storageCapacity = storageCapacity
    }

    override fun setMaxFetchLimit(limit: Int) {
        _maxFetchLimit = limit
    }

    override fun saveMessage(vararg messages: Message) {
        val messageEntities = messages.map {
            MessageEntity(it, jsonAdapter)
        }
    }

    override fun setBackpressureStrategy(strategy: Storage.BackPressureStrategy) {
        TODO("Not yet implemented")
    }

    override fun deleteMessages(messages: List<Message>) {
        TODO("Not yet implemented")
    }

    override fun addDataListener(listener: Storage.DataListener) {
        TODO("Not yet implemented")
    }

    override fun removeDataListener(listener: Storage.DataListener) {
        TODO("Not yet implemented")
    }

    override fun getData(offset: Int, callback: (List<Message>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getCount(callback: (Int) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getDataSync(offset: Int): List<Message> {
        TODO("Not yet implemented")
    }

    override fun cacheContext(context: Map<String, Any>) {
        TODO("Not yet implemented")
    }

    override val context: Map<String, Any>
        get() = TODO("Not yet implemented")

    override fun saveServerConfig(serverConfig: RudderServerConfig) {
        TODO("Not yet implemented")
    }

    override val serverConfig: RudderServerConfig?
        get() = TODO("Not yet implemented")

    override fun saveOptOut(optOut: Boolean) {
        TODO("Not yet implemented")
    }

    override fun saveTraits(traits: IdentifyTraits) {
        TODO("Not yet implemented")
    }

    override fun saveExternalIds(externalIds: List<Map<String, String>>) {
        TODO("Not yet implemented")
    }

    override fun clearExternalIds() {
        TODO("Not yet implemented")
    }

    override fun saveAnonymousId(anonymousId: String) {
        TODO("Not yet implemented")
    }

    override fun saveStartupMessageInQueue(message: Message) {
        TODO("Not yet implemented")
    }

    override fun clearStartupQueue() {
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

    override fun clearStorage() {
        TODO("Not yet implemented")
    }

    override val startupQueue: List<Message>
        get() = TODO("Not yet implemented")
    override val isOptedOut: Boolean
        get() = TODO("Not yet implemented")
    override val optOutTime: Long
        get() = TODO("Not yet implemented")
    override val optInTime: Long
        get() = TODO("Not yet implemented")
    override val traits: IdentifyTraits?
        get() = TODO("Not yet implemented")
    override val externalIds: List<Map<String, String>>?
        get() = TODO("Not yet implemented")
    override val anonymousId: String?
        get() = TODO("Not yet implemented")

    //message table listener
    object MessageDataListener : Dao.DataChangeListener<MessageEntity>{
        override fun onDataInserted(inserted: List<MessageEntity>, allData: List<MessageEntity>) {

        }

        override fun onDataDeleted(deleted: List<MessageEntity>, allData: List<MessageEntity>) {

        }
    }
}