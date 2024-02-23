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

import android.app.Application
import android.content.Context
import android.os.Build
import com.rudderstack.android.BuildConfig
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.internal.RudderPreferenceManager
import com.rudderstack.android.repository.Dao
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
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
import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class AndroidStorageImpl(
    private val application: Application,
    private val useContentProvider: Boolean = false,
    private val instanceName: String,
    private val storageExecutor: ExecutorService = Executors.newSingleThreadExecutor()
) : AndroidStorage {
    private var logger: Logger? = null
    private val Application.dbName get() = "db_${packageName}_$instanceName"
    private var jsonAdapter: JsonAdapter? = null


    private var preferenceManager : RudderPreferenceManager? = null
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
            obj: T, context: Context, fileName: String, logger: Logger? = AndroidLogger
        ): Boolean {
            try {
                val fos: FileOutputStream = context.openFileOutput(
                    fileName, Context.MODE_PRIVATE
                )
                val os = ObjectOutputStream(fos)
                os.writeObject(obj)
                os.close()
                fos.close()
                return true
            } catch (e: Exception) {
                logger?.error(
                    log = "save object: Exception while saving Object to File", throwable = e
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
        private fun <T : Serializable> getObject(
            context: Context, fileName: String, logger: Logger? = AndroidLogger
        ): T? {
            try {
                val file = context.getFileStreamPath(fileName)
                if (file != null && file.exists()) {
                    val fis = context.openFileInput(fileName)
                    val `is` = ObjectInputStream(fis)
                    val obj = `is`.readObject() as? T?
                    `is`.close()
                    fis.close()
                    return obj
                }
            } catch (e: Exception) {
                logger?.error(
                    log = "getObject: Failed to read Object from File", throwable = e
                )
                e.printStackTrace()
            }
            return null
        }

    }

    private val serverConfigFileName
        get() = "$SERVER_CONFIG_FILE_NAME-{instanceName}"
    private val contextFileName
        get() = "$CONTEXT_FILE_NAME-{instanceName}"

    private var messageDao: Dao<MessageEntity>? = null

    private var _storageCapacity: Int = Storage.MAX_STORAGE_CAPACITY //default 2_000
    private var _maxFetchLimit: Int = Storage.MAX_FETCH_LIMIT
    private var _storageListeners = setOf<Storage.DataListener>()

    private var _backPressureStrategy = Storage.BackPressureStrategy.Drop

    //this holds the count of rows in DB, this helps with the back pressure strategy
    //we update this once on init and then on data change listener
    private val _dataCount = AtomicLong()

    private val _optOut = AtomicBoolean(false)
    private val _optOutTime = AtomicLong(0L)
    private val _optInTime = AtomicLong(System.currentTimeMillis())

    private var _anonymousId: String? = null
    private var _userId: String? = null

    /**
     * This queue holds the messages that are generated prior to destinations waking up
     */
    private val startupQ = LinkedList<Message>()

    private var _cachedContext: MessageContext? = null

    private var rudderDatabase: RudderDatabase? = null

//    private val _configSubscriber =
//        ::onConfigChange

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


    private fun initDb(analytics: Analytics) {
        rudderDatabase = RudderDatabase(
            application, application.dbName,//TODO: change this to instance name
            RudderEntityFactory(analytics), useContentProvider, DB_VERSION, executorService =
            storageExecutor
        )
        messageDao = rudderDatabase?.getDao(MessageEntity::class.java, storageExecutor)
        messageDao?.addDataChangeListener(_messageDataListener)

    }

    override fun updateConfiguration(configuration: Configuration) {
        super.updateConfiguration(configuration)
        jsonAdapter = configuration.jsonAdapter
        logger = configuration.logger
    }

    override fun setStorageCapacity(storageCapacity: Int) {
        _storageCapacity = storageCapacity
    }

    override fun setMaxFetchLimit(limit: Int) {
        _maxFetchLimit = limit
    }

    override fun saveMessage(vararg messages: Message) {
        val block = { currentCount: Long ->
            val excessMessages = currentCount + messages.size - _storageCapacity
            if (excessMessages > 0) {
                when (_backPressureStrategy) {
                    Storage.BackPressureStrategy.Drop -> {
                        messages.dropLast(excessMessages.toInt())
                            .saveToDb() //count can never exceed storage cap,
                        //hence excess messages cannot be greater than messages.size
                    }

                    Storage.BackPressureStrategy.Latest -> {

                        messageDao?.delete(
                            "${MessageEntity.ColumnNames.messageId} IN (" +
                            //COMMAND FOR SELECTING FIRST $excessMessages to be removed from DB
                            "SELECT ${MessageEntity.ColumnNames.messageId} FROM ${MessageEntity
                                .TABLE_NAME} " + "ORDER BY ${MessageEntity.ColumnNames.updatedAt} LIMIT $excessMessages)",
                            null
                        ) {
                            //check messages exceed storage cap
                            (if (messages.size > _storageCapacity) {
                                messages.drop(messages.size - _storageCapacity)
                            } else messages.toList()).saveToDb()
                        }
                    }
                }
            } else messages.toList().saveToDb()

        }
        if (_dataCount.get() > 0) {
            block.invoke(_dataCount.get())
        } else {
            messageDao?.getCount {
                _dataCount.set(it)
                block.invoke(it)
            }
        }
    }

    private fun List<Message>.saveToDb() {
        val jsonAdapter = jsonAdapter ?: return
        map {
            MessageEntity(it, jsonAdapter)
        }.apply {
            with(messageDao ?: return) {
                insert(conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_REPLACE) { it ->
                }
            }
        }
    }

    override fun setBackpressureStrategy(strategy: Storage.BackPressureStrategy) {
        _backPressureStrategy = strategy
    }

    override fun deleteMessages(messages: List<Message>) {
        with(messageDao ?: return) {
            messages.entities.filterNotNull().delete { }
        }
    }

    override fun addMessageDataListener(listener: Storage.DataListener) {
        _storageListeners = _storageListeners + listener
    }

    override fun removeMessageDataListener(listener: Storage.DataListener) {
        _storageListeners = _storageListeners - listener
    }

    override fun getData(offset: Int, callback: (List<Message>) -> Unit) {
        messageDao?.runGetQuery(limit = "$offset,$_maxFetchLimit") {
            callback.invoke(it.map { it.message })
        }
    }

    override fun getCount(callback: (Long) -> Unit) {
        messageDao?.getCount(callback = callback)
    }

    override fun getDataSync(offset: Int): List<Message> {
        return messageDao?.runGetQuerySync(
            null, null, null, null, "$offset,$_maxFetchLimit"
        )?.map {
            it.message
        } ?: listOf()
    }

    override fun cacheContext(context: MessageContext) {
        context.save()
    }

    override val context: MessageContext?
        get() = (if (_cachedContext == null) {
            _cachedContext =
                getObject<HashMap<String, Any?>>(application, contextFileName, logger)
            _cachedContext
        } else _cachedContext)


    //for local caching
    private var _serverConfig: RudderServerConfig? = null
    override fun saveServerConfig(serverConfig: RudderServerConfig) {
        synchronized(this) {
            _serverConfig = serverConfig
            saveObject(
                serverConfig, context = application, serverConfigFileName, logger
            )
        }
    }

    override val serverConfig: RudderServerConfig?
        get() = synchronized(this) {
            if (_serverConfig == null) _serverConfig =
                getObject(application, serverConfigFileName, logger)
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


    override fun saveStartupMessageInQueue(message: Message) {
        startupQ.add(message)
    }

    override fun clearStartupQueue() {
        startupQ.clear()
    }

    override fun shutdown() {
        rudderDatabase?.shutDown()
        _dataCount.set(0)
    }

    override fun clearStorage() {
        startupQ.clear()
        messageDao?.delete(null, null)
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
                _anonymousId = preferenceManager?.anonymousId
            }
            return _anonymousId
        }
    override val userId: String?
        get() {
            if (_userId == null) {
                _userId = preferenceManager?.userId
            }
            return _userId
        }
    override val sessionId: Long?
        get() = preferenceManager?.sessionId?.takeIf { it > -1L }
    override val lastActiveTimestamp: Long?
        get() = preferenceManager?.lastActiveTimestamp?.takeIf { it > -1L }

    override fun setAnonymousId(anonymousId: String) {
        _anonymousId = anonymousId
        preferenceManager?.saveAnonymousId(anonymousId)
    }

    override fun setUserId(userId: String) {
        _userId = userId
        preferenceManager?.saveAnonymousId(anonymousId)
    }

    override fun setSessionId(sessionId: Long) {
        preferenceManager?.saveSessionId(sessionId)
    }

    override fun saveLastActiveTimestamp(timestamp: Long) {
        preferenceManager?.saveLastActiveTimestamp(timestamp)
    }

    override fun clearSessionId() {
        preferenceManager?.clearSessionId()
    }

    override fun clearLastActiveTimestamp() {
        preferenceManager?.clearLastActiveTimestamp()
    }

    override val libraryName: String
        get() = BuildConfig.LIBRARY_PACKAGE_NAME
    override val libraryVersion: String
        get() = BuildConfig.LIBRARY_VERSION_NAME
    override val libraryPlatform: String
        get() = "Android"
    override val libraryOsVersion: String
        get() = Build.VERSION.SDK_INT.toString()

    override fun setup(analytics: Analytics) {
        initDb(analytics)
        preferenceManager = RudderPreferenceManager(application, analytics.instanceName)
    }

    private val Iterable<Message>.entities
        get() = map {
            it.entity
        }
    private val Message.entity
        get() = jsonAdapter?.let { MessageEntity(this, it) }

    private fun MessageContext.save() {
        saveObject(
            HashMap(this), application, contextFileName, logger
        )
    }
}