/*
 * Creator: Debanjan Chatterjee on 14/06/23, 5:02 pm Last modified: 14/06/23, 5:02 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid

import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot

interface Reservoir {
    fun insertOrIncrement(metric: MetricModel<out Number>)
    fun getAllMetricsSync(): List<MetricModelWithId<out Number>>
    fun getAllMetrics(callback: (List<MetricModelWithId<out Number>>) -> Unit)

    fun getMetricsFirstSync(limit: Long): List<MetricModelWithId<out Number>>
    fun getMetricsFirstSync(skip: Long, limit: Long): List<MetricModelWithId<out Number>>
    fun getMetricsFirst(
        skip: Long,
        limit: Long,
        callback: (List<MetricModelWithId<out Number>>) -> Unit
    )

    fun getMetricsAndErrors(
        skipForMetrics: Long, skipForErrors: Long, limit: Long, callback: (
            List<MetricModelWithId<out Number>>, List<ErrorEntity>
        ) -> Unit
    )

    fun getMetricsFirst(limit: Long, callback: (List<MetricModelWithId<out Number>>) -> Unit)

    //    fun getMetricsAndErrorFirst(limit : Long, callback : (List<MetricModel<Number>>, List<ErrorModel>) -> Unit)
    fun getMetricsCount(callback: (Long) -> Unit)
    fun clear()
    fun clearMetrics()
    fun resetMetricsFirst(limit: Long)

    fun setMaxErrorCount(maxErrorCount: Long)
    fun saveError(errorEntity: ErrorEntity)
    fun getAllErrorsSync(): List<ErrorEntity>
    fun getAllErrors(callback: (List<ErrorEntity>) -> Unit)

    fun getErrorsFirstSync(limit: Long): List<ErrorEntity>
    fun getErrors(
        skip: Long, limit: Long, callback: (List<ErrorEntity>) -> Unit
    )

    fun getErrorsFirst(limit: Long, callback: (List<ErrorEntity>) -> Unit)
    fun getErrorsCount(callback: (Long) -> Unit)
    fun clearErrors()
    fun clearErrors(ids: Array<Long>)
    fun clearErrorsSync(ids: Array<Long>)

    fun saveSnapshot(snapshot: Snapshot, callback: ((Long) -> Unit)?= null)
    fun saveSnapshotSync(snapshot: Snapshot) : Long
    fun getAllSnapshotsSync(): List<Snapshot>
    fun getAllSnapshots(callback: (List<Snapshot>) -> Unit)

    fun getSnapshots(limit: Long, offset: Int = 0): List<Snapshot>
    fun deleteSnapshots(snapshotIds: List<String>, callback: ((numberOfRows: Int) -> Unit)?= null)

    /**
     * Deletes the snapshots with the given ids
     *
     * @param snapshotIds : List of snapshot ids
     * @return number of rows deleted
     */
    fun deleteSnapshotsSync(snapshotIds: List<String>) : Int
    fun clearSnapshots()

    /**
     * Will reset each element up to the value
     * Let's say a metric with value 10 is fetched and being uploaded
     * Meanwhile the value gets incremented to 15.
     * After the upload is complete, the value should be reset to 5 and not 0
     * This is where is method comes in handy
     *
     * @param dumpedMetrics
     */
    fun resetTillSync(dumpedMetrics: List<MetricModelWithId<out Number>>)
    fun reset()
    interface DataListener {
        /**
         * Called whenever there's a change in data count
         *
         */
        fun onDataChange()

    }
    //this is a combined response class
}