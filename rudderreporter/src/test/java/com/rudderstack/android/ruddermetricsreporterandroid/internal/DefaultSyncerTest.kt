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
import com.rudderstack.android.ruddermetricsreporterandroid.SnapshotCapturer
import com.rudderstack.android.ruddermetricsreporterandroid.UploadMediator
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity
import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator.mockSnapshot
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito
import org.mockito.Mockito.atMostOnce
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class DefaultSyncerTest {

    val reservoir = Mockito.mock(Reservoir::class.java)
    val uploader = Mockito.mock(UploadMediator::class.java)
    val snapshotCapturer: SnapshotCapturer = Mockito.mock(SnapshotCapturer::class.java)

    @Test
    fun testFlushAllMetricsWithEmptyReservoir() {
        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)

        syncer.flushAllMetrics()

        verify(reservoir).getSnapshots(1)
        verifyNoInteractions(uploader)
        verifyNoInteractions(snapshotCapturer)
    }

    @Test
    fun testFlushAllMetricsWithSuccessfulUpload() {
        val snapshot = mockSnapshot()
        whenever(reservoir.getSnapshots(1, 0)).thenReturn(listOf(snapshot))
        whenever(uploader.upload(any(), any())).then {
            assertThat(it.arguments[0], `is`(snapshot))
            val callback = it.arguments[1] as ((Boolean) -> Unit)
            //for subsequent calls snapshot should be empty
            whenever(reservoir.getSnapshots(1, 0)).thenReturn(listOf())
            callback.invoke(true)
        }
        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)

        syncer.flushAllMetrics()

        verify(uploader).upload(eq(snapshot), any())
        verify(reservoir).deleteSnapshots(eq(listOf(snapshot.id)))
    }
    @Test
    fun testFlushAllMetricsWithUploadFailure() {
        val snapshot = mockSnapshot()
        whenever(reservoir.getSnapshots(1, 0)).thenReturn(listOf(snapshot))
        whenever(uploader.upload(any(), any())).then {
            assertThat(it.arguments[0], `is`(snapshot))
            val callback = it.arguments[1] as ((Boolean) -> Unit)
            callback.invoke(false)
        }
        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)

        syncer.flushAllMetrics()

        verify(uploader).upload(eq(snapshot), any())
        verify(reservoir, never()).deleteSnapshots(anyList())
    }
    @Test
    fun testFlushAllMetricsWithConcurrentCalls() {
        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)

        // Simulate concurrent calls by creating multiple threads
        val threads = mutableListOf<Thread>()
        for (i in 1..10) {
            val thread = Thread { syncer.flushAllMetrics() }
            threads.add(thread)
            thread.start()
        }

        for (thread in threads) {
            thread.join()
        }

        // Verify that only one thread was actively flushing snapshots at a time
        verify(reservoir, atMostOnce()).getMetricsFirstSync(anyLong())
        verify(uploader, atMostOnce()).upload(any(), any())
        verify(snapshotCapturer, atMostOnce()).captureSnapshotsAndResetReservoir(anyLong(), any(), any())
    }

    @Test
    fun testFlushAllMetricsWithSnapshotCaptureFailure() {
        whenever(snapshotCapturer.captureSnapshotsAndResetReservoir(anyLong(), any(), any())).then{
            val callback = it.arguments[2] as ((Int) -> Unit)
            callback.invoke(0)
        }
        whenever(reservoir.getSnapshots(1)).thenReturn(listOf())

        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)
        syncer.flushAllMetrics()
        verify(snapshotCapturer).captureSnapshotsAndResetReservoir(anyLong(), any(), any())

        verifyNoInteractions(uploader)
    }
    @Test
    fun testStopScheduling() {
        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)

        syncer.stopScheduling()
        verify(snapshotCapturer).shutdown()
    }
    @Test
    fun testCallbackAfterSuccessfulUpload() {
        val snapshot = mockSnapshot()
        val callback: (Snapshot, Boolean) -> Unit = mock()
        whenever(snapshotCapturer.captureSnapshotsAndResetReservoir(anyLong(), any(), any())).then{
            val callback = it.arguments[2] as ((Int) -> Unit)
            callback.invoke(0)
        }
        whenever(reservoir.getSnapshots(1)).thenReturn(listOf(snapshot))
        whenever(uploader.upload(any(), any())).then {
            assertThat(it.arguments[0], `is`(snapshot))
            val callback = it.arguments[1] as ((Boolean) -> Unit)
            whenever(reservoir.getSnapshots(1)).thenReturn(listOf())

            callback.invoke(true)
        }
        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)
        syncer.setCallback(callback)

        syncer.flushAllMetrics()

        verify(callback, times(1)).invoke(snapshot, true)
    }
    @Test
    fun testCallbackAfterFailedlUpload() {
        val snapshot = mockSnapshot()
        val callback: (Snapshot, Boolean) -> Unit = mock()
        whenever(snapshotCapturer.captureSnapshotsAndResetReservoir(anyLong(), any(), any())).then{
            val callback = it.arguments[2] as ((Int) -> Unit)
            callback.invoke(0)
        }
        whenever(reservoir.getSnapshots(1)).thenReturn(listOf(snapshot))
        whenever(uploader.upload(any(), any())).then {
            assertThat(it.arguments[0], `is`(snapshot))
            val callback = it.arguments[1] as ((Boolean) -> Unit)
            whenever(reservoir.getSnapshots(1)).thenReturn(listOf())

            callback.invoke(false)
        }
        val syncer = DefaultPeriodicSyncer(reservoir, uploader, snapshotCapturer)
        syncer.setCallback(callback)

        syncer.flushAllMetrics()

        verify(callback, times(1)).invoke(snapshot, false)
    }
    @Test
    fun testTimerWithCallbackOnStart() {
        val scheduler = DefaultPeriodicSyncer.Scheduler()
        var schedulerCalled = 0
        scheduler.scheduleTimer(true, 500L) {
            println("timer called")
            schedulerCalled++
        }
        Thread.sleep(2100)
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(5))
    }

    @Test
    fun testTimerWithNoCallbackOnStart() {
        val scheduler = DefaultPeriodicSyncer.Scheduler()
        var schedulerCalled = 0
        scheduler.scheduleTimer(false, 500L) {
            println("timer called")
            schedulerCalled++
        }
        Thread.sleep(2100)
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(4))
    }

    @Test
    fun testTimerStoppedDuringExec() {
        val scheduler = DefaultPeriodicSyncer.Scheduler()
        var schedulerCalled = 0

        scheduler.scheduleTimer(true, 500L) {
            Thread.sleep(200)

            schedulerCalled++
        }
        //should run at 0. 500, 1000, 1500
        Thread.sleep(1100) // stopped during execution.
        scheduler.stop()
        Thread.sleep(1000)
        assertThat(schedulerCalled, `is`(3))
    }




}