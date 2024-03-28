/*
 * Creator: Debanjan Chatterjee on 04/04/22, 1:28 PM Last modified: 04/04/22, 1:28 PM
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

package com.rudderstack.core.internal

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.DummyWebService
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.writeKey
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.vagabond.testcommon.generateTestAnalytics
import junit.framework.TestSuite
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

abstract class ConfigDownloadServiceImplTest {
    protected abstract val jsonAdapter: JsonAdapter
    private lateinit var configDownloadServiceImpl: ConfigDownloadServiceImpl
    private val dummyWebService: DummyWebService = DummyWebService()
    private lateinit var analytics: Analytics

    @Before
    fun setup() {
        val config = Configuration(jsonAdapter)
        analytics = generateTestAnalytics(config)
        dummyWebService.nextBody =
            RudderServerConfig(source = RudderServerConfig.RudderServerConfigSource())
//        ConfigurationsState.update(ConfigurationsState.value ?: Configuration.invoke(jsonAdapter))
        configDownloadServiceImpl = ConfigDownloadServiceImpl(
            Base64.getEncoder().encodeToString(
                String.format(Locale.US, "%s:", writeKey).toByteArray(charset("UTF-8"))
            ), dummyWebService
        )
        configDownloadServiceImpl.setup(analytics)
        configDownloadServiceImpl.updateConfiguration(config)
    }

    @Test
    fun `test successful config download`() {
        val isComplete = AtomicBoolean(false)
        configDownloadServiceImpl.download(callback = { success, rudderServerConfig, lastErrorMsg ->
            assertThat(success, `is`(true))
            assertThat(lastErrorMsg, nullValue())
            assertThat(rudderServerConfig, Matchers.notNullValue())
            assertThat(rudderServerConfig?.source, Matchers.notNullValue())
            isComplete.set(true)
        })

        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilTrue(isComplete)
    }

    @Test
    fun `test failure config download`() {
        val isComplete = AtomicBoolean(false)
        dummyWebService.nextStatusCode = 400
        dummyWebService.nextBody = null
        dummyWebService.nextErrorBody = "Bad Request"
        analytics.currentConfiguration?.copy(
            sdkVerifyRetryStrategy = RetryStrategy.exponential(1)
        )?.let {
            configDownloadServiceImpl.updateConfiguration(
                it
            )
        }
        configDownloadServiceImpl.download(callback = { success, rudderServerConfig, lastErrorMsg ->
            assertThat(success, `is`(false))
            assertThat(lastErrorMsg, notNullValue())
            assertThat(rudderServerConfig, nullValue())
            isComplete.set(true)
        })

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilTrue(isComplete)
    }

    @After
    fun destroy() {
        configDownloadServiceImpl.shutdown()
        analytics.shutdown()
    }

}

class ConfigDownloadTestWithJackson : ConfigDownloadServiceImplTest() {
    override val jsonAdapter: JsonAdapter = JacksonAdapter()

}

class ConfigDownloadTestWithGson : ConfigDownloadServiceImplTest() {
    override val jsonAdapter: JsonAdapter = GsonAdapter()

}

class ConfigDownloadTestWithMoshi : ConfigDownloadServiceImplTest() {
    override val jsonAdapter: JsonAdapter = MoshiAdapter()

}

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ConfigDownloadTestWithMoshi::class,
    ConfigDownloadTestWithGson::class,
    ConfigDownloadTestWithJackson::class
)
class ConfigDownloadTestSuite : TestSuite()