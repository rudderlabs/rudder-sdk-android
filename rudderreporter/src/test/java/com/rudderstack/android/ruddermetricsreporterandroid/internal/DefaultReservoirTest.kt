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
import com.rudderstack.android.ruddermetricsreporterandroid.TEST_ERROR_EVENTS_JSON
import com.rudderstack.android.ruddermetricsreporterandroid.internal.metrics.DefaultAggregatorHandler
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.LabelEntity
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`in`
import org.hamcrest.Matchers.lessThan
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
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
    companion object {
        const val MAX_COUNTERS = 20
        const val MAX_LABEL_MAP_COUNT = 20
        const val MAX_LABELS = 10

    }

    private lateinit var defaultStorage: DefaultReservoir

    //create 200 metric
    private lateinit var testNameCounterMap: Map<String, LongCounter>

    //consider 1000 labels
//    private val testLabelMaps = (0 until MAX_LABEL_MAP_COUNT).map {
//        "testLabel_key_$it" to "testLabel_value_$it"
//    }.toMap()
    private lateinit var testLabels: List<Labels>
    private lateinit var testCounterToLabelMap: Map<String, Labels>


    @Before
    fun initialize() {
        defaultStorage = DefaultReservoir(
            ApplicationProvider.getApplicationContext(), false, TestExecutor()
        )

        testNameCounterMap = (0 until MAX_COUNTERS).associate {
            val name = "testCounter_$it"
            name to LongCounter(
                name, DefaultAggregatorHandler(
                    defaultStorage
                )
            )
        }
        testLabels = (0..MAX_LABELS).map {
            val randomNumberOfPairs = Random.Default.nextInt(
                0, MAX_LABEL_MAP_COUNT
            )
            (0..randomNumberOfPairs).associate {
                val randomLabelIndex = Random.nextInt(
                    0, MAX_LABEL_MAP_COUNT
                )
                "testLabel_key_$randomLabelIndex" to "testLabel_value_$randomLabelIndex"
            }.let { it }
        }
        testCounterToLabelMap = testNameCounterMap.map { it.key }.associateWith {
            val randomLabelIndex = Random.nextInt(0, MAX_LABELS)
            testLabels[randomLabelIndex]
        }
    }

    @After
    fun destroy() {
        defaultStorage.clear()
        defaultStorage.shutDownDatabase()
    }

    @Test
    fun insertOrIncrement() {
        testCounterToLabelMap.forEach { (counterName, labels) ->
            //insert 1 as default value
            defaultStorage.insertOrIncrement(
                MetricModel(
                    counterName, MetricType.COUNTER, 1, labels
                )
            )
        }
        var index = 0
        //increase counters by index
        testCounterToLabelMap.forEach { (counterName, labels) ->
            defaultStorage.insertOrIncrement(
                MetricModel(
                    counterName, MetricType.COUNTER, index.toLong(), labels
                )
            )

            index++
        }

        val savedData = defaultStorage.getAllMetricsSync()
        assertThat(savedData.size, Matchers.equalTo(MAX_COUNTERS))
        savedData.forEachIndexed { index, metric ->
            assertThat(metric.value, Matchers.equalTo(index.toLong() + 1))
            assertThat(metric.name, Matchers.equalTo("testCounter_$index"))
            assertThat(metric.labels, Matchers.equalTo(testCounterToLabelMap[metric.name]))
        }

    }

    @Test
    fun `test duplicate label insertion`() {
        val labelEntities = (0..50).map {
            LabelEntity("testLabel_key_$it", "testLabel_value_$it")
        }
        val labelDao = RudderDatabase.getDao(LabelEntity::class.java)
        with(labelDao) {
            val insertedIds =
                labelEntities.insertSync(conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_IGNORE)
            assertThat(insertedIds?.size, Matchers.equalTo(51))
            assertThat(insertedIds, Matchers.not(Matchers.contains(-1)))

            val duplicateIds =
                labelEntities.insertSync(conflictResolutionStrategy = Dao.ConflictResolutionStrategy.CONFLICT_IGNORE)
                    ?.toSet()
            assertThat(duplicateIds?.size, Matchers.equalTo(1))
            assertThat(duplicateIds, Matchers.contains(-1))
        }
    }

    @Test
    fun `test insertion and reset for all`() {
        testCounterToLabelMap.forEach { (counterName, labels) ->
            //insert 1 as default value
            defaultStorage.insertOrIncrement(
                MetricModel(
                    counterName, MetricType.COUNTER, 1, labels
                )
            )
        }
        var index = 0
        //increase counters by index
        testCounterToLabelMap.forEach { (counterName, labels) ->
            defaultStorage.insertOrIncrement(
                MetricModel(
                    counterName, MetricType.COUNTER, index.toLong(), labels
                )
            )
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

    @Test
    fun `test getMetricsAndErrors when metrics are null`() {
        val errorEntity = ErrorEntity(TEST_ERROR_EVENTS_JSON)
        defaultStorage.clear()
        defaultStorage.saveError(errorEntity)
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, Matchers.empty())
            assertThat(
                errors, allOf(
                    hasSize(1), Matchers.contains(
                        allOf(
                            hasProperty("errorEvent", Matchers.equalTo(TEST_ERROR_EVENTS_JSON))
                        )
                    )
                )
            )
        }

    }

    @Test
    fun `test getMetricsAndErrors when skip is greater than 0`() {
        defaultStorage.clear()
        TestDataGenerator.generateTestErrorEventsJson(12).forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.getMetricsAndErrors(5, 5, 5) { metrics, errors ->
            assertThat(metrics, Matchers.empty())
            assertThat(
                errors, allOf(
                    hasSize(5), everyItem(
                        anyOf(
                            hasProperty(
                                "errorEvent", Matchers.equalTo(
                                    TestDataGenerator.getTestErrorEventJsonWithIdentity(6)
                                )
                            ), hasProperty(
                                "errorEvent", Matchers.equalTo(
                                    TestDataGenerator.getTestErrorEventJsonWithIdentity(7)
                                )
                            ), hasProperty(
                                "errorEvent", Matchers.equalTo(
                                    TestDataGenerator.getTestErrorEventJsonWithIdentity(8)
                                )
                            ), hasProperty(
                                "errorEvent", Matchers.equalTo(
                                    TestDataGenerator.getTestErrorEventJsonWithIdentity(9)
                                )
                            ), hasProperty(
                                "errorEvent", Matchers.equalTo(
                                    TestDataGenerator.getTestErrorEventJsonWithIdentity(10)
                                )
                            )
                        )
                    )
                )
            )
        }
    }

    fun `test getMetricsAndErrors when errors are null`() {
        defaultStorage.clear()
        defaultStorage.insertOrIncrement(
            MetricModel(
                "testCounter", MetricType.COUNTER, 1, mapOf("label" to "value")
            )
        )
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(
                metrics, allOf(
                    hasSize(1), Matchers.contains(
                        allOf(
                            hasProperty("name", Matchers.equalTo("testCounter"))
                        )
                    )
                )
            )
            assertThat(errors, Matchers.empty())
        }
    }

    @Test
    fun `test getMetricsAndErrors when limit exceeds metrics count`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(20)
        val insertedErrors = TestDataGenerator.generateTestErrorEventsJson(32)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        insertedErrors.forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.getMetricsAndErrors(0, 0, 30L) { metrics, errors ->
            assertThat(
                metrics, allOf(
                    hasSize(20), everyItem(
                        allOf(
                            hasProperty(
                                "name", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.name) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "type", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.type) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "labels", hasEntry(
                                    anyOf(
                                        (insertMetricModels.map {
                                            equalTo(
                                                it.labels.keys.toList()[0]
                                            )
                                        })

                                    ), anyOf(
                                        *(insertMetricModels.map {
                                            equalTo(
                                                it.labels.values.toList()[0]
                                            )
                                        }).toTypedArray()
                                    )
                                )
                            ),

                            )
                    )
                )
            )
            assertThat(
                errors, allOf(
                    hasSize(12), everyItem(
                        hasProperty(
                            "errorEvent", anyOf(
                                *(TestDataGenerator.generateTestErrorEventsJson(30).map {
                                    Matchers.equalTo(it)
                                }).toTypedArray()
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test getMetricsAndErrors when limit exceeds errors count`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(12)
        val insertedErrors = TestDataGenerator.generateTestErrorEventsJson(20)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        insertedErrors.forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.getMetricsAndErrors(0, 0, 30L) { metrics, errors ->
            assertThat(
                metrics, allOf(
                    hasSize(12), everyItem(
                        allOf(
                            hasProperty(
                                "name", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.name) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "type", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.type) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "labels", hasEntry(
                                    anyOf(
                                        (insertMetricModels.map {
                                            equalTo(
                                                it.labels.keys.toList()[0]
                                            )
                                        })

                                    ), anyOf(
                                        *(insertMetricModels.map {
                                            equalTo(
                                                it.labels.values.toList()[0]
                                            )
                                        }).toTypedArray()
                                    )
                                )
                            ),

                            )
                    )
                )
            )
            assertThat(
                errors, allOf(
                    hasSize(20), everyItem(
                        hasProperty(
                            "errorEvent", anyOf(
                                *(insertedErrors.map { Matchers.equalTo(it) }).toTypedArray()
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test clear when metrics and errors not empty`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(12)
        val insertedErrors = TestDataGenerator.generateTestErrorEventsJson(20)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        insertedErrors.forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.clear()
        defaultStorage.getMetricsAndErrors(0, 0, 30L) { metrics, errors ->
            assertThat(metrics, Matchers.empty())
            assertThat(errors, Matchers.empty())
        }
    }

    @Test
    fun `test resetMetricsFirst when metrics count greater than limit`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(12)
        val insertedErrors = TestDataGenerator.generateTestErrorEventsJson(8)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        insertedErrors.forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.resetMetricsFirst(10)
        defaultStorage.getMetricsAndErrors(0, 0, 10L) { metrics, errors ->
            assertThat(
                metrics, allOf(
                    hasSize(10), everyItem(
                        allOf(
                            hasProperty(
                                "name", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.name) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "value", equalTo(0L)
                            ),
                            hasProperty(
                                "type", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.type) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "labels", hasEntry(
                                    anyOf(
                                        (insertMetricModels.map {
                                            equalTo(
                                                it.labels.keys.toList()[0]
                                            )
                                        })

                                    ), anyOf(
                                        *(insertMetricModels.map {
                                            equalTo(
                                                it.labels.values.toList()[0]
                                            )
                                        }).toTypedArray()
                                    )
                                )
                            ),

                            )
                    )
                )
            )
            assertThat(
                errors, allOf(
                    hasSize(8), everyItem(
                        hasProperty(
                            "errorEvent", anyOf(
                                *(insertedErrors.map { Matchers.equalTo(it) }).toTypedArray()
                            )
                        )
                    )
                )
            )
        }

        val last2Metrics = listOf(
            TestDataGenerator.getTestMetric(11), TestDataGenerator.getTestMetric(12)
        )
        defaultStorage.getMetricsFirst(10, 2) {
            assertThat(
                it, allOf(
                    hasSize(2), everyItem(
                        allOf(
                            hasProperty(
                                "name", anyOf(
                                    *(last2Metrics.map { Matchers.equalTo(it.name) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "value", anyOf(
                                    *last2Metrics.map { Matchers.equalTo(it.value) }.toTypedArray()
                                )
                            ),
                            hasProperty(
                                "type", anyOf(
                                    *(last2Metrics.map { Matchers.equalTo(it.type) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "labels", hasEntry(
                                    anyOf(
                                        (last2Metrics.map {
                                            equalTo(
                                                it.labels.keys.toList()[0]
                                            )
                                        })

                                    ), anyOf(
                                        *(last2Metrics.map {
                                            equalTo(
                                                it.labels.values.toList()[0]
                                            )
                                        }).toTypedArray()
                                    )
                                )
                            ),

                            )
                    )
                )
            )
        }
    }

    @Test
    fun `test resetMetricsFirst when metrics count lesser than limit`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        val insertedErrors = TestDataGenerator.generateTestErrorEventsJson(8)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        insertedErrors.forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.resetMetricsFirst(12)
        defaultStorage.getMetricsAndErrors(0, 0, 12L) { metrics, errors ->
            assertThat(
                metrics, allOf(
                    hasSize(10), everyItem(
                        allOf(
                            hasProperty(
                                "name", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.name) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "value", equalTo(0L)
                            ),
                            hasProperty(
                                "type", anyOf(
                                    *(insertMetricModels.map { Matchers.equalTo(it.type) }).toTypedArray()
                                )
                            ),
                            hasProperty(
                                "labels", hasEntry(
                                    anyOf(
                                        (insertMetricModels.map {
                                            equalTo(
                                                it.labels.keys.toList()[0]
                                            )
                                        })

                                    ), anyOf(
                                        *(insertMetricModels.map {
                                            equalTo(
                                                it.labels.values.toList()[0]
                                            )
                                        }).toTypedArray()
                                    )
                                )
                            ),

                            )
                    )
                )
            )
            assertThat(
                errors, allOf(
                    hasSize(8), everyItem(
                        hasProperty(
                            "errorEvent", anyOf(
                                *(insertedErrors.map { Matchers.equalTo(it) }).toTypedArray()
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test resetTillSync when metrics value more than dumped value`(
    ) {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        val insertedErrors = TestDataGenerator.generateTestErrorEventsJson(8)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        insertedErrors.forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.resetTillSync(defaultStorage.getAllMetricsSync().map {
            MetricModelWithId(
                it.id, it.name, it.type, (it.value.toLong() - 2L), it.labels
            )
        })
        defaultStorage.getMetricsAndErrors(0, 0, 12L) { metrics, errors ->
            metrics.forEach { metricUnderTest ->
                val associatedMetric = insertMetricModels.firstOrNull {
                    it.name == metricUnderTest.name
                }
                assertThat(associatedMetric, not(nullValue()))
                assertThat(metricUnderTest.value.toInt(), allOf(greaterThan(-1), lessThan(3)))
            }
            assertThat(
                errors, allOf(
                    hasSize(8), everyItem(
                        hasProperty(
                            "errorEvent", anyOf(
                                *(insertedErrors.map { Matchers.equalTo(it) }).toTypedArray()
                            )
                        )
                    )
                )
            )
        }
    }

    fun `test resetTillSync when metrics value less than dumped value`(
    ) {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        val insertedErrors = TestDataGenerator.generateTestErrorEventsJson(8)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        insertedErrors.forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.resetTillSync(defaultStorage.getAllMetricsSync().map {
            MetricModelWithId(
                it.id, it.name, it.type, (it.value.toLong() + 2L), it.labels
            )
        })
        defaultStorage.getMetricsAndErrors(0, 0, 12L) { metrics, errors ->
            metrics.forEach { metricUnderTest ->
                val associatedMetric = insertMetricModels.firstOrNull {
                    it.name == metricUnderTest.name
                }
                assertThat(associatedMetric, not(nullValue()))
                assertThat(metricUnderTest.value, Matchers.equalTo(0L))
            }
            assertThat(
                errors, allOf(
                    hasSize(8), everyItem(
                        hasProperty(
                            "errorEvent", anyOf(
                                *(insertedErrors.map { Matchers.equalTo(it) }).toTypedArray()
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test saveError with all fields`() {
        val errorEntity = ErrorEntity(TEST_ERROR_EVENTS_JSON)
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        defaultStorage.saveError(errorEntity)
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, allOf(not(empty()), hasSize(10)))
            assertThat(
                errors, allOf(
                    hasSize(1), Matchers.contains(
                        allOf(
                            hasProperty("errorEvent", Matchers.equalTo(TEST_ERROR_EVENTS_JSON))
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test clearErrors when errors is not empty`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        TestDataGenerator.generateTestErrorEventsJson(10).forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.clearErrors()
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, allOf(not(empty()), hasSize(10)))
            assertThat(errors, Matchers.empty())
        }
    }

    fun `test clearErrors when errors is empty`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        defaultStorage.clearErrors()
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, allOf(not(empty()), hasSize(10)))
            assertThat(errors, Matchers.empty())
        }
    }

    @Test
    fun `test clearErrors with ids when ids is empty`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        TestDataGenerator.generateTestErrorEventsJson(10).forEach {
            defaultStorage.saveError(ErrorEntity(it))
        }
        defaultStorage.clearErrors(arrayOf())
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, allOf(not(empty()), hasSize(10)))
            assertThat(errors, allOf(not(empty()), hasSize(10)))
        }
    }

    @Test
    fun `test clearErrors with ids when ids is subset of all errors`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        val errorEntities = TestDataGenerator.generateTestErrorEventsJson(10).map {
            ErrorEntity(it)
        }
        errorEntities.forEach {
            defaultStorage.saveError(it)
        }
        val ids = defaultStorage.getAllErrorsSync().take(5).map { it.id }.toTypedArray()
        defaultStorage.clearErrors(ids)
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, allOf(not(empty()), hasSize(10)))
            assertThat(
                errors, allOf(
                    not(empty()), hasSize(5), everyItem(hasProperty("id", not(`in`(ids))))
                )
            )
        }
    }

    @Test
    fun `test clearErrors with ids when ids contain foreign ids`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }
        val errorEntities = TestDataGenerator.generateTestErrorEventsJson(10).map {
            ErrorEntity(it)
        }
        errorEntities.forEach {
            defaultStorage.saveError(it)
        }
        val ids = defaultStorage.getAllErrorsSync().take(5).map { it.id }.toTypedArray()
        val foreignIds = arrayOf(78L, 90L, 100L)
        defaultStorage.clearErrors(ids + foreignIds)
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, allOf(not(empty()), hasSize(10)))
            assertThat(
                errors, allOf(
                    not(empty()), hasSize(5), everyItem(hasProperty("id", not(`in`(ids))))
                )
            )
        }
    }

    @Test
    fun `test clearErrors with ids when errors is empty`() {
        defaultStorage.clear()
        val insertMetricModels = TestDataGenerator.generateTestMetrics(10)
        insertMetricModels.forEach {
            defaultStorage.insertOrIncrement(it)
        }

        defaultStorage.clearErrors(arrayOf(78L, 90L, 100L))
        defaultStorage.getMetricsAndErrors(0, 0, 10) { metrics, errors ->
            assertThat(metrics, allOf(not(empty()), hasSize(10)))
            assertThat(errors, empty())
        }
    }
}

typealias Labels = Map<String, String>