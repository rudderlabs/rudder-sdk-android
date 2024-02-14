/*
 * Creator: Debanjan Chatterjee on 08/02/24, 12:04 pm Last modified: 08/02/24, 12:04 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.core.flushpolicy

import com.rudderstack.core.Analytics
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FlushPolicyTest {
    private lateinit var analytics : Analytics
    private lateinit var flushPolicy : FlushPolicy
    @Before
    fun setup() {
        analytics = generateTestAnalytics(mock<JsonAdapter>())
        flushPolicy = mock()
    }
    @After
    fun destroy() {
        analytics.shutdown()
    }
    @Test
    fun testRescheduleCalledOnFlush() {
        analytics.addFlushPolicies(flushPolicy)
        analytics.blockingFlush()
        verify(flushPolicy).reschedule()
        //test do not crash
    }
}