package com.vagabond.testcommon

import com.rudderstack.core.Analytics
import com.rudderstack.core.Storage
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.RudderServerConfig

class VerificationStorage : Storage {

    override lateinit var analytics: Analytics
    private var storageQ = mutableListOf<Message>()
    private var _serverConfig: RudderServerConfig? = null
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

}
