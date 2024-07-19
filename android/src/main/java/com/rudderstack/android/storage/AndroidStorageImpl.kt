package com.rudderstack.android.storage

import android.app.Application
import android.os.Build
import com.rudderstack.android.BuildConfig
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_ADVERTISING_ID_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_ANONYMOUS_ID_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_APPLICATION_BUILD_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_APPLICATION_VERSION_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_EXTERNAL_ID_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_OPT_STATUS_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_PREFS
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_SESSION_ID_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_TRACK_AUTO_SESSION_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_TRAITS_KEY
import com.rudderstack.android.internal.prefs.SharedPrefs.RUDDER_USER_ID_KEY
import com.rudderstack.android.internal.prefs.SharedPrefsRepository
import com.rudderstack.android.internal.prefs.SharedPrefsStore
import com.rudderstack.android.repository.Dao
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Logger
import com.rudderstack.core.Storage
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.MessageContext
import com.rudderstack.core.models.RudderServerConfig
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

    override lateinit var analytics: Analytics

    private var logger: Logger? = null
    private val dbName get() = "rs_persistence_$writeKey"
    private var jsonAdapter: JsonAdapter? = null

    private val String.key: String
        get() = "$this-$writeKey"

    private var rudderPrefsRepo: SharedPrefsRepository = SharedPrefsStore(application, RUDDER_PREFS.key)
    private var oldRudderPrefs: SharedPrefsRepository = SharedPrefsStore(application, RUDDER_PREFS)

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

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        initDb(analytics)
    }

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
            RudderEntityFactory(analytics.currentConfiguration?.jsonAdapter),
            useContentProvider,
            DB_VERSION,
            providedExecutorService = storageExecutor
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
        rudderPrefsRepo.save(RUDDER_OPT_STATUS_KEY, optOut)
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

    override val startupQueue: List<Message> = startupQ
    override val optOutTime: Long = _optOutTime.get()
    override val optInTime: Long = _optInTime.get()
    override val anonymousId: String = rudderPrefsRepo.getString(RUDDER_ANONYMOUS_ID_KEY)
    override val userId: String = rudderPrefsRepo.getString(RUDDER_USER_ID_KEY)
    override val sessionId: Long = rudderPrefsRepo.getLong(RUDDER_SESSION_ID_KEY)
    override val lastActiveTimestamp: Long = rudderPrefsRepo.getLong(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY)
    override val advertisingId: String = rudderPrefsRepo.getString(RUDDER_ADVERTISING_ID_KEY)
    override val trackAutoSession: Boolean = rudderPrefsRepo.getBoolean(RUDDER_TRACK_AUTO_SESSION_KEY)
    override val build: Int = rudderPrefsRepo.getInt(RUDDER_APPLICATION_BUILD_KEY)
    override val versionName: String = rudderPrefsRepo.getString(RUDDER_APPLICATION_VERSION_KEY)
    override val isOptedOut: Boolean = rudderPrefsRepo.getBoolean(RUDDER_OPT_STATUS_KEY)

    override val v1AnonymousId: String = oldRudderPrefs.getString(RUDDER_ANONYMOUS_ID_KEY)
    override val v1SessionId: Long = oldRudderPrefs.getLong(RUDDER_SESSION_ID_KEY)
    override val v1LastActiveTimestamp: Long = oldRudderPrefs.getLong(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY)
    override val v1AdvertisingId: String = oldRudderPrefs.getString(RUDDER_ADVERTISING_ID_KEY)
    override val v1Build: Int = oldRudderPrefs.getInt(RUDDER_APPLICATION_BUILD_KEY)
    override val v1VersionName: String = oldRudderPrefs.getString(RUDDER_APPLICATION_VERSION_KEY)
    override val v1OptOut: Boolean = oldRudderPrefs.getBoolean(RUDDER_OPT_STATUS_KEY)
    override val v1Traits: Map<String, Any?>? = oldRudderPrefs.getString(RUDDER_TRAITS_KEY).let {
        jsonAdapter?.readJson(it, object : RudderTypeAdapter<Map<String, Any?>>() {})
    }
    override val v1ExternalIds: List<Map<String, String>>? = oldRudderPrefs.getString(RUDDER_EXTERNAL_ID_KEY).let {
        jsonAdapter?.readJson(it, object : RudderTypeAdapter<List<Map<String, String>>>() {})
    }

    override fun setAnonymousId(anonymousId: String) {
        _anonymousId = anonymousId
        rudderPrefsRepo.save(RUDDER_ANONYMOUS_ID_KEY, anonymousId)
    }

    override fun setUserId(userId: String) {
        _userId = userId
        rudderPrefsRepo.save(RUDDER_USER_ID_KEY, userId)
    }

    override fun setSessionId(sessionId: Long) {
        rudderPrefsRepo.save(RUDDER_SESSION_ID_KEY, sessionId)
    }

    override fun setTrackAutoSession(trackAutoSession: Boolean) {
        rudderPrefsRepo.save(RUDDER_TRACK_AUTO_SESSION_KEY, trackAutoSession)
    }

    override fun saveLastActiveTimestamp(timestamp: Long) {
        rudderPrefsRepo.save(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY, timestamp)
    }

    override fun saveAdvertisingId(advertisingId: String) {
        rudderPrefsRepo.save(RUDDER_ADVERTISING_ID_KEY, advertisingId)
    }

    override fun clearSessionId() {
        rudderPrefsRepo.clear(RUDDER_SESSION_ID_KEY)
    }

    override fun clearLastActiveTimestamp() {
        rudderPrefsRepo.clear(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY)
    }

    override fun resetV1AnonymousId() {
        oldRudderPrefs.clear(RUDDER_ANONYMOUS_ID_KEY)
    }

    override fun resetV1OptOut() {
        oldRudderPrefs.clear(RUDDER_OPT_STATUS_KEY)
    }

    override fun resetV1Traits() {
        oldRudderPrefs.clear(RUDDER_TRAITS_KEY)
    }

    override fun resetV1ExternalIds() {
        oldRudderPrefs.clear(RUDDER_EXTERNAL_ID_KEY)
    }

    override fun resetV1AdvertisingId() {
        oldRudderPrefs.clear(RUDDER_ADVERTISING_ID_KEY)
    }

    override fun resetV1Build() {
        oldRudderPrefs.clear(RUDDER_APPLICATION_BUILD_KEY)
    }

    override fun resetV1Version() {
        oldRudderPrefs.clear(RUDDER_APPLICATION_VERSION_KEY)
    }

    override fun resetV1SessionId() {
        oldRudderPrefs.clear(RUDDER_SESSION_ID_KEY)
    }

    override fun resetV1SessionLastActiveTimestamp() {
        oldRudderPrefs.clear(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY)
    }

    override fun migrateV1StorageToV2Sync(): Boolean {
        return migrateV1MessagesToV2Database(
            context = application,
            v2Database = rudderDatabase ?: return false,
            jsonAdapter = jsonAdapter ?: return false,
            logger = logger
        )
    }

    override fun migrateV1StorageToV2(callback: (Boolean) -> Unit) {
        storageExecutor.execute {
            callback(migrateV1StorageToV2Sync())
        }
    }

    override fun deleteV1SharedPreferencesFile() {
        storageExecutor.execute {
            rudderPrefsRepo.deleteSharedPrefs(RUDDER_PREFS)
        }
    }

    override fun deleteV1ConfigFiles() {
        storageExecutor.execute {
            deleteFile(application, SERVER_CONFIG_FILE_NAME)
            deleteFile(application, V1_RUDDER_FLUSH_CONFIG_FILE_NAME)
        }
    }

    override fun setBuild(build: Int) {
        rudderPrefsRepo.save(RUDDER_APPLICATION_BUILD_KEY, build)
    }

    override fun setVersionName(versionName: String) {
        rudderPrefsRepo.save(RUDDER_APPLICATION_VERSION_KEY, versionName)
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
        get() = jsonAdapter?.let { MessageEntity(this, it) }

    private fun MessageContext.save() {
        saveObject(
            HashMap(this), application, contextFileName, logger
        )
    }
}
