/*
 * Creator: Debanjan Chatterjee on 12/12/23, 5:42 pm Last modified: 12/12/23, 5:42 pm
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

package com.rudderstack.core

import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpInterceptor
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import java.util.concurrent.Callable
import java.util.concurrent.Future

class DummyWebService : WebService {
    var nextStatusCode = 200
    var nextBody: Any? = null
    var nextErrorBody: String? = null
    var nextError: Throwable? = null
    private val dummyExecutor = DummyExecutor()

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseClass: Class<T>
    ): Future<HttpResponse<T>> {
        val future: Future<HttpResponse<T>> = dummyExecutor.submit(Callable {
            HttpResponse(
                nextStatusCode, nextBody as? T?, nextErrorBody, nextError
            )
        })
        return future
    }

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>
    ): Future<HttpResponse<T>> {
        val future: Future<HttpResponse<T>> = dummyExecutor.submit(Callable {
            HttpResponse(
                nextStatusCode, nextBody as? T?, nextErrorBody, nextError
            )
        })
        return future
    }

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
        callback: (HttpResponse<T>) -> Unit
    ) {
        callback(
            HttpResponse(
                nextStatusCode, nextBody as? T?, nextErrorBody, nextError
            )
        )
    }

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseClass: Class<T>,
        callback: (HttpResponse<T>) -> Unit
    ) {
        callback(
            HttpResponse(
                nextStatusCode, nextBody as? T?, nextErrorBody, nextError
            )
        )
    }

    override fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseClass: Class<T>,
        isGzipEnabled: Boolean
    ): Future<HttpResponse<T>> {
        return dummyExecutor.submit(Callable {
            HttpResponse(
                nextStatusCode, nextBody as? T?, nextErrorBody, nextError
            )
        })
    }

    override fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
        isGzipEnabled: Boolean
    ): Future<HttpResponse<T>> {
        return dummyExecutor.submit(Callable {
            HttpResponse(
                nextStatusCode, nextBody as? T?, nextErrorBody, nextError
            )
        })
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

    }

    override fun setInterceptor(httpInterceptor: HttpInterceptor) {

    }

    override fun shutdown(shutdownExecutor: Boolean) {

    }

}