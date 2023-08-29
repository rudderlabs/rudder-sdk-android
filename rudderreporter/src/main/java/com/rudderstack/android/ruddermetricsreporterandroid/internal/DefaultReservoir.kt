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
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.LabelEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.MetricEntity
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.ExecutorService
import kotlin.math.pow

class DefaultReservoir(
    androidContext: Context, useContentProvider: Boolean,
    private val dbExecutor: ExecutorService? = null
) :
    Reservoir {
    private val dbName = "metrics_db_${androidContext.packageName}.db"
    private val metricDao: Dao<MetricEntity>
    private val labelDao: Dao<LabelEntity>
    private var _storageListeners = listOf<Reservoir.DataListener>()

    init {
        RudderDatabase.init(
            androidContext, dbName, DefaultEntityFactory(), useContentProvider,
            DB_VERSION, dbExecutor
        )
        metricDao = RudderDatabase.getDao(MetricEntity::class.java)
        labelDao = RudderDatabase.getDao(LabelEntity::class.java)
    }

    override fun insertOrIncrement(
        metric: MetricModel<Number>
    ) {
        val labels = metric.labels.map { LabelEntity(it.key, it.value) }
        if (labels.isEmpty()) {
            insertCounterWithLabelMask(metric, "")
            return
        }

        with(labelDao) {
            labels.insertWithDataCallback(
                conflictResolutionStrategy =
                Dao.ConflictResolutionStrategy.CONFLICT_IGNORE
            ) { rowIds: List<Long>,
                insertedData: List<LabelEntity?> ->
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
                        getLabelMaskForMetricWithLong(insertedIds).also {
                            println("label mask for metric ${metric.name} and labels $insertedIds is $it")
                        }
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
        rowIds: List<Long>,
        insertedData: List<LabelEntity?>,
        queryData: List<LabelEntity>
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
        metric: MetricModel<Number>,
        labelMaskForMetric: String
    ) {
        val metricEntity = MetricEntity(
            metric.name,
            metric.value.toLong(),
            MetricType.COUNTER.value,
            labelMaskForMetric
        )
        with(metricDao) {
            val insertedRowId = listOf(metricEntity).insertSync(
                conflictResolutionStrategy =
                Dao.ConflictResolutionStrategy.CONFLICT_IGNORE
            )?.firstOrNull()
            if (insertedRowId == -1L) {
                this.execSqlSync(
                    "UPDATE " + MetricEntity.TABLE_NAME + " SET " +
                            MetricEntity.ColumnNames.VALUE + " = (" + MetricEntity.ColumnNames.VALUE + " + " + metric.value +
                            ") WHERE " + MetricEntity.ColumnNames.NAME + "='" + metric.name + "'"
                            + " AND " + MetricEntity.ColumnNames.LABEL + "='" + labelMaskForMetric + "'"
                            + " AND " + MetricEntity.ColumnNames.TYPE + "='" + MetricType.COUNTER.value + "'"
                            + ";"
                )
            } 
        }
    }

    override fun getMetricsFirstSync(limit: Long): List<MetricModelWithId<out Number>> {
        with(metricDao) {
            val metricEntities = runGetQuerySync(limit = limit.toString())
            return metricEntities?.map {
                val labels = getLabelsForMetric(it)
                MetricModelWithId(
                    it.id.toString(),
                    it.name,
                    MetricType.getType(it.type),
                    it.value,
                    labels
                )
            } ?: listOf()
        }
    }

    override fun getMetricsFirst(
        skip: Long,
        limit: Long,
        callback: (List<MetricModelWithId<out Number>>) -> Unit
    ) {
        with(metricDao) {
            runGetQuery(
                limit = limit.toString(),
                offset = if (skip > 0) skip.toString() else null
            ) { metricEntities ->
                callback(metricEntities.map {
                    val labels = getLabelsForMetric(it)
                    MetricModelWithId(
                        it.id.toString(), it.name, MetricType.getType(it.type),
                        it.value, labels
                    )
                })
            }
        }
    }

    override fun getMetricsFirst(
        limit: Long,
        callback: (List<MetricModelWithId<out Number>>) -> Unit
    ) {
        with(metricDao) {
            runGetQuery(limit = limit.toString()) { metricEntities ->
                callback(metricEntities.map {
                    val labels = getLabelsForMetric(it)
                    MetricModelWithId(
                        it.id.toString(), it.name, MetricType.getType(it.type),
                        it.value, labels
                    )
                })
            }
        }
    }

    override fun getMetricsCount(callback: (Long) -> Unit) {
        metricDao.getCount(callback = callback)
    }

    override fun clear() {
        metricDao.delete(null, null)
        labelDao.delete(null, null)
    }

    override fun resetFirst(limit: Long) {
        with(metricDao) {
            execSql(
                "UPDATE ${MetricEntity.TABLE_NAME} SET ${MetricEntity.ColumnNames.VALUE}=0" +
                        " WHERE ${MetricEntity.ColumnNames.ID} IN (SELECT ${MetricEntity.ColumnNames.ID} " +
                        "FROM ${MetricEntity.TABLE_NAME} ORDER BY ${MetricEntity.ColumnNames.ID} ASC LIMIT $limit)"
            )
        }
    }

    override fun resetTillSync(dumpedMetrics: List<MetricModelWithId<out Number>>) {
        with(metricDao) {
//            dbExecutor?.execute {
            beginTransaction()
            dumpedMetrics.forEach { metric ->

                execSqlSync(
                    "UPDATE ${MetricEntity.TABLE_NAME} " +
                            "SET ${MetricEntity.ColumnNames.VALUE}=${MetricEntity.ColumnNames.VALUE}-${metric.value}" +
                            " WHERE ${MetricEntity.ColumnNames.ID}='${metric.id}'"
                )
            }
            setTransactionSuccessful()
            endTransaction()
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
                        it.id.toString(),
                        it.name,
                        MetricType.getType(it.type),
                        it.value,
                        labels
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
                    it.id.toString(),
                    it.name, MetricType.getType(it.type), it.value, labels
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
    }
}