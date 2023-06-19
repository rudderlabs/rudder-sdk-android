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
import com.rudderstack.android.repository.Dao
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Labels
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.LabelEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.MetricEntity
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.ExecutorService
import kotlin.math.pow

class DefaultReservoir(
    androidContext: Context, useContentProvider: Boolean,
    dbExecutor: ExecutorService? = null
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
        val labels = metric.labels.data.map { LabelEntity(it.key, it.value) }
        with(labelDao) {
            println(labels.joinToString(",") {
                "${it.name} to ${it.value}"
            })
            labels.insertWithDataCallback(conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_IGNORE) { insertedData: Map<Long, LabelEntity?> ->
                if (insertedData.isEmpty()) return@insertWithDataCallback
                println("inserted ids: ${insertedData.keys.joinToString(",")}")
                //callback is done inside executor
                var insertedIds = listOf<Long>()
                insertedData.onEachIndexed  { index, insertedEntry ->
                    val rowId = insertedEntry.key
                    val entry = insertedEntry.value
                    if (rowId == -1L || entry == null) {
                        println("fetching id for ${labels[index].name}")
                        val name = labels[index].name
                        val valueOfLabel = labels[index].value
                        val idOfAlreadyCreatedLabel = runGetQuerySync(
                            selection = "${LabelEntity.Columns.NAME} = ? AND ${LabelEntity.Columns.VALUE} = ?",
                            selectionArgs = arrayOf(name, valueOfLabel)
                        )?.firstOrNull()?.id
                        if (idOfAlreadyCreatedLabel != null) {
                            println("found: $idOfAlreadyCreatedLabel")
                            insertedIds = insertedIds + idOfAlreadyCreatedLabel
                        }
                    } else insertedIds = insertedIds + entry.id
                }

                println("inserted =  $insertedIds")
                val labelMaskForMetric = if (insertedIds.isEmpty()) "" else run {
                    val maxIdInserted = insertedIds.max()
                    val useBigDec = (maxIdInserted >= 63)
                    if (useBigDec) {
                        var labelIdsMask = BigDecimal.ZERO
                        insertedIds.forEach { id ->
                            labelIdsMask += BigDecimal(2).pow(id.toInt())
                        }
                        labelIdsMask.toString()
                    } else {
                        var labelIdsMask = 0L
                        insertedIds.forEach { id ->
                            labelIdsMask += 2.0.pow(id.toDouble()).toLong()
                        }
                        labelIdsMask.toString()
                    }
                }
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
                                    ") WHERE " + MetricEntity.ColumnNames.NAME + "=" + metric.name
                                    + " AND " + MetricEntity.ColumnNames.LABEL + "=" + labelMaskForMetric
                                    + "AND " + MetricEntity.ColumnNames.TYPE + "=" + MetricType.COUNTER.value
                                    + ";"
                        )
                    }
                }
                _storageListeners.forEach { it.onDataChange() }
            }
        }

    }

    override fun getMetricsFirst(limit: Int): List<MetricModel<*>> {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun removeFirst(limit: Int) {
        TODO("Not yet implemented")
    }

    override fun getAllMetrics(callback: (List<MetricModel<Number>>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getAllMetricsSync(): List<MetricModel<Long>> {
        with(metricDao) {
            getAll { metricEntities ->
                metricEntities.map {
                    val labels = getLabelsForMetric(it)
                    MetricModel(it.name, MetricType.getType(it.type), it.value, labels)
                }
            }
        }
        return listOf()
    }

    private fun getLabelsForMetric(metricEntity: MetricEntity): Labels {
        val labelMask = metricEntity.label
        val labelIds = mutableListOf<Long>()
        try {
            if (labelMask.isEmpty()) {
                return Labels.of()
            }
            var labels = labelMask.toLong()
//            val maxPowerOf2 = (ln(labels.toDouble()) / ln(2.0)).toLong()
//            for (i in 0..maxPowerOf2) {
//                val valueAtI = 2.0.pow(i.toDouble()).toLong()
//                if (labels and valueAtI > 0 ) {
//                    labelIds.add(i)
//                }
//            }
            var pos = 0
            while (labels > 0) {
                val isPositionToBeTaken = labels and 1
                if (isPositionToBeTaken > 0) {
                    labelIds.add(2.0.pow(pos).toLong())
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
            return Labels.of()
        }
        with(labelDao) {
            runGetQuerySync(
                selection = "${LabelEntity.Columns.ID} IN (?)",
                selectionArgs = arrayOf(labelIds.joinToString(","))
            )?.map {
                it.name to it.value
            }?.toTypedArray()?.let {
                return Labels.of(*it)
            }
        }
        return Labels.of()
    }

    companion object {
        private const val DB_VERSION = 1
    }
}