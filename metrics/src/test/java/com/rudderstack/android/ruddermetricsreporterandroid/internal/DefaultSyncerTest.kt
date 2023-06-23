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

import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.Uploader
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Labels
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import org.awaitility.Awaitility
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.Assert.*

import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.verify
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DefaultSyncerTest {

    val mockReservoir = Mockito.mock(Reservoir::class.java)
    val mockUploader = Mockito.mock(Uploader::class.java)

    @Test
    fun checkSyncWithSuccess() {
        var lastMetricsIndex = 0
        val limit = 20
        val maxMetrics = 100
        val isDone = AtomicBoolean(false)
        val interval = 1000L
        Mockito.`when`(mockReservoir.getMetricsFirst(Mockito.anyLong(), org.mockito.kotlin.any())).then {
            val callback = it.arguments[1] as ((List<MetricModel<Number>>) -> Unit)
            val metrics = if (lastMetricsIndex + 1 < maxMetrics) getTestMetricList(
                lastMetricsIndex,
                limit
            ) else emptyList()
            lastMetricsIndex += metrics.size
            callback.invoke(metrics)
        }

        Mockito.`when`(mockUploader.upload(org.mockito.kotlin.any(),org.mockito.kotlin.any(), org.mockito.kotlin.any())).then {
            val callback = it.arguments[2] as ((Boolean) -> Unit)
            callback.invoke(true)
        }
        val syncer = DefaultSyncer(mockReservoir, mockUploader)
        var cumulativeIndex = 0
        syncer.setCallback { uploaded, success ->
            println("uploaded  ${uploaded.size} checkedIndex $cumulativeIndex")
            if (cumulativeIndex + 1 >= maxMetrics){
                assert(false) //should not reach here
                isDone.set(true)
            }else {
                assertThat(
                    uploaded,
                    allOf(
                        notNullValue(),
                        not(empty()),
                        hasSize(limit),
                        Matchers.contains<MetricModel<out Number>>(*(getTestMetricList(cumulativeIndex, limit).toTypedArray()))
                    )
                )
                if (cumulativeIndex + uploaded.size + 1 >= maxMetrics) {
                    Thread.sleep(1000)
                    isDone.set(true)
                }
            }
            cumulativeIndex += uploaded.size


        }
        syncer.startScheduledSyncs(interval, true, limit.toLong())

        Awaitility.await().untilTrue(isDone)

    }
    @Test
    fun testTimerWithCallbackOnStart(){
        val scheduler = DefaultSyncer.Scheduler()
        var schedulerCalled = 0
        scheduler.scheduleTimer(true, 500L){
            println("timer called")
            schedulerCalled ++
        }
        Thread.sleep(2100)
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(5))
    }
    @Test
    fun testTimerWithNoCallbackOnStart(){
        val scheduler = DefaultSyncer.Scheduler()
        var schedulerCalled = 0
        scheduler.scheduleTimer(false, 500L){
            println("timer called")
            schedulerCalled ++
        }
        Thread.sleep(2100)
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(4))
    }
    @Test
    fun testTimerStoppedDuringExec(){
        val scheduler = DefaultSyncer.Scheduler()
        var schedulerCalled = 0

        scheduler.scheduleTimer(true, 500L){
            Thread.sleep(200)

            schedulerCalled ++
        }
        //should run at 0. 500, 1000, 1500
        Thread.sleep(1100) // stopped during execution.
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(3))
    }
    private fun getTestMetricList(startIndex: Int, limit: Int): List<MetricModel<Number>> {
        return (startIndex until startIndex + limit).map {
            MetricModel(
                "testMetric$it",
                MetricType.COUNTER,
                it.toLong(),
                Labels.of("testLabel_$it" to "testValue_$it")
            )
        }
    }

    @Test
    fun stopScheduling() {
    }
}