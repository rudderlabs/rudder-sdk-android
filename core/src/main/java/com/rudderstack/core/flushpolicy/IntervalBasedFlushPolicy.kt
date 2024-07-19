/*
 * Creator: Debanjan Chatterjee on 31/01/24, 2:51 pm Last modified: 31/01/24, 2:51 pm
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
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class IntervalBasedFlushPolicy : FlushPolicy {

    override lateinit var analytics: Analytics

    private var thresholdCountDownTimer: Timer? = null

    private var flushCall: AtomicReference<Analytics.() -> Unit> = AtomicReference({})

    private val _isShutDown = AtomicBoolean(false)
    private var _currentFlushIntervalAtomic = AtomicLong(0L)
    private val _currentFlushInterval
        get() = _currentFlushIntervalAtomic.get()
    private var periodicTaskScheduler: TimerTask? = null

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _isShutDown.set(false)
        thresholdCountDownTimer = Timer(analytics.writeKey + "-IntervalBasedFlushPolicy")

    }

    override fun reschedule() {
        if (_isShutDown.get()) return
        rescheduleTimer()
    }

    override fun onRemoved() {
        periodicTaskScheduler?.cancel()
        thresholdCountDownTimer?.purge()
        periodicTaskScheduler = null
        thresholdCountDownTimer = null
    }

    override fun setFlush(flush: Analytics.() -> Unit) {
        flushCall.set(flush)
    }

    override fun updateConfiguration(configuration: Configuration) {
        if (shouldRescheduleTimer(configuration)) {
            updateMaxFlush(configuration.maxFlushInterval)
            rescheduleTimer()
        }
    }

    private fun updateMaxFlush(maxFlushInterval: Long) {
        _currentFlushIntervalAtomic.set(maxFlushInterval)
    }

    private fun shouldRescheduleTimer(configuration: Configuration?): Boolean {
        val newValue = configuration?.maxFlushInterval?.coerceAtLeast(0L) ?: 0L
        return (!_isShutDown.get() && configuration != null && _currentFlushInterval != newValue)
    }

    private fun rescheduleTimer() {
        periodicTaskScheduler?.cancel()
        thresholdCountDownTimer?.purge()

        if (_isShutDown.get()) {
            return
        }

        periodicTaskScheduler = createFlushTimerTask()
        thresholdCountDownTimer?.schedule(
            periodicTaskScheduler, _currentFlushInterval, _currentFlushInterval
        )

    }

    private fun createFlushTimerTask() = object : TimerTask() {
        override fun run() {
            synchronized(this@IntervalBasedFlushPolicy) {
                if (!_isShutDown.get())
                    flushCall.get()(analytics)
            }
        }
    }

    override fun shutdown() {
        if (_isShutDown.compareAndSet(false, true)) {
            onRemoved()
        }
    }
}
