/*
 * Creator: Debanjan Chatterjee on 30/12/21, 6:28 PM Last modified: 30/12/21, 6:28 PM
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

package com.rudderstack.core.internal

import com.rudderstack.core.Configuration
import com.rudderstack.core.State
import com.rudderstack.core.Storage
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.models.Message
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Storage Decorator that invokes the listener based on the threshold set, with respect to time or
 * data count
 *
 * @property storage Platform specific implementation of [Storage]
 */
internal class FlushScheduler @JvmOverloads constructor(
    private val dataChangeListener: Listener,
    private val thresholdCountDownTimer: Timer = Timer("data_listen")
) {
    private val storage
        get() = ConfigurationsState.value?.storage

    private var periodicTaskScheduler: TimerTask? = null
    private val _isShutDown = AtomicBoolean(false)
    private var _currentFlushIntervalAtomic = AtomicLong(0L)
    private val _currentFlushInterval
        get() = _currentFlushIntervalAtomic.get()
    private val isInitialized = AtomicBoolean(false)
    private val onDataChange = object : Storage.DataListener {
        override fun onDataChange() {
            if (_isShutDown.get()) return
            storage?.getCount {
                if (it >= (ConfigurationsState.value?.flushQueueSize ?: 0)) {
                    dataChangeListener.onDataChange()
                    rescheduleTimer()
                }
            }
        }

        override fun onDataDropped(messages: List<Message>, error: Throwable) {
            /**
             * We won't be considering dropped events here
             */
        }
    }
    private val _configurationObserver: State.Observer<Configuration> =
        State.Observer { configuration: Configuration? ->
            configuration ?: return@Observer
            if (isInitialized.compareAndSet(false, true)) {
                configuration.storage.addDataListener(onDataChange)
                updateMaxFlush(configuration.maxFlushInterval)
                rescheduleTimer()
            } else {
                if (shouldRescheduleTimer(configuration)) {
                    updateMaxFlush(configuration.maxFlushInterval ?: 0L)
                    rescheduleTimer()
                }
            }
        }

    private fun updateMaxFlush(maxFlushInterval: Long) {
        _currentFlushIntervalAtomic.set(maxFlushInterval)
    }

    private fun shouldRescheduleTimer(configuration: Configuration?): Boolean {
        val newValue = configuration?.maxFlushInterval?.coerceAtLeast(0L) ?: 0L
        return (configuration != null && _currentFlushInterval != newValue)
    }


    init {
        ConfigurationsState.subscribe(_configurationObserver)

    }

    private fun rescheduleTimer() {
        periodicTaskScheduler?.cancel()
        thresholdCountDownTimer.purge()
//        if (configuration != null) {
        periodicTaskScheduler = object : TimerTask() {
            override fun run() {
                dataChangeListener.onDataChange()
            }
        }
        if (!_isShutDown.get()) {
            println("rescheduling : $this with interval : $_currentFlushInterval")
            thresholdCountDownTimer.schedule(
                periodicTaskScheduler, _currentFlushInterval, _currentFlushInterval
            )
        }
//        }
    }

    fun shutdown() {
        if (_isShutDown.compareAndSet(false, true)) {
            println("shutting down : $this")
            storage?.removeDataListener(onDataChange)
            thresholdCountDownTimer.cancel()
            ConfigurationsState.removeObserver(_configurationObserver)
        }
    }

    /**
     * Listener for listening to data change. Should adhere to [Configuration]
     *
     *
     */
    internal fun interface Listener {
        /**
         * Data changed in database. Either the threshold time elapsed or data count has changed
         *
         *
         */
        fun onDataChange()
    }
}