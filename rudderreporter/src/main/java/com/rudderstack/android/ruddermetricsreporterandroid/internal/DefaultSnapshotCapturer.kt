/*
 * Creator: Debanjan Chatterjee on 06/11/23, 1:05 pm Last modified: 06/11/23, 1:05 pm
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

import android.util.Log
import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.SnapshotCapturer
import com.rudderstack.android.ruddermetricsreporterandroid.SnapshotCreator
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

internal class DefaultSnapshotCapturer(
    private val snapshotCreator: SnapshotCreator,
    private val snapshotExecutor: ExecutorService = Executors.newCachedThreadPool()
) : SnapshotCapturer {
    private val snapshotLock = ReentrantLock()
    override fun captureSnapshotsAndResetReservoir(
        batchItemCount: Long, reservoir: Reservoir
    ): Int {
        snapshotLock.lock()
        var metrics = reservoir.getMetricsFirstSync(batchItemCount)
        var errors = reservoir.getErrorsFirstSync(batchItemCount)
        var totalBatches = 0
        var validMetrics = metrics.filterWithValidValues()
        while (validMetrics.isNotEmpty() || errors.isNotEmpty()) {

            snapshotCreator.createSnapshot(validMetrics, errors.map { it.errorEvent })?.apply {
                if (reservoir.saveSnapshotSync(this) > -1) {
                    reservoir.resetTillSync(metrics)
                    reservoir.clearErrorsSync(errors.map { it.id }.toTypedArray())
                } else return totalBatches

            } ?: return totalBatches
            totalBatches++
            metrics = if (metrics.size >= batchItemCount) reservoir.getMetricsFirstSync(
                batchItemCount * totalBatches, batchItemCount
            ) else listOf()
            validMetrics = metrics.filterWithValidValues()
            errors = reservoir.getErrorsFirstSync(batchItemCount)
        }
        snapshotLock.unlock()
        return totalBatches
    }

    override fun captureSnapshotsAndResetReservoir(
        batchItemCount: Long, reservoir: Reservoir, callback: (totalBatches: Int) -> Unit
    ) {
        snapshotExecutor.execute {
            callback.invoke(captureSnapshotsAndResetReservoir(batchItemCount, reservoir))
        }
    }

    override fun shutdown() {
        snapshotExecutor.shutdown()
    }

    private fun List<MetricModelWithId<out Number>>.filterWithValidValues(): List<MetricModelWithId<out Number>> {
        return this.filter {
            it.value.toLong() > 0
        }
    }
}