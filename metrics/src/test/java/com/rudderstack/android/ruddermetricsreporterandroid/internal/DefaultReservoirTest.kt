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
import com.rudderstack.android.ruddermetricsreporterandroid.internal.metrics.DefaultAggregatorHandler
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Labels
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import org.hamcrest.Matchers
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
        const val MAX_COUNTERS = 100
        const val MAX_LABEL_MAP_COUNT = 2000
        const val MAX_LABELS = 100

    }
    private lateinit var defaultStorage : DefaultReservoir



    //create 200 metric
    private lateinit var testNameCounterMap : Map<String, LongCounter>
    //consider 1000 labels
//    private val testLabelMaps = (0 until MAX_LABEL_MAP_COUNT).map {
//        "testLabel_key_$it" to "testLabel_value_$it"
//    }.toMap()
    private lateinit var testLabels : List<Labels>



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
            val randomNumberOfPairs = kotlin.random.Random.Default.nextInt(
                0,
                MAX_LABEL_MAP_COUNT
            )
            (0..randomNumberOfPairs).associate {
                val randomLabelIndex = kotlin.random.Random.nextInt(
                    0,
                    MAX_LABEL_MAP_COUNT
                )
                "testLabel_key_$randomLabelIndex" to "testLabel_value_$randomLabelIndex"
            }.let { Labels.of(it) }
        }
    }
    @Test
    fun insertOrIncrement() {
        val randomCounterToLabelsMap = createTestCounterToLabelsMap()
        randomCounterToLabelsMap.forEach { (counterName, labels) ->
            //insert 1 as default value
            defaultStorage.insertOrIncrement(MetricModel(counterName, MetricType.COUNTER, 1, labels))
        }
        var index = 0
        //increase counters by index
        randomCounterToLabelsMap.forEach { (counterName, labels) ->
            defaultStorage.insertOrIncrement(MetricModel(counterName, MetricType.COUNTER, index.toLong(), labels))
            index++
        }

        val savedData = defaultStorage.getAllMetricsSync()
        savedData.forEachIndexed { index, metric ->
            assertThat(metric.value, Matchers.equalTo(index.toLong() + 1))
            assertThat(metric.name, Matchers.equalTo("testCounter_$index"))
            assertThat(metric.labels, Matchers.equalTo(randomCounterToLabelsMap[metric.name]))
        }

    }

    private fun createTestCounterToLabelsMap() : Map<String, Labels> {
        return testNameCounterMap.map { it.key }.associateWith {
            val randomLabelIndex = Random.nextInt(0, MAX_LABELS)
            testLabels[randomLabelIndex]
        }
    }
}