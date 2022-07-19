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
import android.os.Build
import com.rudderstack.android.android.BuildConfig
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.internal.RudderPreferenceManager
import com.rudderstack.android.repository.Dao
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.core.Logger
import com.rudderstack.core.Storage
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

internal class AndroidStorage(
    private val androidContext: Context,
    private val jsonAdapter: JsonAdapter,
    useContentProvider: Boolean,
    private val logger: Logger = AndroidLogger
) : Storage {
    private val dbName = "db_${androidContext.packageName}"

    companion object {
        private const val DB_VERSION = 1
        private const val SERVER_CONFIG_FILE_NAME = "server_config"

        private const val CONTEXT_FILE_NAME = "pref_context"

        //file access
        /**
         * Saves a serializable object in file
         *
         * @param T
         * @param obj
         * @param context
         * @param fileName
         * @return
         */
        private fun <T : Serializable> saveObject(
            obj: T,
            context: Context,
            fileName: String
        ): Boolean {
            try {
                val fos: FileOutputStream = context.openFileOutput(
                    fileName,
                    Context.MODE_PRIVATE
                )
                val os = ObjectOutputStream(fos)
                os.writeObject(obj)
                os.close()
                fos.close()
                return true
            } catch (e: Exception) {
                AndroidLogger.error(
                    log = "save object: Exception while saving Object to File",
                    throwable = e
                )
                e.printStackTrace()
            }
            return false
        }

        /**
         *
         *
         * @param T
         * @param context
         * @param fileName
         * @return
         */
        private fun <T : Serializable> getObject(context: Context, fileName: String): T? {
            try {
                val file = context.getFileStreamPath(fileName)
                if (file != null && file.exists()
                ) {
                    val fis =
                        context.openFileInput(fileName)
                    val `is` = ObjectInputStream(fis)
                    val obj = `is`.readObject() as? T?
                    `is`.close()
                    fis.close()
                    return obj
                }
            } catch (e: Exception) {
                AndroidLogger.error(
                    log = "getObject: Failed to read Object from File",
                    throwable = e
                )
                e.printStackTrace()
            }
            return null
        }

    }

    private val messageDao: Dao<MessageEntity>

    private var _storageCapacity: Int = Storage.MAX_STORAGE_CAPACITY //default 2_000
    private var _maxFetchLimit: Int = Storage.MAX_FETCH_LIMIT
    private var _storageListeners = listOf<Storage.DataListener>()

    private var _backPressureStrategy = Storage.BackPressureStrategy.Drop

    //this holds the count of rows in DB, this helps with the back pressure strategy
    //we update this once on init and then on data change listener
    private val _dataCount = AtomicLong()

    private val _optOut = AtomicBoolean(false)
    private val _optOutTime = AtomicLong(0L)
    private val _optInTime = AtomicLong(System.currentTimeMillis())

    private var _anonymousId: String? = null

    /**
     * This queue holds the messages that are generated prior to destinations waking up
     */
    private val startupQ = LinkedList<Message>()

    private var _cachedContext: MessageContext? = null

    //message table listener
    private val _messageDataListener = object : Dao.DataChangeListener<MessageEntity> {
        override fun onDataInserted(inserted: List<MessageEntity>, allData: List<MessageEntity>) {
            onDataChange(allData.size.toLong())
        }

        override fun onDataDeleted(deleted: List<MessageEntity>, allData: List<MessageEntity>) {
            onDataChange(allData.size.toLong())
        }

        private fun onDataChange(dataSize: Long) {
            _dataCount.set(dataSize)
            _storageListeners.forEach {
                it.onDataChange()
            }
        }
    }

    init {
        RudderDatabase.init(
            androidContext, dbName, RudderEntityFactory(jsonAdapter), useContentProvider,
            DB_VERSION
        )
        messageDao = RudderDatabase.getDao(MessageEntity::class.java)
        messageDao.addDataChangeListener(_messageDataListener)

    }

    override fun setStorageCapacity(storageCapacity: Int) {
        _storageCapacity = storageCapacity
    }

    override fun setMaxFetchLimit(limit: Int) {
        _maxFetchLimit = limit
    }

    override fun saveMessage(vararg messages: Message) {
        //handle backpressure TODO
        val block = { count: Long ->
            val excessMessages = count + messages.size - _storageCapacity
            val messageEntities = if (excessMessages > 0) {
                when (_backPressureStrategy) {
                    Storage.BackPressureStrategy.Drop -> {
                        messages.dropLast(excessMessages.toInt()).map {
                            MessageEntity(it, jsonAdapter)
                        }
                    }
                    Storage.BackPressureStrategy.Latest -> {
                        messageDao.delete(
                            "${MessageEntity.ColumnNames.messageId} IN (" +
                                    //COMMAND FOR SELECTING FIRST $excessMessages to be removed from DB
                                    "SELECT ${MessageEntity.ColumnNames.messageId} FROM ${MessageEntity.TABLE_NAME} " +
                                    "ORDER BY ${MessageEntity.ColumnNames.timestamp} LIMIT $excessMessages",
                            null
                        ) {}
                        messages.map {
                            MessageEntity(it, jsonAdapter)
                        }
                    }
                }
            } else
                messages.map {
                    MessageEntity(it, jsonAdapter)
                }
            with(messageDao) {
                messageEntities.insert {
                }
            }
        }
        if (_dataCount.get() > 0) {
            block.invoke(_dataCount.get())
        } else {
            messageDao.getCount {
                _dataCount.set(it)
                block.invoke(it)
            }
        }
    }

    override fun setBackpressureStrategy(strategy: Storage.BackPressureStrategy) {
        _backPressureStrategy = strategy
    }

    override fun deleteMessages(messages: List<Message>) {
        with(messageDao) {
            messages.entities.delete { }
        }
    }

    override fun addDataListener(listener: Storage.DataListener) {
        _storageListeners = _storageListeners + listener
    }

    override fun removeDataListener(listener: Storage.DataListener) {
        _storageListeners = _storageListeners - listener
    }

    override fun getData(offset: Int, callback: (List<Message>) -> Unit) {
        messageDao.runGetQuery(limit = "$offset,$_maxFetchLimit") {
            callback.invoke(it.map { it.message })
        }
    }

    override fun getCount(callback: (Long) -> Unit) {
        messageDao.getCount(callback = callback)
    }

    override fun getDataSync(offset: Int): List<Message> {
        return messageDao.runGetQuerySync(
            null, null, null, null,
            "$offset,$_maxFetchLimit"
        )?.map {
            it.message
        } ?: listOf()
    }

    override fun cacheContext(context: MessageContext) {
        context.save()
    }

    override val context: MessageContext?
        get() = (if (_cachedContext == null) {
            _cachedContext = getObject<HashMap<String, Any?>>(androidContext, CONTEXT_FILE_NAME)
            _cachedContext
        } else _cachedContext)

    //for local caching
    private var _serverConfig: RudderServerConfig? = null
    override fun saveServerConfig(serverConfig: RudderServerConfig) {
        synchronized(this) {
            _serverConfig = serverConfig
            saveObject(serverConfig, context = androidContext, SERVER_CONFIG_FILE_NAME)
        }
    }

    override val serverConfig: RudderServerConfig?
        get() = synchronized(this) {
            if (_serverConfig == null) _serverConfig =
                getObject(androidContext, SERVER_CONFIG_FILE_NAME)
            _serverConfig
        }

    override fun saveOptOut(optOut: Boolean) {
        _optOut.set(optOut)
        if (optOut) {
            _optOutTime.set(System.currentTimeMillis())
        } else {
            _optInTime.set(System.currentTimeMillis())
        }
    }

    override fun saveAnonymousId(anonymousId: String) {
        _anonymousId = anonymousId
        RudderPreferenceManager.saveAnonymousId(anonymousId)
    }

    override fun saveStartupMessageInQueue(message: Message) {
        startupQ.add(message)
    }

    override fun clearStartupQueue() {
        startupQ.clear()
    }

    override fun shutdown() {
        //nothing much to do here
       }

    override fun clearStorage() {
        startupQ.clear()
        messageDao.delete(null, null)
    }

    override val startupQueue: List<Message>
        get() = startupQ
    override val isOptedOut: Boolean
        get() = _optOut.get()
    override val optOutTime: Long
        get() = _optOutTime.get()
    override val optInTime: Long
        get() = _optInTime.get()
    override val anonymousId: String?
        get() {
            if (_anonymousId == null) {
                _anonymousId = RudderPreferenceManager.anonymousId
            }
            return _anonymousId
        }
    override val libraryName: String
        get() = BuildConfig.LIBRARY_PACKAGE_NAME
    override val libraryVersion: String
        get() = BuildConfig.LIBRARY_VERSION_NAME
    override val libraryPlatform: String
        get() = "Android"
    override val libraryOsVersion: String
        get() = Build.VERSION.SDK_INT.toString()

    private val Iterable<Message>.entities
        get() = map {
            it.entity
        }
    private val Message.entity
        get() = MessageEntity(this, jsonAdapter)

    private fun MessageContext.save() {
        saveObject(HashMap(this), androidContext, CONTEXT_FILE_NAME)
    }


}