/*
 * Creator: Debanjan Chatterjee on 12/01/22, 11:51 AM Last modified: 12/01/22, 11:49 AM
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

import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.dataPlaneUrl
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.core.writeKey
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.Message
import com.rudderstack.models.TrackMessage
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import junit.framework.TestSuite
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.ArgumentCaptorHolder4
import org.mockito.kotlin.ArgumentCaptorHolder5
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.then
import org.mockito.kotlin.verify
import java.util.Base64
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

abstract class DataServiceUploadImplTest {
    protected abstract val jsonAdapter: JsonAdapter
    private lateinit var dataServiceImpl: DataUploadService

    //    private val dummyWebService = DummyWebService()
    private val testMessagesList = listOf<Message>(
        TrackMessage.create("m-1", anonymousId = "anon-1", timestamp = "09-01-2022"),
        TrackMessage.create("m-2", anonymousId = "anon-2", timestamp = "09-01-2022"),
        TrackMessage.create("m-3", anonymousId = "anon-3", timestamp = "09-01-2022"),
        TrackMessage.create("m-4", anonymousId = "anon-4", timestamp = "09-01-2022"),
        TrackMessage.create("m-5", anonymousId = "anon-5", timestamp = "09-01-2022"),
        TrackMessage.create("m-6", anonymousId = "anon-6", timestamp = "09-01-2022"),
        TrackMessage.create("m-7", anonymousId = "anon-7", timestamp = "09-01-2022"),
        TrackMessage.create("m-8", anonymousId = "anon-8", timestamp = "09-01-2022"),
        TrackMessage.create("m-9", anonymousId = "anon-9", timestamp = "09-01-2022"),
        TrackMessage.create("m-10", anonymousId = "anon-10", timestamp = "09-01-2022"),
        TrackMessage.create("m-11", anonymousId = "anon-11", timestamp = "09-01-2022"),
    )

    @Before
    fun setup() {

    }


    @Test
    fun `test proper data sent to web service`() {
        val writeKey = "write_key"
        val dummyWebService = mock<WebService> {
            on {
                post(
                    anyMap(),
                    anyOrNull(),
                    anyString(),
                    anyString(),
                    any<Class<String>>(),
                    anyBoolean(),
                    any()
                )
            }.doAnswer {
                    val callback = it.arguments[6] as (HttpResponse<String>) -> Unit
                    callback(HttpResponse(200, "OK", null))
                }

        }
        dataServiceImpl = DataUploadServiceImpl(
            writeKey, dummyWebService
        )
        val configuration =
            Configuration(jsonAdapter, dataPlaneUrl = dataPlaneUrl, base64Generator = {
                Base64.getEncoder().encodeToString(
                    String.format(Locale.US, "%s:", it).toByteArray(charset("UTF-8"))
                )
            })
        dataServiceImpl.updateConfiguration(configuration)
//        val isComplete = AtomicBoolean(false)
        val argCaptors = argumentCaptor<Map<String,String>, String, String, Class<String>, Boolean>(
//            Map::class, //headers
//            String::class,//body
//            String::class,//endpoint
//            Class::class,// response class
//            Boolean::class,//gzip
        )
        dataServiceImpl.upload(testMessagesList) {
            assertThat(it.status, allOf(greaterThanOrEqualTo(200), lessThan(209)))
        }
        verify(dummyWebService).post(
            argCaptors.component1().capture(),
            anyOrNull(),
            argCaptors.component2().capture(),
            argCaptors.component3().capture(),
            argCaptors.component4().capture(),
            argCaptors.component5().capture(),
            anyOrNull()
        )
        val encodedWriteKey = configuration.base64Generator?.generateBase64(writeKey)
        assertThat(argCaptors.component1().lastValue, allOf(hasEntry("Content-Type", "application/json"),
            hasEntry("Authorization", String.format(Locale.US, "Basic %s", encodedWriteKey))))
        assertThat(argCaptors.component2().lastValue, not(emptyString()))
        assertThat(argCaptors.component3().lastValue, equalTo("v1/batch"))
        assertThat(argCaptors.component4().lastValue, equalTo(String::class.java))
        assertThat(argCaptors.component5().lastValue, equalTo(configuration.gzipEnabled))
    }

    @Test
    fun testUploadFailure() {
        val dummyWebService = mock<WebService>{
            on {
                post(
                    anyMap(),
                    anyOrNull(),
                    anyString(),
                    anyString(),
                    any<Class<String>>(),
                    anyBoolean(),
                    any()
                )
            }.doAnswer {
                val callback = it.arguments[6] as (HttpResponse<String>) -> Unit
                callback(HttpResponse(400, "Bad Request", null))
            }
        }
        dataServiceImpl = DataUploadServiceImpl("write_key", dummyWebService)
        dataServiceImpl.upload(testMessagesList) {
            assertThat(it.status, allOf(greaterThanOrEqualTo(400)))
        }
    }
//
//    @Test
//    fun testUploadSync() {
//        dummyWebService.nextStatusCode = 200
//        val response = dataServiceImpl.uploadSync(testMessagesList)
//        assertThat(response?.status ?: 0, allOf(greaterThanOrEqualTo(200), lessThan(209)))
//
//    }
}

class DataServiceUploadTestWithJackson : DataServiceUploadImplTest() {
    override val jsonAdapter: JsonAdapter = JacksonAdapter()

}

class DataServiceUploadTestWithGson : DataServiceUploadImplTest() {
    override val jsonAdapter: JsonAdapter = GsonAdapter()

}

class DataServiceUploadTestWithMoshi : DataServiceUploadImplTest() {
    override val jsonAdapter: JsonAdapter = MoshiAdapter()

}

@RunWith(Suite::class)
@Suite.SuiteClasses(
    DataServiceUploadTestWithJackson::class,
    DataServiceUploadTestWithGson::class,
    DataServiceUploadTestWithMoshi::class
)
class DataUploadTestSuite : TestSuite() {

}