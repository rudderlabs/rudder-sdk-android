/*
 * Creator: Debanjan Chatterjee on 23/06/23, 10:34 am Last modified: 23/06/23, 10:34 am
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

import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.UploadMediator
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator
import org.awaitility.Awaitility
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class DefaultSyncerTest {

    val mockReservoir = Mockito.mock(Reservoir::class.java)
    val mockUploader = Mockito.mock(UploadMediator::class.java)
    private val mockLibraryMetadata = LibraryMetadata("test", "1.0.0", "14", "testKey")

    @Test
    fun checkSyncWithSuccess() {
        println("********checkSyncWithSuccess***********")

//        var lastMetricsIndex = 0
        val limit = 20
        val maxMetrics = 110
        val maxErrors = 210
        val isMetricsDone = AtomicBoolean(false)
        val isErrorsDone = AtomicBoolean(false)
        val interval = 200L
        mockTheReservoir(maxMetrics, maxErrors)

        mockTheUploaderToSucceed()
        val syncer = DefaultSyncer(mockReservoir, mockUploader, mockLibraryMetadata)
        var cumulativeIndexMetrics = 0
        var cumulativeIndexMErrors = 0
        syncer.setCallback { uploadedMetrics, uploadedErrorModel, success ->
            println("uploaded-m  ${uploadedMetrics.size} cIndex-m $cumulativeIndexMetrics")
            println("uploaded-e  ${uploadedErrorModel.eventsJson.size} cIndex-e $cumulativeIndexMErrors")
            if (cumulativeIndexMetrics > maxMetrics) {
                assert(false) // should not reach here
                isMetricsDone.set(true)
            } else {
                val expected = getTestMetricList(
                    cumulativeIndexMetrics,
                    (maxMetrics - cumulativeIndexMetrics).coerceAtMost(limit),
                )
                if (expected.isNotEmpty()) {
                    assertThat(
                        uploadedMetrics,
                        allOf(
                            notNullValue(),
                            not(empty()),
                            hasSize((maxMetrics - cumulativeIndexMetrics).coerceAtMost(limit)),
                            Matchers.contains<MetricModel<out Number>>(
                                *(expected.toTypedArray()),
                            ),
                        ),
                    )
                } else {
                    assertThat(uploadedMetrics, empty())
                }
                if (cumulativeIndexMErrors < maxErrors) {
                    val expectedSizeOfErrors = (maxErrors - cumulativeIndexMErrors).coerceAtMost(limit)
                    assertThat(
                        uploadedErrorModel.eventsJson,
                        allOf(
                            notNullValue(),
                            not(empty()),
                            hasSize(expectedSizeOfErrors),
                        ),
                    )
                } else {
                    assertThat(uploadedErrorModel.eventsJson, empty())
                }

                if (cumulativeIndexMetrics + uploadedMetrics.size == maxMetrics) {
//                    Thread.sleep(1000)
                    isMetricsDone.set(true)
                }
                if (cumulativeIndexMetrics + uploadedMetrics.size > maxMetrics) {
                    assert(false) // should not reach here
                    isMetricsDone.set(true)
                }
                if (cumulativeIndexMErrors + uploadedErrorModel.eventsJson.size == maxErrors) {
                    isErrorsDone.set(true)
                }
                if (cumulativeIndexMErrors + uploadedErrorModel.eventsJson.size > maxErrors) {
                    assert(false)
                    isErrorsDone.set(true)
                }
            }
            cumulativeIndexMetrics += uploadedMetrics.size
            cumulativeIndexMErrors += uploadedErrorModel.eventsJson.size
        }
        syncer.startScheduledSyncs(interval, true, limit.toLong())

        Awaitility.await().atMost(4, TimeUnit.MINUTES).until {
            isMetricsDone.get() && isErrorsDone.get()
        }
        syncer.stopScheduling()
        println("********checkSyncWithSuccess***********")
    }

    @Test
    fun `test sync with failure`() {
        println("********checkSyncWithFailure***********")

        val limit = 20
        val maxMetrics = 110
        val maxErrors = 210
        val interval = 200L
        val syncCounter = AtomicInteger(0)
        mockTheReservoir(maxMetrics, maxErrors)

        mockTheUploaderToFail()
        val syncer = DefaultSyncer(mockReservoir, mockUploader, mockLibraryMetadata)
        val expectedMetrics = getTestMetricList(
            0,
            (maxMetrics).coerceAtMost(limit),
        )
        val expectedSizeOfErrors = (maxErrors).coerceAtMost(limit)
        syncer.setCallback { uploadedMetrics, uploadedErrorModel, success ->
            println(
                "success: $success, uploaded metrics size: ${uploadedMetrics.size}, " +
                    "uploaded errors size: ${uploadedErrorModel.eventsJson.size}",
            )
            assertThat(success, `is`(false))
            assertThat(
                uploadedMetrics,
                Matchers.contains<MetricModel<out Number>>(
                    *(expectedMetrics.toTypedArray()),
                ),
            )
            assertThat(
                uploadedErrorModel.eventsJson,
                allOf(
                    notNullValue(),
                    not(empty()),
                    hasSize(expectedSizeOfErrors),
                ),
            )
            // let's wait for 5 calls
            syncCounter.incrementAndGet()
        }
        syncer.startScheduledSyncs(interval, true, limit.toLong())

        Awaitility.await().atMost(2, TimeUnit.MINUTES).untilAtomic(syncCounter, equalTo(5))
        syncer.stopScheduling()
        println("********checkSyncWithFailure***********")
    }

    @Test
    fun stopScheduling() {
        val limit = 20
        val maxMetrics = 110
        val maxErrors = 210
        val interval = 200L
        mockTheReservoir(maxMetrics, maxErrors)

        mockTheUploaderToSucceed()
        val syncer = DefaultSyncer(mockReservoir, mockUploader, mockLibraryMetadata)
        syncer.startScheduledSyncs(interval, true, limit.toLong())
        Thread.sleep(interval / 2) // some time elapse before stopping
        syncer.stopScheduling()
        // waiting to stop
        Thread.sleep(interval + 10)
        syncer.setCallback { _, _, _ ->
            assert(false) // call shouldn't reach here
        }
        Thread.sleep(interval * 5)
    }

    @Test
    fun testTimerWithCallbackOnStart() {
        val scheduler = DefaultSyncer.Scheduler()
        var schedulerCalled = 0
        scheduler.scheduleTimer(true, 500L) {
            schedulerCalled++
        }
        Thread.sleep(2100)
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(5))
    }

    @Test
    fun testTimerWithNoCallbackOnStart() {
        val scheduler = DefaultSyncer.Scheduler()
        var schedulerCalled = 0
        scheduler.scheduleTimer(false, 500L) {
            schedulerCalled++
        }
        Thread.sleep(2100)
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(4))
    }

    @Test
    fun testTimerStoppedDuringExec() {
        val scheduler = DefaultSyncer.Scheduler()
        var schedulerCalled = 0

        scheduler.scheduleTimer(true, 500L) {
            Thread.sleep(200)

            schedulerCalled++
        }
        // should run at 0. 500, 1000, 1500
        Thread.sleep(1100) // stopped during execution.
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(3))
    }

    private fun getTestMetricList(startPos: Int, limit: Int): List<MetricModelWithId<Number>> {
        return (startPos until startPos + limit).map {
            val index = it + 1
            MetricModelWithId(
                (index).toString(),
                "testMetric$it",
                MetricType.COUNTER,
                index.toLong(),
                mapOf("testLabel_$it" to "testValue_$it"),
            )
        }
    }
    private fun mockTheUploaderToSucceed() {
        Mockito.`when`(
            mockUploader.upload(
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
            ),
        ).then {
            val callback = it.arguments[2] as ((Boolean) -> Unit)
            callback.invoke(true)
        }
    }
    private var errorBeginIndex: Int = 0
    private fun mockTheReservoir(maxMetrics: Int, maxErrors: Int) {
        Mockito.`when`(
            mockReservoir.getMetricsAndErrors(
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.anyLong(),
                org.mockito.kotlin.any(),
            ),
        ).then {
            val callback = it.arguments[3] as (
            (
                List<MetricModelWithId<out Number>>,
                List<ErrorEntity>,
            ) -> Unit
            )
            val skipMetrics = (it.arguments[0] as Long).toInt().coerceAtLeast(0)
            val skipError = (it.arguments[1] as Long).toInt().coerceAtLeast(0) + errorBeginIndex
            val limit = (it.arguments[2] as Long).toInt()
            val metrics = if (skipMetrics < maxMetrics) {
                getTestMetricList(
                    skipMetrics,
                    (maxMetrics - skipMetrics).coerceAtMost(limit),
                )
            } else {
                emptyList()
            }
            //            lastMetricsIndex += metrics.size
            callback.invoke(
                metrics,
                TestDataGenerator.generateTestErrorEventsJson(
                    skipError until (maxErrors).coerceAtMost(skipError + limit),
                ).map {
                    ErrorEntity(it)
                },
            )
        }
        Mockito.`when`(mockReservoir.clearErrors(org.mockito.kotlin.any())).then {
            val idsToCLear = it.arguments[0] as Array<Long>
            errorBeginIndex += idsToCLear.size
            Unit
        }
    }
    private fun mockTheUploaderToFail() {
        Mockito.`when`(
            mockUploader.upload(
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
            ),
        ).then {
            val callback = it.arguments[2] as ((Boolean) -> Unit)
            callback.invoke(false)
        }
    }

    @After
    fun tearDown() {
        errorBeginIndex = 0
    }
}
