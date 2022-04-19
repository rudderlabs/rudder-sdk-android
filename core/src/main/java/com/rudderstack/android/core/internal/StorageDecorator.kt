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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.Settings
import com.rudderstack.android.core.State
import com.rudderstack.android.core.Storage
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.Message
import java.util.*

/**
 * Storage Decorator that invokes the listener based on the threshold set, with respect to time or
 * data count
 *
 * @property storage Platform specific implementation of [Storage]
 */
internal class StorageDecorator(
    private val storage: Storage,
    private val settingsState: State<Settings> = SettingsState,
    private val dataChangeListener: Listener? = null
) : Storage by storage {
    private val thresholdCountDownTimer = Timer("data_listen")
    private var periodicTaskScheduler: TimerTask? = null
    private var _isShutDown = false

    private val onDataChange = object : Storage.DataListener {
        override fun onDataChange(messages: List<Message>) {
            println("\non data change: $messages \nthread: ${Thread.currentThread().name}\n")

            if (messages.isNotEmpty() && settingsState.value?.flushQueueSize ?: 0 <= messages.size) {
                dataChangeListener?.onDataChange(messages)
                rescheduleTimer(settingsState.value)
            }
        }

        override fun onDataDropped(messages: List<Message>, error: Throwable) {
            /**
             * We won't be considering dropped events here
             */
        }
    }

    init {
        settingsState.subscribe {
            rescheduleTimer(it)
        }
        addDataListener(onDataChange)
    }

    private fun rescheduleTimer(settings: Settings?) {
        periodicTaskScheduler?.cancel()
        thresholdCountDownTimer.purge()
        if (settings != null) {
            periodicTaskScheduler = object : TimerTask() {
                override fun run() {
                    getData { data ->
                        if (data.isNotEmpty())
                            dataChangeListener?.onDataChange(data)
                    }
                }
            }
            if (!_isShutDown)
                thresholdCountDownTimer.schedule(
                    periodicTaskScheduler,
                    settings.maxFlushInterval,
                    settings.maxFlushInterval
                )
        }
    }

    override fun shutdown() {
        _isShutDown = true
        storage.shutdown()
        removeDataListener(onDataChange)
        thresholdCountDownTimer.cancel()

    }

    /**
     * Listener for listening to data change. Should adhere to [Settings]
     *
     *
     */
    internal fun interface Listener {
        /**
         * Data changed in database. Either the threshold time or threshold data count has elapsed
         *
         * @param messages List of [Message]
         */
        fun onDataChange(messages: List<Message>)
    }
}