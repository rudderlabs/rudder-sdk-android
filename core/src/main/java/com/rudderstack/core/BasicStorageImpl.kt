package com.rudderstack.core

import com.rudderstack.core.models.IdentifyTraits
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.RudderServerConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.Properties
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference

private const val PROPERTIES_FILE_NAME = "config.properties"
private const val LIB_KEY_NAME = "libraryName"
private const val LIB_KEY_VERSION = "rudderCoreSdkVersion"
private const val LIB_KEY_PLATFORM = "platform"
private const val LIB_KEY_OS_VERSION = "os_version"

@Suppress("ThrowableNotThrown")
class BasicStorageImpl @JvmOverloads constructor(
    /**
     * queue size should be greater than or equals [Storage.MAX_STORAGE_CAPACITY]
     */
    private val queue: Queue<Message> = LinkedBlockingQueue(),
) : Storage {

    override lateinit var analytics: Analytics
    private var configurationRef = AtomicReference<Configuration>(null)

    private val logger
        get() = configurationRef.get()?.logger
    private var backPressureStrategy = Storage.BackPressureStrategy.Drop

    private var _storageCapacity = Storage.MAX_STORAGE_CAPACITY
    private var _maxFetchLimit = Storage.MAX_FETCH_LIMIT

    private var _dataChangeListeners = setOf<WeakReference<Storage.DataListener>>()
    private var _isOptOut = false
    private var _optOutTime = -1L
    private var _optInTime = -1L

    private var _serverConfig: RudderServerConfig? = null
    private var _traits: IdentifyTraits? = null
//    private var _externalIds: List<Map<String, String>>? = null

    //library details
    private val libDetails: Map<String, String> by lazy {
        try {
            Properties().let {
                it.load(FileInputStream(PROPERTIES_FILE_NAME))
                mapOf(
                    LIB_KEY_NAME to it.getProperty(LIB_KEY_NAME),
                    LIB_KEY_VERSION to it.getProperty(LIB_KEY_VERSION),
                    LIB_KEY_PLATFORM to it.getProperty(LIB_KEY_PLATFORM),
                    LIB_KEY_OS_VERSION to it.getProperty(LIB_KEY_OS_VERSION)
                )
            }
        } catch (ex: IOException) {
            logger?.error(log = "Config fetch error", throwable = ex)
            mapOf()
        }

    }

    /**
     * This queue holds the messages that are generated prior to destinations waking up
     */
    private val startupQ = LinkedList<Message>()

    private val serverConfigFile = File("temp/rudder-analytics/server_config")
    override fun setStorageCapacity(storageCapacity: Int) {
        _storageCapacity = storageCapacity
    }

    override fun setMaxFetchLimit(limit: Int) {
        _maxFetchLimit = limit
    }

    override fun saveMessage(vararg messages: Message) {
        //a block to call data listener
        val dataFailBlock: List<Message>.() -> Unit = {
            _dataChangeListeners.forEach {
                it.get()?.onDataDropped(
                    this,
                    IllegalArgumentException("Storage Capacity Exceeded")
                )
            }
        }
        synchronized(this) {
            val excessMessages = queue.size + messages.size - _storageCapacity
            if (excessMessages > 0) {
                if (backPressureStrategy == Storage.BackPressureStrategy.Drop) {
                    logger?.warn(log = "Max storage capacity reached, dropping last$excessMessages latest events")

                    (messages.size - excessMessages).takeIf {
                        it > 0
                    }?.apply {
                        queue.addAll(messages.take(this))
                        //callback
                        messages.takeLast(excessMessages).run(dataFailBlock)

                    } ?: messages.toList().run(dataFailBlock)

                } else {
                    logger?.warn(log = "Max storage capacity reached, dropping first$excessMessages oldest events")
                    val tobeRemovedList = ArrayList<Message>(excessMessages)
                    var counter = excessMessages
                    while (counter > 0) {
                        val item = queue.poll()
                        if (item != null) {
                            counter--
                            tobeRemovedList.add(item)
                        } else
                            break
                    }
                    queue.addAll(messages.takeLast(_storageCapacity))
                    //callback
                    tobeRemovedList.run(dataFailBlock)
                }
            } else {
                queue.addAll(messages)
            }
        }
        onDataChange()
    }

    override fun setBackpressureStrategy(strategy: Storage.BackPressureStrategy) {
        backPressureStrategy = strategy
    }

    override fun deleteMessages(messages: List<Message>) {
        //basic storage does not support async delete
        deleteMessagesSync(messages)
    }

    override fun addMessageDataListener(listener: Storage.DataListener) {
        _dataChangeListeners = _dataChangeListeners + WeakReference(listener)
    }

    override fun removeMessageDataListener(listener: Storage.DataListener) {

        _dataChangeListeners = _dataChangeListeners.filter {
            it.get() != null && it.get() != listener
        }.toSet()

    }

    override fun getData(offset: Int, callback: (List<Message>) -> Unit) {
        callback.invoke(
            synchronized(this) {
                if (queue.size <= offset) emptyList() else
                    queue.toMutableList().takeLast(queue.size - offset)
                        .take(_maxFetchLimit).toList()
            })
    }

    override fun getCount(callback: (Long) -> Unit) {
        synchronized(this) {
            queue.size.toLong().apply(callback)
        }
    }

    override fun getDataSync(offset: Int): List<Message> {
        return synchronized(this) {
            if (queue.size <= offset) emptyList() else queue.toList().takeLast(queue.size - offset)
                .take(_maxFetchLimit)
        }
    }

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

        } catch (ex: Exception) {
            logger?.error(log = "Server Config cannot be saved", throwable = ex)
        }
    }

    override fun saveOptOut(optOut: Boolean) {
        _isOptOut = optOut
        if (optOut) {
            _optOutTime = System.currentTimeMillis()
        } else
            _optInTime = System.currentTimeMillis()
    }

    override fun saveStartupMessageInQueue(message: Message) {
        synchronized(this) {
            startupQ.add(message)
        }
    }

    override fun clearStartupQueue() {
        synchronized(this) {
            startupQ.clear()
        }
    }

    override fun shutdown() {
        //nothing much to do here

    }

    override fun clearStorage() {
        synchronized(this) {
            queue.clear()
            startupQ.clear()
            _traits = null
            _serverConfig = null
            serverConfigFile.delete()
        }
    }

    override fun deleteMessagesSync(messages: List<Message>) {
        val messageIdsToRemove = messages.map { it.messageId }
        synchronized(this) {
            queue.removeAll {
                it.messageId in messageIdsToRemove
            }
        }
        onDataChange()
    }


    override val serverConfig: RudderServerConfig?
        get() = _serverConfig ?: if (serverConfigFile.exists()) {
            val fis = FileInputStream(serverConfigFile)
            val oos = ObjectInputStream(fis)
            _serverConfig = oos.readObject() as RudderServerConfig?
            _serverConfig
        } else null


    override val startupQueue: List<Message>
        get() = startupQ

    override val isOptedOut: Boolean
        get() = _isOptOut
    override val optOutTime: Long
        get() = _optOutTime
    override val optInTime: Long
        get() = _optInTime

    override val libraryName: String
        get() = libDetails[LIB_KEY_NAME] ?: ""

    override val libraryVersion: String
        get() = libDetails[LIB_KEY_VERSION] ?: ""

    override val libraryPlatform: String
        get() = libDetails[LIB_KEY_PLATFORM] ?: ""

    override val libraryOsVersion: String
        get() = libDetails[LIB_KEY_OS_VERSION] ?: ""

    override fun updateConfiguration(configuration: Configuration) {
        configurationRef.set(configuration)
    }

    override fun toString(): String {
        return "BasicStorageImpl(queue=$queue, _storageCapacity=$_storageCapacity, _maxFetchLimit=$_maxFetchLimit, _dataChangeListeners=$_dataChangeListeners, _isOptOut=$_isOptOut, _optOutTime=$_optOutTime, _optInTime=$_optInTime, _serverConfig=$_serverConfig, _traits=$_traits, libDetails=$libDetails, serverConfigFile=$serverConfigFile)"
    }

    private fun onDataChange() {
        synchronized(this) {
            _dataChangeListeners.forEach {
                it.get()?.onDataChange()
            }

        }
    }
}
