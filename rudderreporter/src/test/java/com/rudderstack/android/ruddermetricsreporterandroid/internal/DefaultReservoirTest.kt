/*
 * Creator: Debanjan Chatterjee on 16/06/23, 9:07 pm Last modified: 16/06/23, 9:07 pm
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

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.repository.Dao
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.android.ruddermetricsreporterandroid.internal.metrics.DefaultAggregatorHandler
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.LabelEntity
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class DefaultReservoirTest {
    companion object{
        const val MAX_COUNTERS = 20
        const val MAX_LABEL_MAP_COUNT = 20
        const val MAX_LABELS = 10

    }
    private lateinit var defaultStorage : DefaultReservoir

    //create 200 metric
    private lateinit var testNameCounterMap : Map<String, LongCounter>
    //consider 1000 labels
//    private val testLabelMaps = (0 until MAX_LABEL_MAP_COUNT).map {
//        "testLabel_key_$it" to "testLabel_value_$it"
//    }.toMap()
    private lateinit var testLabels : List<Labels>
    private lateinit var testCounterToLabelMap : Map<String, Labels>


    @Before
    fun initialize() {
        defaultStorage = DefaultReservoir(ApplicationProvider.getApplicationContext(), false,
            TestExecutor())

        testNameCounterMap = (0 until MAX_COUNTERS).associate {
            val name = "testCounter_$it"
            name to LongCounter(
                name,
                DefaultAggregatorHandler(
                    defaultStorage
                )
            )
        }
        testLabels = (0..MAX_LABELS).map {
            val randomNumberOfPairs = Random.Default.nextInt(
                0,
                MAX_LABEL_MAP_COUNT
            )
            (0..randomNumberOfPairs).associate {
                val randomLabelIndex = Random.nextInt(
                    0,
                    MAX_LABEL_MAP_COUNT
                )
                "testLabel_key_$randomLabelIndex" to "testLabel_value_$randomLabelIndex"
            }.let { it }
        }
        testCounterToLabelMap =  testNameCounterMap.map { it.key }.associateWith {
            val randomLabelIndex = Random.nextInt(0, MAX_LABELS)
            testLabels[randomLabelIndex]
        }
    }
    @After
    fun destroy(){
        defaultStorage.clear()
        defaultStorage.shutDownDatabase()
    }
    @Test
    fun insertOrIncrement() {
        testCounterToLabelMap.forEach { (counterName, labels) ->
            println("inserting first time for $counterName, $labels")
            //insert 1 as default value
            defaultStorage.insertOrIncrement(MetricModel(counterName, MetricType.COUNTER, 1, labels))
        }
        var index = 0
        //increase counters by index
        testCounterToLabelMap.forEach { (counterName, labels) ->
            println("inserting second time for $counterName, $labels")
            defaultStorage.insertOrIncrement(MetricModel(counterName, MetricType.COUNTER, index.toLong(), labels))

            index++
        }

        val savedData = defaultStorage.getAllMetricsSync()
        assertThat(savedData.size, Matchers.equalTo(MAX_COUNTERS ))
        savedData.forEachIndexed { index, metric ->
            assertThat(metric.value, Matchers.equalTo(index.toLong() + 1))
            assertThat(metric.name, Matchers.equalTo("testCounter_$index"))
            assertThat(metric.labels, Matchers.equalTo(testCounterToLabelMap[metric.name]))
        }

    }
    @Test
    fun `test duplicate label insertion`(){
        val labelEntities = (0..50).map{
            LabelEntity("testLabel_key_$it", "testLabel_value_$it")
        }
        val labelDao = RudderDatabase.getDao(LabelEntity::class.java)
        with(labelDao){
           val insertedIds = labelEntities.insertSync(conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_IGNORE)
            assertThat(insertedIds?.size, Matchers.equalTo(51))
            assertThat(insertedIds, Matchers.not(Matchers.contains(-1)))

            val duplicateIds = labelEntities.insertSync(conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_IGNORE)?.toSet()
            assertThat(duplicateIds?.size, Matchers.equalTo(1))
            assertThat(duplicateIds, Matchers.contains(-1))
        }
    }
    @Test
    fun `test insertion and reset for all`(){
        testCounterToLabelMap.forEach { (counterName, labels) ->
            //insert 1 as default value
            defaultStorage.insertOrIncrement(MetricModel(counterName, MetricType.COUNTER, 1, labels))
        }
        var index = 0
        //increase counters by index
        testCounterToLabelMap.forEach { (counterName, labels) ->
            defaultStorage.insertOrIncrement(MetricModel(counterName, MetricType.COUNTER, index.toLong(), labels))
            index++
        }
        val allMetrics = defaultStorage.getAllMetricsSync()
        allMetrics.forEachIndexed { index, metric ->
            assertThat(metric.value, Matchers.equalTo(index.toLong() + 1))
            assertThat(metric.name, Matchers.equalTo("testCounter_$index"))
            assertThat(metric.labels, Matchers.equalTo(testCounterToLabelMap[metric.name]))
        }
        defaultStorage.reset()
        val allResetMetrics = defaultStorage.getAllMetricsSync()
        allResetMetrics.forEachIndexed { index, metric ->
            assertThat(metric.value, Matchers.equalTo(0L))
            assertThat(metric.name, Matchers.equalTo("testCounter_$index"))
            assertThat(metric.labels, Matchers.equalTo(testCounterToLabelMap[metric.name]))
        }

    }

}

typealias Labels = Map<String, String>