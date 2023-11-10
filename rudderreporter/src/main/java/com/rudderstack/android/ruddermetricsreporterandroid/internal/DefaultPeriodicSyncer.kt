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
import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot
import com.rudderstack.android.ruddermetricsreporterandroid.SnapshotCapturer
import com.rudderstack.android.ruddermetricsreporterandroid.PeriodicSyncer
import com.rudderstack.android.ruddermetricsreporterandroid.UploadMediator
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

class DefaultPeriodicSyncer internal constructor(
    private val reservoir: Reservoir,
    private val uploader: UploadMediator,
    private val snapshotCapturer: SnapshotCapturer,
//    private val libraryMetadata: LibraryMetadata
) : PeriodicSyncer {
    private var _callback: ((
        snapshot: Snapshot, success: Boolean
    ) -> Unit)? = null
        set(value) {
            synchronized(this) {
                field = value
            }
        }

    private val _isShutDown = AtomicBoolean(false)
    private val _atomicRunning = AtomicBoolean(false)

    private var flushCount = DEFAULT_FLUSH_SIZE
    private val scheduler = Scheduler()
    @Deprecated("Use startPeriodicSyncs instead",
        ReplaceWith("startPeriodicSyncs(interval, flushOnStart, flushCount)")
    )
    override fun startScheduledSyncs(interval: Long, flushOnStart: Boolean, flushCount: Long) {
        startPeriodicSyncs(interval, flushOnStart, flushCount)
    }

    override fun startPeriodicSyncs(
        interval: Long, flushOnStart: Boolean, flushCount: Long
    ) {
        this.flushCount = flushCount
        _isShutDown.set(false)
        scheduler.scheduleTimer(flushOnStart, interval) {
            captureSnapshotAndFlush(flushCount)
        }
    }

    private fun captureSnapshotAndFlush(flushCount: Long) {
        snapshotCapturer.captureSnapshotsAndResetReservoir(flushCount, reservoir) {
            flushAllSnapshots()
        }
    }
    private fun flushAllSnapshots(){
        if (_isShutDown.get()) return
        if (_atomicRunning.compareAndSet(false, true)) {
            uploadSnapshots()
        }
    }

    /**
     * Set a callback for the syncer to invoke after every flush
     *
     * @param callback A higher order function that calls back with the metrics and error model
     * that were uploaded and a boolean indicating if the upload was successful.
     * Even in case of the upload being unsuccessful, the metrics and error model params contains
     * the metrics and errors that were attempted to be uploaded.
     *
     */
    override fun setCallback(
        callback: ((
            snapshot: Snapshot, success: Boolean
        ) -> Unit)?
    ) {
        this._callback = callback
    }

    private fun uploadSnapshots() {
        val snapshotToUpload = reservoir.getSnapshots(1)
        if (snapshotToUpload.isNotEmpty()) {
            uploader.upload(snapshotToUpload.first()) {
                if (it) {
                    reservoir.deleteSnapshots(snapshotToUpload.map { it.id })
                    _callback?.invoke(snapshotToUpload.first(), true)
                    if (_isShutDown.get()) {
                        _atomicRunning.set(false)
                        stopScheduling()
                        return@upload
                    }
                    uploadSnapshots()
                } else {
                    _callback?.invoke(snapshotToUpload.first(), false)
                    _atomicRunning.set(false)
                    if (_isShutDown.get()) stopScheduling()
                }
            }
        }
    }

    override fun stopScheduling() {
        _isShutDown.set(true)
        if (_atomicRunning.get()) return
        scheduler.stop()
        snapshotCapturer.shutdown()
    }

    override fun flushAllMetrics() {
        if (_isShutDown.get()) return
        captureSnapshotAndFlush(flushCount)
    }

    companion object {
        private const val DEFAULT_FLUSH_SIZE = 20L
    }

    class Scheduler internal constructor() {
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
            thresholdCountDownTimer.scheduleAtFixedRate(
                periodicTaskScheduler, if (callbackOnStart) 0 else flushInterval, flushInterval
            )

        }

        fun stop() {
            periodicTaskScheduler?.cancel()
            thresholdCountDownTimer.cancel()
        }
    }

}