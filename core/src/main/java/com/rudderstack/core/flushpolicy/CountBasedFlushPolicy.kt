package com.rudderstack.core.flushpolicy

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Storage
import com.rudderstack.core.models.Message
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class CountBasedFlushPolicy : FlushPolicy {

    override lateinit var analytics: Analytics

    private var storage: Storage? = null
        set(value) {
            synchronized(this) {
                field = value
            }
        }
        get() = synchronized(this) {
            field
        }
    private var flushCall: AtomicReference<Analytics.() -> Unit> = AtomicReference({})
    private val flushQSizeThreshold = AtomicInteger(-1)
    private val _isShutDown = AtomicBoolean(false)
    private val onDataChange = object : Storage.DataListener {
        override fun onDataChange() {
            if (_isShutDown.get()) return
            flushIfNeeded()
        }
        override fun onDataDropped(messages: List<Message>, error: Throwable) {
            /**
             * We won't be considering dropped events here
             */
        }
    }

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        if (storage == analytics.storage) return
        storage = analytics.storage
        storage?.addMessageDataListener(onDataChange)
    }

    private fun flushIfNeeded() {
        storage?.getCount {
            val threshold = flushQSizeThreshold.get()
            if (!_isShutDown.get() && threshold in 1..it) {
                flushCall.get()(analytics)
            }
        }
    }

    override fun reschedule() {
        //-no-op
    }

    override fun setFlush(flush: Analytics.() -> Unit) {
        flushCall.set(flush)
    }

    override fun onRemoved() {
        storage?.removeMessageDataListener(onDataChange)
        storage = null
    }

    override fun updateConfiguration(configuration: Configuration) {
        if (configuration.flushQueueSize == flushQSizeThreshold.get())
            return
        flushQSizeThreshold.set(configuration.flushQueueSize)
        flushIfNeeded()
    }

    override fun shutdown() {
        onRemoved()
        _isShutDown.set(true)
    }
}
