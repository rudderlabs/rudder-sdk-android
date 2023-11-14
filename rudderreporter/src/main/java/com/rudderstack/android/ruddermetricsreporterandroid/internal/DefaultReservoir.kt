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

package com.rudderstack.android.ruddermetricsreporterandroid.internal

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rudderstack.android.repository.Dao
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.LabelEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.MetricEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.SnapshotEntity
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.pow

class DefaultReservoir @JvmOverloads constructor(
    androidContext: Context,
    useContentProvider: Boolean,
    dbExecutor: ExecutorService? = null
) : Reservoir {
    private val dbName = "metrics_db_${androidContext.packageName}.db"
    private val metricDao: Dao<MetricEntity>
    private val labelDao: Dao<LabelEntity>
    private val errorDao: Dao<ErrorEntity>
    private val snapshotDao: Dao<SnapshotEntity>
    private var _storageListeners = listOf<Reservoir.DataListener>()

    private val maxErrorCount = AtomicLong(MAX_ERROR_COUNT)
    init {
        RudderDatabase.init(
            androidContext,
            dbName,
            DefaultEntityFactory(),
            useContentProvider,
            DB_VERSION,
            dbExecutor
        )
        metricDao = RudderDatabase.getDao(MetricEntity::class.java)
        labelDao = RudderDatabase.getDao(LabelEntity::class.java)
        errorDao = RudderDatabase.getDao(ErrorEntity::class.java)
        snapshotDao = RudderDatabase.getDao(SnapshotEntity::class.java)
    }

    override fun insertOrIncrement(
        metric: MetricModel<out Number>
    ) {
        val labels = metric.labels.map { LabelEntity(it.key, it.value) }
        if (labels.isEmpty()) {
            insertCounterWithLabelMask(metric, "")
            return
        }

        with(labelDao) {
            labels.insertWithDataCallback(
                conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_IGNORE
            ) { rowIds: List<Long>, insertedData: List<LabelEntity?> ->
                if (insertedData.isEmpty()) {
                    insertCounterWithLabelMask(metric, "")
                    return@insertWithDataCallback
                }
                //callback is done inside executor
                val insertedIds = getInsertedLabelIds(rowIds, insertedData, labels)

                val labelMaskForMetric = if (insertedIds.isEmpty()) "" else run {
                    val maxIdInserted = insertedIds.max()
                    val useBigDec = (maxIdInserted >= 63)
                    if (useBigDec) {
                        getLabelMaskForMetricWithBigDec(insertedIds)
                    } else {
                        getLabelMaskForMetricWithLong(insertedIds)
//                            .also {
//                            println("label mask for metric ${metric.name} and labels $insertedIds is $it")
//                        }
                    }
                }
                insertCounterWithLabelMask(metric, labelMaskForMetric)
                _storageListeners.forEach { it.onDataChange() }
            }
        }

    }

    private fun getLabelMaskForMetricWithLong(insertedIds: List<Long>): String {
        var labelIdsMask = 0L
        insertedIds.forEach { id ->
            labelIdsMask += 2.0.pow(id.toDouble()).toLong()
        }
        return labelIdsMask.toString()
    }

    private fun getLabelMaskForMetricWithBigDec(insertedIds: List<Long>): String {
        var labelIdsMask = BigDecimal.ZERO
        insertedIds.forEach { id ->
            labelIdsMask += BigDecimal(2).pow(id.toInt())
        }
        return labelIdsMask.toString()
    }

    private fun Dao<LabelEntity>.getInsertedLabelIds(
        rowIds: List<Long>, insertedData: List<LabelEntity?>, queryData: List<LabelEntity>
    ): List<Long> {
        var insertedIds = listOf<Long>()
        rowIds.onEachIndexed { index, rowId ->
            val entry = insertedData[index]
            if (rowId == -1L || entry == null) {
                val name = queryData[index].name
                val valueOfLabel = queryData[index].value
                val idOfAlreadyCreatedLabel = runGetQuerySync(
                    selection = "${LabelEntity.Columns.NAME} = ? AND ${LabelEntity.Columns.VALUE} = ?",
                    selectionArgs = arrayOf(name, valueOfLabel)
                )?.firstOrNull()?.id
                if (idOfAlreadyCreatedLabel != null) {
                    insertedIds = insertedIds + idOfAlreadyCreatedLabel
                }
            } else insertedIds = insertedIds + entry.id
        }
        return insertedIds
    }

    private fun insertCounterWithLabelMask(
        metric: MetricModel<out Number>, labelMaskForMetric: String
    ) {
        val metricEntity = MetricEntity(
            metric.name, metric.value.toLong(), MetricType.COUNTER.value, labelMaskForMetric
        )
        with(metricDao) {
            val insertedRowId = listOf(metricEntity).insertSync(
                conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_IGNORE
            )?.firstOrNull()
            if (insertedRowId == -1L) {
                this.execSqlSync(
                    "UPDATE " + MetricEntity.TABLE_NAME + " SET " + MetricEntity.ColumnNames.VALUE + " = (" + MetricEntity.ColumnNames.VALUE + " + " + metric.value + ") WHERE " + MetricEntity.ColumnNames.NAME + "='" + metric.name + "'" + " AND " + MetricEntity.ColumnNames.LABEL + "='" + labelMaskForMetric + "'" + " AND " + MetricEntity.ColumnNames.TYPE + "='" + MetricType.COUNTER.value + "'" + ";"
                )
            } /*else {
                println("inserting metric ${metric.name} label mask $labelMaskForMetric")
            }*/
        }
    }

    override fun getMetricsFirstSync(limit: Long): List<MetricModelWithId<out Number>> {
        with(metricDao) {
            val metricEntities = runGetQuerySync(limit = limit.toString())
            return metricEntities?.map {
                val labels = getLabelsForMetric(it)
                MetricModelWithId(
                    it.id.toString(), it.name, MetricType.getType(it.type), it.value, labels
                )
            } ?: listOf()
        }
    }

    override fun getMetricsFirstSync(skip: Long, limit: Long): List<MetricModelWithId<out Number>> {
        with(metricDao) {
            val metricEntities = runGetQuerySync(limit = limit.toString(),
                offset = if (skip > 0) skip.toString() else null)
            return metricEntities?.map {
                val labels = getLabelsForMetric(it)
                MetricModelWithId(
                    it.id.toString(), it.name, MetricType.getType(it.type), it.value, labels
                )
            } ?: listOf()
        }
    }

    override fun getMetricsFirst(
        skip: Long, limit: Long, callback: (List<MetricModelWithId<out Number>>) -> Unit
    ) {
        with(metricDao) {
            runGetQuery(
                limit = limit.toString(), offset = if (skip > 0) skip.toString() else null
            ) { metricEntities ->
                callback(metricEntities.map {
                    val labels = getLabelsForMetric(it)
                    MetricModelWithId(
                        it.id.toString(), it.name, MetricType.getType(it.type), it.value, labels
                    )
                })
            }
        }
    }

    override fun getMetricsFirst(
        limit: Long, callback: (List<MetricModelWithId<out Number>>) -> Unit
    ) {
        with(metricDao) {
            runGetQuery(limit = limit.toString()) { metricEntities ->
                callback(metricEntities.map {
                    val labels = getLabelsForMetric(it)
                    MetricModelWithId(
                        it.id.toString(), it.name, MetricType.getType(it.type), it.value, labels
                    )
                })
            }
        }
    }

    override fun getMetricsAndErrors(
        skipForMetrics: Long, skipForErrors: Long, limit: Long, callback: (
            List<MetricModelWithId<out Number>>, List<ErrorEntity>
        ) -> Unit
    ) {
        getMetricsFirst(skipForMetrics, limit) { metrics ->
            getErrors(skipForErrors, limit) { errors ->
                callback(metrics, errors)
            }
        }
    }

    override fun getMetricsCount(callback: (Long) -> Unit) {
        metricDao.getCount(callback = callback)
    }

    override fun clear() {
        clearErrors()
        clearMetrics()
        clearSnapshots()
    }

    override fun clearMetrics() {
        metricDao.delete(null, null)
        labelDao.delete(null, null)
    }

    override fun resetMetricsFirst(limit: Long) {
        with(metricDao) {
            execSql(
                "UPDATE ${MetricEntity.TABLE_NAME} SET ${MetricEntity.ColumnNames.VALUE}=0" + " WHERE ${MetricEntity.ColumnNames.ID} IN (SELECT ${MetricEntity.ColumnNames.ID} " + "FROM ${MetricEntity.TABLE_NAME} ORDER BY ${MetricEntity.ColumnNames.ID} ASC LIMIT $limit)"
            )
        }
    }

    override fun setMaxErrorCount(maxErrorCount: Long) {
        synchronized(this.maxErrorCount) {
            this.maxErrorCount.set(maxErrorCount)
        }
    }

    override fun saveError(errorEntity: ErrorEntity) {
        with(errorDao) {
            getCount {
                synchronized(maxErrorCount) {
                    if (it >= maxErrorCount.get()) return@getCount
                }
                //TODO (add log if exceeded)
                listOf(errorEntity).insert {
                    //TODO (add log if failed)
                    if (it.isNotEmpty() && it.first()
                            .toLong() > -1
                    ) _storageListeners.forEach { it.onDataChange() }
                }
            }

        }
    }

    override fun getAllErrorsSync(): List<ErrorEntity> {
        return errorDao.getAllSync() ?: listOf()
    }

    override fun getAllErrors(callback: (List<ErrorEntity>) -> Unit) {
        with(errorDao) {
            runGetQuery(
            ) { errorEntities ->
                callback(errorEntities)
            }
        }
    }

    override fun getErrorsFirstSync(limit: Long): List<ErrorEntity> {
        with(errorDao) {
            return runGetQuerySync(limit = limit.toString()) ?: listOf()
        }
    }

    override fun getErrors(skip: Long, limit: Long, callback: (List<ErrorEntity>) -> Unit) {
        with(errorDao) {
            runGetQuery(
                limit = limit.toString(),
                offset = if (skip > 0) skip.toString() else null,
                callback = callback
            )
        }
    }

    override fun getErrorsFirst(limit: Long, callback: (List<ErrorEntity>) -> Unit) {
        with(errorDao) {
            runGetQuery(limit = limit.toString(), callback = callback)
        }
    }

    override fun getErrorsCount(callback: (Long) -> Unit) {
        errorDao.getCount(callback = callback)
    }

    override fun clearErrors() {
        errorDao.delete(null, null)
    }

    override fun clearErrors(ids: Array<Long>) {
        errorDao.delete(
            whereClause = "${
                ErrorEntity.ColumnNames.ID
            } IN (${ids.joinToString(",") { it.toString() }})", null
        )

    }

    override fun clearErrorsSync(ids: Array<Long>) {
        errorDao.deleteSync(
            whereClause = "${
                ErrorEntity.ColumnNames.ID
            } IN (${ids.joinToString(",") { it.toString() }})", null
        )
    }

    override fun saveSnapshot(snapshot: Snapshot, callback: ((Long) -> Unit)?) {
        with(snapshotDao) {
            val snapshotEntity = SnapshotEntity(
                snapshot
            )
            listOf(snapshotEntity).insert {
                callback?.invoke(it.firstOrNull()?:0L)
            }
        }
    }

    override fun saveSnapshotSync(snapshot: Snapshot) : Long {
        with(snapshotDao){
            val snapshotEntity = SnapshotEntity(
                snapshot
            )
            return listOf(snapshotEntity).insertSync()?.firstOrNull() ?: -1L
        }
    }

    override fun getAllSnapshotsSync(): List<Snapshot> {
        return snapshotDao.getAllSync()?.map {
            it.toSnapshot()
        } ?: listOf()
    }

    override fun getAllSnapshots(callback: (List<Snapshot>) -> Unit) {
        with(snapshotDao) {
            runGetQuery { snapshotEntities ->
                callback(snapshotEntities.map {
                    it.toSnapshot()
                })
            }
        }
    }

    override fun getSnapshots(limit: Long, offset: Int): List<Snapshot> {
        return snapshotDao.runGetQuerySync(limit = limit.toString(), offset = offset.toString())
            ?.map {
                it.toSnapshot()
            } ?: listOf()
    }

    override fun deleteSnapshots(snapshotIds: List<String>, callback: ((Int) -> Unit)?) {
        snapshotDao.delete(
            whereClause = "${
                SnapshotEntity.ColumnNames.ID
            } IN (${snapshotIds.joinToString(",") { "'$it'" }})", null
        )
    }

    override fun deleteSnapshotsSync(snapshotIds: List<String>): Int {
        return snapshotDao.deleteSync(
            "${
                SnapshotEntity.ColumnNames.ID
            } IN (${snapshotIds.joinToString(",") { "'$it'" }})", null)
    }

    override fun clearSnapshots() {
        snapshotDao.delete(null, null)
    }


    override fun resetTillSync(dumpedMetrics: List<MetricModelWithId<out Number>>) {
        with(metricDao) {
//            dbExecutor?.execute {
//            execTransaction {
            dumpedMetrics.forEach { metric ->
                execSqlSync(
                    "UPDATE ${MetricEntity.TABLE_NAME} " + "SET ${MetricEntity.ColumnNames.VALUE}=CASE WHEN ${
                        MetricEntity.ColumnNames.VALUE
                    }>${metric.value} THEN" + " (${
                        MetricEntity.ColumnNames.VALUE
                    }-${metric.value.toLong().coerceAtLeast(0L)}) ELSE 0 END " + " WHERE " + "${
                        MetricEntity.ColumnNames.ID
                    }='${metric.id}'"

                )
            }
//            }
        }
    }

    override fun reset() {
        with(metricDao) {
            execSql("UPDATE ${MetricEntity.TABLE_NAME} SET ${MetricEntity.ColumnNames.VALUE}=0")
        }
    }

    override fun getAllMetrics(callback: (List<MetricModelWithId<out Number>>) -> Unit) {
        with(metricDao) {
            getAll { metricEntities ->
                metricEntities.map {
                    val labels = getLabelsForMetric(it)
                    MetricModelWithId(
                        it.id.toString(), it.name, MetricType.getType(it.type), it.value, labels
                    )
                }.let {
                    callback(it)
                }
            }
        }
    }

    override fun getAllMetricsSync(): List<MetricModelWithId<out Number>> {
        return with(metricDao) {
            val metricEntities = getAllSync()
            metricEntities?.map {
                val labels = getLabelsForMetric(it)
                MetricModelWithId(
                    it.id.toString(), it.name, MetricType.getType(it.type), it.value, labels
                )
            }
        } ?: listOf()
    }

    private fun getLabelsForMetric(metricEntity: MetricEntity): Map<String, String> {
        val labelMask = metricEntity.label
        val labelIds = mutableListOf<Long>()
        try {
            if (labelMask.isEmpty()) {
                return mapOf()
            }
            var labels = labelMask.toLong()
            var pos = 0
            while (labels > 0) {
                val isPositionToBeTaken = labels and 1
                if (isPositionToBeTaken > 0) {
                    labelIds.add(pos.toLong())
                }
                ++pos
                labels = labels shr 1
            }

        } catch (ex: Exception) {
            var pos = 0
            var labels = BigInteger(labelMask)
            while (labels > BigInteger.ZERO) {
                val isPositionToBeTaken = labels and 1.toBigInteger()
                if (isPositionToBeTaken.toInt() > 0) {
                    labelIds.add(2.0.pow(pos).toLong())
                }
                ++pos
                labels = labels shr 1
            }
        }
        if (labelIds.isEmpty()) {
            return mapOf()
        }
        with(labelDao) {
            return runGetQuerySync(
                selection = "${LabelEntity.Columns.ID} IN (${labelIds.joinToString(",") { "'${it}'" }})"
            )?.associate {
                it.name to it.value
            } ?: mapOf()
        }
    }

    @VisibleForTesting
    fun shutDownDatabase() {
        RudderDatabase.shutDown()
    }

    companion object {
        private const val DB_VERSION = 1
        private const val MAX_ERROR_COUNT = 1000L
    }
}