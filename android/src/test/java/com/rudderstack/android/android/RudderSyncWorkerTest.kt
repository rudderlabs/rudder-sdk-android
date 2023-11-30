/*
 * Creator: Debanjan Chatterjee on 07/09/22, 12:19 PM Last modified: 07/09/22, 12:08 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

package com.rudderstack.android.android

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestWorkerBuilder
import com.rudderstack.android.android.utils.TestLogger
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.internal.infrastructure.sync.RudderSyncWorker
import com.rudderstack.android.internal.infrastructure.sync.registerWorkManager
import com.rudderstack.core.Analytics
import com.rudderstack.core.Base64Generator
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.annotation.Config
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class RudderSyncWorkerTest {
    @get:Rule
    val mockkRule = MockKRule(this)
    @MockK
    lateinit var analytics: Analytics

    private lateinit var application: Application
    private lateinit var executorService: ExecutorService
    @Before
    fun setup(){
//        MockitoAnnotations.openMocks(this)
        application = ApplicationProvider.getApplicationContext()
        val logger = TestLogger()
        every{analytics.logger} returns logger
        application.registerWorkManager(analytics, RudderWorkerConfig("", false, jsonAdapter = JacksonAdapter(),
        logger = AndroidLogger, controlPlaneUrl = null, dataPlaneUrl = null)
        )
        executorService = Executors.newSingleThreadExecutor()

    }

    @Test
    fun testRudderSyncWorker(){
        every{analytics.blockingFlush( true, any(), null)} returns true
        every { analytics.isShutdown } returns false
        val worker = TestWorkerBuilder.from(application, RudderSyncWorker::class.java, executorService).build()
        val result = worker.doWork()
        verify(exactly = 1){
            analytics.blockingFlush(true,  org.mockito.kotlin.any<Base64Generator>(), any())
        }
    }
}