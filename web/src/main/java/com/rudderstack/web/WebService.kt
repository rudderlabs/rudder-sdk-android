/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.web

import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.util.concurrent.Future

/**
 * Basic interface with web services like get and post
 * Default implementation can be obtained through WebServiceFactory
 */
interface WebService {
    /**
     * Performs http get and provides a future.
     * The future.get() must not be called from the main thread as it will block the calling thread.
     * However this is pretty useful for test cases and if synchronicity is required
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param endpoint The endpoint to call for the http method
     * @param responseClass The response class to be provided for deserialization
     * @return A Future object for the http method
     */
    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseClass: Class<T>,
    ): Future<HttpResponse<T>>

    /**
     * Performs http get and provides a future.
     * The future.get() must not be called from the main thread as it will block the calling thread.
     * However this is pretty useful for test cases and if synchronicity is required
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param endpoint The endpoint to call for the http method
     * @param responseTypeAdapter The response type is inferred from this TypeAdapter object
     * @return A Future object for the http method
     */
    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
    ): Future<HttpResponse<T>>

    /**
     * Performs Http Get asynchronously and provides the result in a callback
     *
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param endpoint The endpoint to call for the http method
     * @param responseTypeAdapter The response type is inferred from this TypeAdapter object
     * @param callback A callback for the http response
     */
    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
        callback: (HttpResponse<T>) -> Unit,
    )

    /**
     * Performs Http Get asynchronously and provides the result in a callback
     *
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param endpoint The endpoint to call for the http method
     * @param responseClass The response class to be provided for deserialization
     * @param callback A callback for the http response
     */
    fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseClass: Class<T>,
        callback: (HttpResponse<T>) -> Unit,
    )

    /**
     * Performs http post and provides a future.
     * The future.get() must not be called from the main thread as it will block the calling thread.
     * However this is pretty useful for test cases and if synchronicity is required
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param body The body to be posted. Only json are allowed as of now.
     * @param endpoint The endpoint to call for the http method
     * @param responseClass The response class to be provided for deserialization
     * @return A Future object for the http method
     */
    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseClass: Class<T>,
        isGzipEnabled: Boolean = false,
    ): Future<HttpResponse<T>>

    /**
     * Performs http post and provides a future.
     * The future.get() must not be called from the main thread as it will block the calling thread.
     * However this is pretty useful for test cases and if synchronicity is required
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param body The body to be posted. Only json are allowed as of now.
     * @param endpoint The endpoint to call for the http method
     * @param responseTypeAdapter The response type is inferred from this TypeAdapter object
     * @return A Future object for the http method
     */
    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
        isGzipEnabled: Boolean = false,
    ): Future<HttpResponse<T>>

    /**
     * Performs http post asynchronously and provides the result in a callback
     *
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param body The body to be posted. Only json are allowed as of now.
     * @param endpoint The endpoint to call for the http method
     * @param responseClass The response class to be provided for deserialization
     * @param callback A callback for the http response
     */
    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseClass: Class<T>,
        isGzipEnabled: Boolean = false,
        callback: (HttpResponse<T>) -> Unit,
    )

    /**
     * Performs http post and provides a future.
     *
     * @param T The type of Body in HttpResponse
     * @param headers A Map<String,String> for being attached as headers
     * @param query the query map.
     * @param body The body to be posted. Only json are allowed as of now.
     * @param endpoint The endpoint to call for the http method
     * @param responseTypeAdapter The response type is inferred from this TypeAdapter object
     * @param callback A callback for the http response
     */
    fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
        isGzipEnabled: Boolean = false,
        callback: (HttpResponse<T>) -> Unit,
    )

    /**
     * Sets an interceptor that aids in adding or saving headers or other repetitive things
     *
     * @param httpInterceptor An implementation of HttpInterceptor
     * @see HttpInterceptor
     */
    fun setInterceptor(httpInterceptor: HttpInterceptor)

    /**
     * Performs Cleanup. Also shuts down the executor service. Do not call this method if the
     * same executor service is being used by other objects. In that case handle the executor
     * shutdown yourself.
     */
    fun shutdown(shutdownExecutor: Boolean = true)
}
