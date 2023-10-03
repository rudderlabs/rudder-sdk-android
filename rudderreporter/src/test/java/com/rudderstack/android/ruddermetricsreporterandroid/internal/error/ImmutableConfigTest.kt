/*
 * Creator: Debanjan Chatterjee on 21/09/23, 6:37 pm Last modified: 21/09/23, 6:37 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal.error

import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.error.BreadcrumbType
import com.rudderstack.android.ruddermetricsreporterandroid.error.CrashFilter
import com.rudderstack.android.ruddermetricsreporterandroid.internal.NoopLogger
import org.junit.Assert.*

import org.junit.Test

class ImmutableConfigTest {

    @Test
    fun `shouldDiscardError should return false for crashFilter with keywords`() {
        val crashFilter = CrashFilter.generateWithKeyWords(listOf("test"))
        val exception = Exception("test")
        val immutableConfig = ImmutableConfig(
            LibraryMetadata(
                "test_lib",
                "1.3.0", "14", "my_write_key"
            ),
            listOf("com.rudderstack.android"),
            setOf(BreadcrumbType.ERROR),
            emptyList(),
            crashFilter,
            NoopLogger,
            10,
            10,
            null,
            "test",
            null,
            null
        )
        assertFalse(immutableConfig.shouldDiscardError(exception))
    }
    @Test
    fun `shouldDiscardError should return false for null crashFilter`() {
        val exception = Exception("test")
        val immutableConfig = ImmutableConfig(
            LibraryMetadata(
                "test_lib",
                "1.3.0", "14", "my_write_key"
            ),
            listOf("com.rudderstack.android"),
            setOf(BreadcrumbType.ERROR),
            emptyList(),
            null,
            NoopLogger,
            10,
            10,
            null,
            "test",
            null,
            null
        )
        assertFalse(immutableConfig.shouldDiscardError(exception))
    }
    @Test
    fun `shouldDiscardError should return true for empty keywords crashFilter`() {
        val crashFilter = CrashFilter.generateWithKeyWords(emptyList())
        val exception = Exception("test")
        val immutableConfig = ImmutableConfig(
            LibraryMetadata(
                "test_lib",
                "1.3.0", "14", "my_write_key"
            ),
            listOf("com.rudderstack.android"),
            setOf(BreadcrumbType.ERROR),
            emptyList(),
            crashFilter,
            NoopLogger,
            10,
            10,
            null,
            "test",
            null,
            null
        )
        assertTrue(immutableConfig.shouldDiscardError(exception))
    }
}