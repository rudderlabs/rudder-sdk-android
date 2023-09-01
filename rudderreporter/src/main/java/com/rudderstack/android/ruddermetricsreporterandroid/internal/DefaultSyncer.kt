/*
 * Creator: Debanjan Chatterjee on 22/06/23, 7:58 pm Last modified: 22/06/23, 7:58 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.Syncer
import com.rudderstack.android.ruddermetricsreporterandroid.UploadMediator
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

class DefaultSyncer internal constructor(
    private val reservoir: Reservoir,
    private val uploader: UploadMediator
) : Syncer {
    private var callback: ((uploaded: List<MetricModel<out Number>>, success: Boolean) -> Unit)? =
        null

    private val _isShutDown = AtomicBoolean(false)
    private val _atomicRunning = AtomicBoolean(false)

    private var flushCount = DEFAULT_FLUSH_SIZE
    private val scheduler = Scheduler()
    override fun startScheduledSyncs(
        interval: Long, flushOnStart: Boolean,
        flushCount: Long
    ) {
        this.flushCount = flushCount
        _isShutDown.set(false)
        scheduler.scheduleTimer(flushOnStart, interval) {
            flushAllMetrics()
        }
    }


    override fun setCallback(callback: ((uploaded: List<MetricModel<out Number>>, success: Boolean) -> Unit)?) {
        this.callback = callback
    }

    private fun flushMetrics(flushCount: Long) {
        flushMetrics(0L, flushCount)
    }
    private fun flushMetrics(startIndex: Long, flushCount: Long) {
        //TODO: add error handling getMetricsAndErrorFirst
        reservoir.getMetricsAndErrors(startIndex, flushCount) { metrics, errors ->
            val validMetrics = metrics.filterWithValidValues()
            if (validMetrics.isEmpty() && errors.isEmpty()) {
                _atomicRunning.set(false)
                if (_isShutDown.get())
                    stopScheduling()
                return@getMetricsAndErrors
            }

            uploader.upload(validMetrics, ErrorModel(errors.map { it.errorEvent })) { success ->
                if (success) {
                    reservoir.resetTillSync(validMetrics)
                    reservoir.clearErrors(errors.map { it.id }.toTypedArray())
                }

                callback?.invoke(validMetrics, success)
                if (_isShutDown.get()) {
                    _atomicRunning.set(false)
                    stopScheduling()
                    return@upload
                }
                if(success)
                    flushMetrics(startIndex + flushCount, flushCount)
                else
                    _atomicRunning.set(false)
            }
        }
    }

    override fun stopScheduling() {
        _isShutDown.set(true)
        if (_atomicRunning.get())
            return
        scheduler.stop()
    }

    override fun flushAllMetrics() {
        if (_isShutDown.get())
            return
        if (_atomicRunning.compareAndSet(false, true)) {
            flushMetrics(flushCount)
        }
    }

    companion object {
        private const val DEFAULT_FLUSH_SIZE = 20L
    }

    class Scheduler internal constructor(){
        private val thresholdCountDownTimer = Timer("metrics_scheduler")
        private var periodicTaskScheduler: TimerTask? = null

        fun scheduleTimer(callbackOnStart: Boolean, flushInterval: Long, callback: () -> Unit) {
            periodicTaskScheduler?.cancel()
            thresholdCountDownTimer.purge()
            periodicTaskScheduler = object : TimerTask() {
                override fun run() {
                    callback.invoke()
                }
            }
            println("rescheduling : $this")
            thresholdCountDownTimer.scheduleAtFixedRate(
                periodicTaskScheduler,
                if (callbackOnStart) 0 else flushInterval,
                flushInterval
            )

        }
        fun stop(){
            periodicTaskScheduler?.cancel()
            thresholdCountDownTimer.cancel()
        }
    }
    private fun List<MetricModelWithId<out Number>>.filterWithValidValues(): List<MetricModelWithId<out Number>> {
        return this.filter {
            it.value.toLong() > 0
        }
    }
}