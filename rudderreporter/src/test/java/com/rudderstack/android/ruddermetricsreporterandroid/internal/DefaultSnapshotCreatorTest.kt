/*
 * Creator: Debanjan Chatterjee on 10/11/23, 4:24 pm Last modified: 10/11/23, 4:24 pm
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
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorEvent
import com.rudderstack.android.ruddermetricsreporterandroid.utils.NotEmptyStringMatcher
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.isA
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DefaultSnapshotCreatorTest {
    private val mockJsonAdapter = mock<JsonAdapter>()
    private val snapshotCreator = DefaultSnapshotCreator(
        LibraryMetadata(
        "test", "1.0", "1.0", "1.0", "1.0"), 1, mockJsonAdapter)
    @Test
    fun testCreateSnapshotWithSuccessfulSnapshotCreation() {
        val metrics = listOf(TestDataGenerator.getTestMetric(10))
        val errorEvents = listOf("Error event 1")

        whenever(mockJsonAdapter.writeToJson(any(), any())).thenReturn(TestDataGenerator
            .mockSnapshot().snapshot)
        whenever(mockJsonAdapter.readJson(anyString(), any<RudderTypeAdapter<Map<String, Any>>>()))
            .thenReturn(
            mapOf())
        val mapCaptor = argumentCaptor<Map<String, Any?>>()
        val snapshot = snapshotCreator.createSnapshot(metrics, errorEvents)
        verify(mockJsonAdapter).writeToJson(mapCaptor.capture(), any<RudderTypeAdapter<Map<String, Any?>>>())

        assertNotNull(snapshot)
        val mapCaptured = mapCaptor.firstValue.toMap()
        println("id captured: ${mapCaptured["message_id"].toString()}")
        MatcherAssert.assertThat(mapCaptured, allOf(Matchers.aMapWithSize(5),
            hasKey("message_id"), hasKey("metrics"), hasKey("source"), hasKey("errors"),
        ))
        MatcherAssert.assertThat(mapCaptured["message_id"], NotEmptyStringMatcher())
    }
}