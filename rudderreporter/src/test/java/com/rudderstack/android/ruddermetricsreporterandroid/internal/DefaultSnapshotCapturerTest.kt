/*
 * Creator: Debanjan Chatterjee on 10/11/23, 10:55 am Last modified: 10/11/23, 10:55 am
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
import com.rudderstack.android.ruddermetricsreporterandroid.SnapshotCreator
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator.getMetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator.mockSnapshot
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.anyArray
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class DefaultSnapshotCapturerTest {

    private val mockSnapshotCreator = mock<SnapshotCreator>()
    private val mockReservoir = mock<Reservoir>()
    private val snapshotCapturer = DefaultSnapshotCapturer(mockSnapshotCreator, TestExecutor())

    @Test
    fun testCaptureSnapshotsAndResetReservoirWithSuccessfulSnapshotCapture() {
        val snapshot = mockSnapshot()
        val metricModelWithIdList = listOf(getMetricModelWithId(1))
        whenever(mockSnapshotCreator.createSnapshot(anyList(), anyList())).thenReturn(snapshot)
        whenever(mockReservoir.getMetricsFirstSync(anyLong())).thenReturn(metricModelWithIdList)
        whenever(mockReservoir.getErrorsFirstSync(anyLong())).thenReturn(listOf())
        whenever(mockReservoir.saveSnapshotSync(snapshot)).thenReturn(0L)

        val totalBatches = snapshotCapturer.captureSnapshotsAndResetReservoir(10, mockReservoir)

        assertEquals(1, totalBatches)
        verify(mockReservoir).getMetricsFirstSync(10L)
        verify(mockReservoir).resetTillSync(metricModelWithIdList)
        verify(mockReservoir).clearErrorsSync(emptyArray())
    }

    @Test
    fun testCaptureSnapshotsAndResetReservoirWithFailedSnapshotCapture() {
        val metricModelWithIdList = listOf(getMetricModelWithId(1))

        whenever(mockSnapshotCreator.createSnapshot(anyList(), anyList())).thenReturn(null)
        whenever(mockReservoir.getMetricsFirstSync(anyLong())).thenReturn(metricModelWithIdList)
        whenever(mockReservoir.getErrorsFirstSync(anyLong())).thenReturn(listOf())

        val totalBatches = snapshotCapturer.captureSnapshotsAndResetReservoir(10, mockReservoir)

        assertEquals(0, totalBatches)
        verify(mockReservoir, times(0)).clearErrorsSync(anyArray())
        verify(mockReservoir, times(0)).resetTillSync(anyList())
    }

    @Test
    fun testCaptureSnapshotsAndResetReservoirWithConcurrentSnapshotCapture() {
        val threads = mutableListOf<Thread>()
        for (i in 1..10) {
            val thread = Thread { snapshotCapturer.captureSnapshotsAndResetReservoir(10, mockReservoir) }
            threads.add(thread)
            thread.start()
        }

        for (thread in threads) {
            thread.join()
        }

        verify(mockReservoir, times(10)).getMetricsFirstSync(10L)
        verify(mockReservoir, times(10)).getErrorsFirstSync(anyLong())
    }

    @Test
    fun testCaptureSnapshotsAndResetReservoirWithCallbackInvocation() {
        val callbackMock = mock<(Int) -> Unit>()
        snapshotCapturer.captureSnapshotsAndResetReservoir(10, mockReservoir, callbackMock)

        verify(callbackMock, times(1)).invoke(0)
    }
    @Test
    fun testShutdown() {
        val testExecutor = TestExecutor()
        val snapshotCapturer = DefaultSnapshotCapturer(mockSnapshotCreator, testExecutor)

        snapshotCapturer.shutdown()

        assertTrue(testExecutor.isTerminated)
    }
}