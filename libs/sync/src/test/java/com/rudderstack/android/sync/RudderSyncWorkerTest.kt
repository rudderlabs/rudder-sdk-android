/*
 * Creator: Debanjan Chatterjee on 18/03/24, 12:47 pm Last modified: 18/03/24, 12:13 pm
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

package com.rudderstack.android.sync

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestWorkerBuilder
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.internal.infrastructure.sync.RudderSyncWorker
import com.rudderstack.android.internal.infrastructure.sync.WorkManagerAnalyticsFactory
import com.rudderstack.android.internal.infrastructure.sync.registerWorkManager
import com.rudderstack.android.sync.utils.TestLogger
import com.rudderstack.core.Analytics
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
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
    @MockK
    lateinit var configuration: ConfigurationAndroid

    private lateinit var application: Application
    private lateinit var executorService: ExecutorService
    @Before
    fun setup(){
        MockKAnnotations.init()
//        MockitoAnnotations.openMocks(this)
        application = ApplicationProvider.getApplicationContext()
        val logger = TestLogger()
        every{configuration.defaultProcessName} returns null
        every{configuration.multiProcessEnabled} returns false
        every{configuration.networkExecutor} returns mockk()
        every{analytics.logger} returns logger
        every{analytics.currentConfigurationAndroid} returns configuration
        every{analytics.currentConfiguration} returns configuration
        application.registerWorkManager(analytics, DummyAnalyticsFactory::class.java)
        executorService = Executors.newSingleThreadExecutor()

    }

    @Test
    fun testRudderSyncWorker(){
        every{analytics.blockingFlush()} returns true
        every { analytics.isShutdown } returns false
        val worker = TestWorkerBuilder.from(application, RudderSyncWorker::class.java, executorService).build()
        val result = worker.doWork()
        verify(exactly = 1){
            analytics.blockingFlush()
        }
    }
    inner class DummyAnalyticsFactory: WorkManagerAnalyticsFactory {
        override fun createAnalytics(application: Application): Analytics {
            return analytics
        }

    }
}