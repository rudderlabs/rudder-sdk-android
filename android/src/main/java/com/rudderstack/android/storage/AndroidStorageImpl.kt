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
import android.os.Build
import android.util.Log
import com.rudderstack.android.BuildConfig
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
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

private const val DB_VERSION = 1
private const val SERVER_CONFIG_FILE_NAME = "RudderServerConfig"

private const val CONTEXT_FILE_NAME = "pref_context"
private const val V1_RUDDER_FLUSH_CONFIG_FILE_NAME = "RudderFlushConfig"

class AndroidStorageImpl(
    private val application: Application,
    private val useContentProvider: Boolean = false,
    private val writeKey: String,
    private val storageExecutor: ExecutorService = Executors.newSingleThreadExecutor()
) : AndroidStorage {

    private var logger: Logger? = null
    private val dbName get() = "rs_persistence_$writeKey"
    private var jsonAdapter: JsonAdapter? = null

    private var preferenceManager: RudderPreferenceManager? = null

    private val serverConfigFileName
        get() = "$SERVER_CONFIG_FILE_NAME-{$writeKey}"
    private val contextFileName
        get() = "$CONTEXT_FILE_NAME-{$writeKey}"

    private var messageDao: Dao<MessageEntity>? = null

    private var _storageCapacity: Int = Storage.MAX_STORAGE_CAPACITY //default 2_000
    private var _maxFetchLimit: Int = Storage.MAX_FETCH_LIMIT
    private var _storageListeners = setOf<Storage.DataListener>()

    private var _backPressureStrategy = Storage.BackPressureStrategy.Drop

    //this holds the count of rows in DB, this helps with the back pressure strategy
    //we update this once on init and then on data change listener
    private val _dataCount = AtomicLong()

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

    //message table listener
    private val _messageDataListener = object : Dao.DataChangeListener<MessageEntity> {
        override fun onDataInserted(inserted: List<MessageEntity>) {
            onDataChange()
        }

        override fun onDataDeleted(deleted: List<MessageEntity>) {
            onDataChange()
        }

        private fun onDataChange() {
            messageDao?.getCount {
                _dataCount.set(it)
                _storageListeners.forEach {
                    it.onDataChange()
                }
            }
        }
    }

    private fun initDb(analytics: Analytics) {
        rudderDatabase = RudderDatabase(
            application,
            dbName,
            RudderEntityFactory(analytics.jsonAdapter),
            useContentProvider,
            DB_VERSION,
            providedExecutorService = storageExecutor
        )
        jsonAdapter = analytics.jsonAdapter
        messageDao = rudderDatabase?.getDao(MessageEntity::class.java, storageExecutor)
        messageDao?.addDataChangeListener(_messageDataListener)

    }

    override fun updateConfiguration(configuration: Configuration) {
        super.updateConfiguration(configuration)
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
                                    "SELECT ${MessageEntity.ColumnNames.messageId} FROM ${
                                        MessageEntity
                                            .TABLE_NAME
                                    } " + "ORDER BY ${MessageEntity.ColumnNames.updatedAt} LIMIT $excessMessages)",
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
        messageDao?.runGetQuery(
            limit = "$offset,$_maxFetchLimit",
            orderBy = MessageEntity.ColumnNames.updatedAt
        ) {
            callback.invoke(it.map { it.message })
        }
    }

    override fun getCount(callback: (Long) -> Unit) {
        messageDao?.getCount(callback = callback)
    }

    override fun getDataSync(offset: Int): List<Message> {
        return messageDao?.runGetQuerySync(
            null, null, null, MessageEntity.ColumnNames.updatedAt,
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
            _cachedContext = getObject<HashMap<String, Any?>>(application, contextFileName, logger)
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
        preferenceManager?.saveOptStatus(optOut)
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

    override fun deleteMessagesSync(messages: List<Message>) {
        with(messageDao ?: return) {
            messages.entities.filterNotNull().deleteSync()
        }
    }

    override val startupQueue: List<Message>
        get() = startupQ
    override val isOptedOut: Boolean
        get() = preferenceManager?.optStatus?:false
    override val optOutTime: Long
        get() = _optOutTime.get()
    override val optInTime: Long
        get() = _optInTime.get()
    override val v1OptOut: Boolean
        get() = preferenceManager?.v1optOutStatus ?: false
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
    override val advertisingId: String?
        get() = preferenceManager?.advertisingId
    override val v1AnonymousId: String?
        get() = preferenceManager?.v1AnonymousId
    override val v1SessionId: Long?
        get() = preferenceManager?.v1SessionId?.takeIf { it > -1L }
    override val v1LastActiveTimestamp: Long?
        get() = preferenceManager?.v1LastActiveTimestamp?.takeIf { it > -1L }
    override val v1Traits: Map<String, Any?>?
        get() = preferenceManager?.v1Traits?.let {
            jsonAdapter?.readJson(it, object : RudderTypeAdapter<Map<String, Any?>>() {})
        }
    override val v1ExternalIds: List<Map<String, String>>?
        get() = preferenceManager?.v1ExternalIdsJson?.let {
            jsonAdapter?.readJson(it, object : RudderTypeAdapter<List<Map<String, String>>>() {})
        }
    override val v1AdvertisingId: String?
        get() = preferenceManager?.v1AdvertisingId
    override val trackAutoSession: Boolean
        get() = preferenceManager?.trackAutoSession?: false
    override val build: Int?
        get() = preferenceManager?.build
    override val v1Build: Int?
        get() = preferenceManager?.v1Build
    override val versionName: String?
        get() = preferenceManager?.versionName
    override val v1VersionName: String?
        get() = preferenceManager?.v1VersionName

    override fun setAnonymousId(anonymousId: String) {
        _anonymousId = anonymousId
        preferenceManager?.saveAnonymousId(anonymousId)
    }

    override fun setUserId(userId: String) {
        _userId = userId
        preferenceManager?.saveUserId(userId)
    }

    override fun setSessionId(sessionId: Long) {
        preferenceManager?.saveSessionId(sessionId)
    }

    override fun setTrackAutoSession(trackAutoSession: Boolean) {
        preferenceManager?.saveTrackAutoSession(trackAutoSession)
    }

    override fun saveLastActiveTimestamp(timestamp: Long) {
        preferenceManager?.saveLastActiveTimestamp(timestamp)
    }

    override fun saveAdvertisingId(advertisingId: String) {
        preferenceManager?.saveAdvertisingId(advertisingId)
    }

    override fun clearSessionId() {
        preferenceManager?.clearSessionId()
    }

    override fun clearLastActiveTimestamp() {
        preferenceManager?.clearLastActiveTimestamp()
    }

    override fun resetV1AnonymousId() {
        preferenceManager?.resetV1AnonymousId()
    }

    override fun resetV1OptOut() {
        preferenceManager?.resetV1OptOut()
    }

    override fun resetV1Traits() {
        preferenceManager?.resetV1Traits()
    }

    override fun resetV1ExternalIds() {
        preferenceManager?.resetV1ExternalIds()
    }

    override fun resetV1AdvertisingId() {
        preferenceManager?.resetV1AdvertisingId()
    }

    override fun resetV1Build() {
        preferenceManager?.resetV1Build()
    }

    override fun resetV1Version() {
        preferenceManager?.resetV1VersionName()
    }

    override fun resetV1SessionId() {
        preferenceManager?.resetV1SessionId()
    }

    override fun resetV1SessionLastActiveTimestamp() {
        preferenceManager?.resetV1LastActiveTimestamp()
    }

    override fun migrateV1StorageToV2Sync(): Boolean {
        return migrateV1MessagesToV2Database(application, rudderDatabase?:return false,
            jsonAdapter?:return false, logger)
    }

    override fun migrateV1StorageToV2(callback: (Boolean) -> Unit) {
        storageExecutor.execute {
            callback(migrateV1StorageToV2Sync())
        }
    }

    override fun deleteV1SharedPreferencesFile() {
        storageExecutor.execute {
            preferenceManager?.deleteV1PreferencesFile()
        }
    }

    override fun deleteV1ConfigFiles() {
        storageExecutor.execute {
            deleteFile(application, SERVER_CONFIG_FILE_NAME)
            deleteFile(application, V1_RUDDER_FLUSH_CONFIG_FILE_NAME)
        }
    }

    override fun setBuild(build: Int) {
        preferenceManager?.saveBuild(build)
    }

    override fun setVersionName(versionName: String) {
        preferenceManager?.saveVersionName(versionName)
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
        preferenceManager = RudderPreferenceManager(application, writeKey)
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
