/*
 * Creator: Debanjan Chatterjee on 22/06/23, 6:16 pm Last modified: 22/06/23, 6:16 pm
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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ConfigModule
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ContextModule
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestDataGenerator
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpInterceptor
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.util.Date
import java.util.concurrent.Future

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
open class DefaultUploaderTest {

    @Test
    fun `test uploadWithSuccessfulUpload`() {
        val mockSnapshot = TestDataGenerator.mockSnapshot()
        val callbackMock = mock<(Boolean) -> Unit>()
        val mockWebService = MockPostWebService(200)
        val uploadMediator = DefaultUploadMediator("https://api.example.com", mock(), mock(), true, mockWebService)


        uploadMediator.upload(mockSnapshot, callbackMock)

        verify(callbackMock, times(1)).invoke(true)
    }
    @Test
    fun `test uploadWithFailedUpload`() {
        val mockSnapshot = TestDataGenerator.mockSnapshot()
        val callbackMock = mock<(Boolean) -> Unit>()
        val mockWebService = MockPostWebService(400)
        val uploadMediator = DefaultUploadMediator("https://api.example.com", mock(), mock(), true, mockWebService)


        uploadMediator.upload(mockSnapshot, callbackMock)

        verify(callbackMock, times(1)).invoke(false)
    }



    companion object {
        private const val MANUFACTURER = "Google"
        private const val MODEL = "pixel 7"
        private const val OS_VERSION = "10"
        private const val API_LEVEL = 28
        private const val OS_BUILD = "Android"
        private const val ID = "id"

    }

    class MockPostWebService(private val statusCode: Int) : WebService {
        override fun <T : Any> get(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            endpoint: String,
            responseClass: Class<T>
        ): Future<HttpResponse<T>> {
            TODO("Not yet implemented")
        }

        override fun <T : Any> get(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            endpoint: String,
            responseTypeAdapter: RudderTypeAdapter<T>
        ): Future<HttpResponse<T>> {
            TODO("Not yet implemented")
        }

        override fun <T : Any> get(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            endpoint: String,
            responseTypeAdapter: RudderTypeAdapter<T>,
            callback: (HttpResponse<T>) -> Unit
        ) {
            TODO("Not yet implemented")
        }

        override fun <T : Any> get(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            endpoint: String,
            responseClass: Class<T>,
            callback: (HttpResponse<T>) -> Unit
        ) {
            TODO("Not yet implemented")
        }

        override fun <T : Any> post(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            body: String?,
            endpoint: String,
            responseClass: Class<T>,
            isGzipEnabled: Boolean
        ): Future<HttpResponse<T>> {
            TODO("Not yet implemented")
        }

        override fun <T : Any> post(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            body: String?,
            endpoint: String,
            responseTypeAdapter: RudderTypeAdapter<T>,
            isGzipEnabled: Boolean
        ): Future<HttpResponse<T>> {
            TODO("Not yet implemented")
        }

        override fun <T : Any> post(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            body: String?,
            endpoint: String,
            responseClass: Class<T>,
            isGzipEnabled: Boolean,
            callback: (HttpResponse<T>) -> Unit
        ) {
            callback.invoke(HttpResponse(statusCode, null, null))
        }

        override fun <T : Any> post(
            headers: Map<String, String>?,
            query: Map<String, String>?,
            body: String?,
            endpoint: String,
            responseTypeAdapter: RudderTypeAdapter<T>,
            isGzipEnabled: Boolean,
            callback: (HttpResponse<T>) -> Unit
        ) {
            callback.invoke(HttpResponse(statusCode, null, null))
        }

        override fun setInterceptor(httpInterceptor: HttpInterceptor) {
            TODO("Not yet implemented")
        }

        override fun shutdown(shutdownExecutor: Boolean) {
            TODO("Not yet implemented")
        }
    }
}

