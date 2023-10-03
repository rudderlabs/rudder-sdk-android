/*
 * Creator: Debanjan Chatterjee on 21/09/23, 6:17 pm Last modified: 21/09/23, 6:17 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.error

import org.junit.Test

class CrashFilterTest {
    @Test
    fun `should return true if the exception message contains the keyword`() {
        val crashFilter = CrashFilter.generateWithKeyWords(listOf("test"))
        val exception = Exception("test")
        assert(crashFilter.shouldKeep(exception))
    }

    @Test
    fun `should return true if the causing message contains the keyword`() {
        val crashFilter = CrashFilter.generateWithKeyWords(listOf("test"))
        val exception = Exception("test_wrapper", Exception("test"))
        assert(crashFilter.shouldKeep(exception))
    }

    @Test
    fun `should return true if the stacktrace contains the keyword dracula`() {
        val crashFilter = CrashFilter.generateWithKeyWords(listOf("dracula"))
        val exception = Exception("some_exception", Exception("test"))
        assert(crashFilter.shouldKeep(exception)) // this method name has dracula in it
    }

    @Test
    fun `should return false for empty keywords list `() {
        val crashFilter = CrashFilter.generateWithKeyWords(emptyList())
        val exception = Exception("some_exception", Exception("test"))
        assert(!crashFilter.shouldKeep(exception))
    }

    @Test
    fun `should return false if the exception message or stacktrace doesn't contain the keyword`() {
        val crashFilter = CrashFilter.generateWithKeyWords(listOf("dracula"))
        val exception = Exception("some_exception", Exception("some_other_exception"))
        assert(!crashFilter.shouldKeep(exception))
    }

}