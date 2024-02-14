/*
 * Creator: Debanjan Chatterjee on 08/02/24, 11:20 am Last modified: 08/02/24, 11:20 am
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
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.busyWait
import com.rudderstack.models.Message
import com.rudderstack.web.HttpResponse
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class IntervalBasedFlushPolicyTest {
    private lateinit var analytics: Analytics
    private lateinit var flushPolicy: IntervalBasedFlushPolicy
    private lateinit var mockUploadService: DataUploadService

    @Before
    fun setup() {
        mockUploadService = mock()
        val mockedResponse: HttpResponse<out Any> = HttpResponse(200, "OK", null)
        whenever(mockUploadService.upload(any(), any(), any())).then {
//            storage.deleteMessages(data)
            it.getArgument<(response: HttpResponse<out Any>) -> Unit>(2).invoke(
                mockedResponse
            )
        }
        Mockito.`when`(mockUploadService.uploadSync(any<List<Message>>(), anyOrNull())).thenReturn(
            mockedResponse
        )
        analytics = generateTestAnalytics(
            Configuration(mock(), shouldVerifySdk = false),
            dataUploadService = mockUploadService
        )
        flushPolicy = IntervalBasedFlushPolicy()
        analytics.removeAllFlushPolicies()
        analytics.addFlushPolicies(flushPolicy)
    }

    @After
    fun tearDown() {
        flushPolicy.shutdown()
        analytics.shutdown()
    }

    @Test
    fun testSetup() {
        flushPolicy.setup(analytics)
        //test do not crash
    }

    @Test
    fun testUpdateConfiguration() {
        val config = mock<Configuration>()
        val flushCalledCount = AtomicInteger(0)
        whenever(config.maxFlushInterval).thenReturn(100)
        flushPolicy.setFlush {
            flushCalledCount.incrementAndGet()
        }
        flushPolicy.updateConfiguration(config)
        busyWait(150L)
        assertThat(flushCalledCount.get(), Matchers.equalTo(1))
    }
    @Test
    fun testReschedule() {
        val config = mock<Configuration>()
        val flushCalledCount = AtomicInteger(0)
        whenever(config.maxFlushInterval).thenReturn(300)
        flushPolicy.setFlush {
            flushCalledCount.incrementAndGet()
        }

        flushPolicy.updateConfiguration(config)// a long duration
        busyWait(250) // just before the flush
        flushPolicy.reschedule()

        busyWait(150L)
        assertThat(flushCalledCount.get(), Matchers.equalTo(0))
    }
}