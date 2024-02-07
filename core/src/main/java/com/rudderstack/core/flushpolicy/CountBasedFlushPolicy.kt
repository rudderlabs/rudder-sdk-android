/*
 * Creator: Debanjan Chatterjee on 31/01/24, 12:19 pm Last modified: 31/01/24, 12:19 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.core.flushpolicy

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Storage
import com.rudderstack.models.Message
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class CountBasedFlushPolicy : FlushPolicy {
    private var storage: Storage? = null
        set(value) {
            synchronized(this) {
                field = value
            }
        }
        get() = synchronized(this) {
            field
        }
    private var _analyticsRef = AtomicReference<Analytics>(null)
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

    private fun flushIfNeeded() {
        storage?.getCount {
            val threshold = flushQSizeThreshold.get()
            if (threshold in 1..it) {
                if (!_isShutDown.get()) _analyticsRef.get()?.flush()
            }
        }
    }

    override fun reschedule() {
        //-no-op
    }

    override fun onRemoved() {
        _analyticsRef.set(null)
        storage?.removeMessageDataListener(onDataChange)
        storage = null
    }

    override fun setup(analytics: Analytics) {
        _analyticsRef.set(analytics)
        if(storage == analytics.storage) return
        storage = analytics.storage
        storage?.addMessageDataListener(onDataChange)
    }

    override fun updateConfiguration(configuration: Configuration) {
        if(configuration.flushQueueSize == flushQSizeThreshold.get())
            return
        flushQSizeThreshold.set(configuration.flushQueueSize)
        flushIfNeeded()
    }

    override fun shutdown() {
        onRemoved()
        _isShutDown.set(true)
    }
}