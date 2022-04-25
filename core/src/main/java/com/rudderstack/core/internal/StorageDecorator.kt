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

import com.rudderstack.core.Settings
import com.rudderstack.core.State
import com.rudderstack.core.Storage
import com.rudderstack.core.internal.states.SettingsState
import com.rudderstack.android.models.Message
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

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
    private val _isShutDown = AtomicBoolean(false)

    private val onDataChange = object : Storage.DataListener {
        override fun onDataChange() {
            if(_isShutDown.get())
                return
            getCount {
                if (it >= (settingsState.value?.flushQueueSize ?: 0)
                ) {
                    dataChangeListener?.onDataChange()
                    rescheduleTimer(settingsState.value)
                }
            }
        }

        override fun onDataDropped(messages: List<Message>, error: Throwable) {
            /**
             * We won't be considering dropped events here
             */
        }
    }
    private val _settingsObserver = { settings : Settings? ->
//        println("settings state listen this: $this")
        rescheduleTimer(settings)
    }


        init {
        settingsState.subscribe (_settingsObserver)
        addDataListener(onDataChange)
    }

    private fun rescheduleTimer(settings: Settings?) {
        periodicTaskScheduler?.cancel()
        thresholdCountDownTimer.purge()
        if (settings != null) {
            periodicTaskScheduler = object : TimerTask() {
                override fun run() {
                    /*getData { data ->
                        if (data.isNotEmpty())*/
//                    println("periodic_task_call, $this, timer: $thresholdCountDownTimer, " +
//                            "decorator: ${this@StorageDecorator}")
                    thresholdCountDownTimer
                    dataChangeListener?.onDataChange()
//                    }
                }
            }
            if (!_isShutDown.get()) {
                println("rescheduling : $this")
                thresholdCountDownTimer.schedule(
                    periodicTaskScheduler,
                    settings.maxFlushInterval,
                    settings.maxFlushInterval
                )
            }
        }
    }

    override fun shutdown() {
        if(_isShutDown.compareAndSet(false, true)) {
            println("shutting down : $this")
            storage.shutdown()
            removeDataListener(onDataChange)
            thresholdCountDownTimer.cancel()
            settingsState.removeObserver(_settingsObserver)
        }
    }

    /**
     * Listener for listening to data change. Should adhere to [Settings]
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