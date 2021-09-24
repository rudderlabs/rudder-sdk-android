/*
 * Creator: Debanjan Chatterjee on 24/09/21, 11:09 PM Last modified: 22/09/21, 7:23 PM
 * Copyright: All rights reserved Ⓒ 2021 http://hiteshsahu.com
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

package com.rudderstack.android.web

import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import java.util.concurrent.Future

interface WebService {

    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String, responseClass: Class<T>,
    ): Future<HttpResponse<T>>

    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String, responseTypeAdapter: RudderTypeAdapter<T>
    ): Future<HttpResponse<T>>

    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String, responseTypeAdapter: RudderTypeAdapter<T>,
        callback: (HttpResponse<T>) -> Unit
    )
    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String, responseClass: Class<T>,
        callback: (HttpResponse<T>) -> Unit
    )
    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String, responseClass: Class<T>
    ): Future<HttpResponse<T>>

    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String, responseTypeAdapter: RudderTypeAdapter<T>
    ): Future<HttpResponse<T>>



    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String, responseClass: Class<T>,
        callback: (HttpResponse<T>) -> Unit
    )

    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String, responseTypeAdapter: RudderTypeAdapter<T>,
        callback: (HttpResponse<T>) -> Unit
    )

    fun setInterceptor(httpInterceptor: HttpInterceptor)
}