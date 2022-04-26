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

package com.rudderstack.web.internal

import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpInterceptor
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.*

class WebServiceImpl internal constructor(
    baseUrl : String,
    private val jsonAdapter: JsonAdapter,
    private val defaultTimeout: Int = 10000,
    private val executor: ExecutorService = Executors.newCachedThreadPool()
) : WebService {

    private var _interceptor: HttpInterceptor? = null
    private val baseUrl: String

    private enum class HttpMethod {
        POST,
        GET
    }
    init {
        this.baseUrl = baseUrl.validatedBaseUrl
    }

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseClass: Class<T>
    ): Future<HttpResponse<T>> {
        return executor.submit(Callable {
            httpCall(headers, query, null, endpoint, HttpMethod.GET, responseClass)
        })
    }

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>
    ): Future<HttpResponse<T>> {
        return executor.submit(Callable {
            httpCall(headers, query, null, endpoint, HttpMethod.GET, responseTypeAdapter)
        })
    }

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
        callback: (HttpResponse<T>) -> Unit
    ) {
        executor.execute {
            callback.invoke(
                httpCall(headers, query, null, endpoint, HttpMethod.GET, responseTypeAdapter)
            )
        }
    }

    override fun <T : Any> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String,
        responseClass: Class<T>,
        callback: (HttpResponse<T>) -> Unit
    ) {

        executor.execute {
            callback.invoke(
                httpCall(headers, query, null, endpoint, HttpMethod.GET, responseClass)
            )
        }
    }

    override fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseClass: Class<T>
    ): Future<HttpResponse<T>> {
        return executor.submit(Callable {
            httpCall(headers, query, body, endpoint, HttpMethod.POST, responseClass)
        })
    }

    override fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>
    ): Future<HttpResponse<T>> {
        return executor.submit(Callable {
            httpCall(headers, query, body, endpoint, HttpMethod.POST, responseTypeAdapter)
        })
    }

    override fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseClass: Class<T>,
        callback: (HttpResponse<T>) -> Unit
    ) {
        executor.execute {
            callback.invoke(
                httpCall(headers, query, body, endpoint, HttpMethod.POST, responseClass)
            )
        }
    }

    override fun <T : Any> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        responseTypeAdapter: RudderTypeAdapter<T>,
        callback: (HttpResponse<T>) -> Unit
    ) {
        executor.execute {
            callback.invoke(
                httpCall(headers, query, body, endpoint, HttpMethod.POST, responseTypeAdapter)
            )
        }
    }


    override fun setInterceptor(httpInterceptor: HttpInterceptor) {
        _interceptor = httpInterceptor
    }

//    @Throws(Throwable::class)
    private fun <T : Any> httpCall(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        type: HttpMethod,

        responseClass: Class<T>
    ): HttpResponse<T> {
        return rawHttpCall(headers, query, body, endpoint, type, deserializer = { json ->
            jsonAdapter.readJson(json, responseClass)
                ?: throw IllegalArgumentException("Json adapter not able to parse response body")
        })
    }

    @Throws(IOException::class)
    private fun <T> httpCall(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        type: HttpMethod,
        typeAdapter: RudderTypeAdapter<T>
    ): HttpResponse<T> {
        return rawHttpCall(headers, query, body, endpoint, type, deserializer = { json ->
            jsonAdapter.readJson(json, typeAdapter)
                ?: throw IllegalArgumentException("Json adapter not able to parse response body")
        })
    }


    private fun <T> rawHttpCall(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        type: HttpMethod,
        deserializer: (String) -> T?
    ): HttpResponse<T> {
        try {

        val httpConnection =
            createHttpConnection(endpoint, headers, type, query, body, defaultTimeout) {
                //call interceptor if any changes to HttpConnection required
                _interceptor?.intercept(it) ?: it
            }
        // create connection
        httpConnection.connect()

        // get input stream from connection to get output from the server
        if (httpConnection.responseCode in (200 until 300)) { //success block
            val bis = BufferedInputStream(httpConnection.inputStream)
            val baos = ByteArrayOutputStream()
            var res = bis.read()
            // read response from the server
            while (res != -1) {
                baos.write(res)
                res = bis.read()
            }
            // finally return response when reading from server is completed
            /*if (baos.toString().equals("OK", ignoreCase = true)) {
                return Utils.NetworkResponses.SUCCESS
            }*/
            return HttpResponse(
                httpConnection.responseCode,
                deserializer.invoke(baos.toString()),
                null
            )

        } else {
            val bis = BufferedInputStream(httpConnection.errorStream)
            val baos = ByteArrayOutputStream()
            var res = bis.read()
            // read response from the server
            while (res != -1) {
                baos.write(res)
                res = bis.read()
            }
            // finally return response when reading from server is completed
            val errorResp = baos.toString()
//                RudderLogger.logError("EventRepository: flushEventsToServer: ServerError: $errorResp")
            // return null as request made is not successful
//                if (errorResp.lowercase().contains("invalid write key")) {
//                    return Utils.NetworkResponses.WRITE_KEY_ERROR
//                }
            return HttpResponse(httpConnection.responseCode, null, errorResp)
        }
        } catch (ex : Exception) {
//            RudderLogger.logError(ex)
            ex.printStackTrace()
            return HttpResponse(status = HttpResponse.HTTP_STATUS_NONE,
            body = null,
            errorBody = null,
            error = ex)
        }

    }
//    @kotlin.jvm.Throws(IOException::class)
    @Throws(IOException::class)
    private fun createHttpConnection(
        endpoint: String,
        headers: Map<String, String>?, type: HttpMethod,
        query: Map<String, String>?,
        body: String?, defaultTimeout: Int,
        onHttpConnectionCreated: (HttpURLConnection) -> HttpURLConnection
    ): HttpURLConnection {
        //the url to hit
        val urlStr = "$baseUrl$endpoint" + if (!query.isNullOrEmpty()) query.createQueryString()
            .let {    //add query params
                if (it.isNotEmpty()) "?$it" else ""
            } else ""
        val url = URL(urlStr)
        // get connection object
        var httpConn = url.openConnection() as HttpURLConnection

        httpConn.connectTimeout = defaultTimeout
        //set headers
        headers?.iterator()?.forEach { headerEntry ->
            httpConn.setRequestProperty(headerEntry.key, headerEntry.value)
        }

        //  set content type for network request if not present
        if (headers?.containsKey("Content-Type") == false)
            httpConn.setRequestProperty("Content-Type", "application/json")
        // set authorization header
        /*httpConnection.setRequestProperty(
            "Authorization",
            String.format(Locale.US, "Basic %s", this.authHeaderString)
        )*/
        // set anonymousId header for definitive routing
//            httpConnection.setRequestProperty("AnonymousId", this.anonymousIdHeaderString)
        // set request method
        httpConn.requestMethod = when (type) {
            HttpMethod.GET -> "GET"
            HttpMethod.POST -> "POST"
        }
        httpConn = onHttpConnectionCreated.invoke(httpConn)

        // get output stream and write payload content
        if(type == HttpMethod.POST) {
            // set connection object to return output
            httpConn.doOutput = true
            val os = httpConn.outputStream
            val osw = OutputStreamWriter(os, "UTF-8")

            body?.apply {
                osw.write(this)
            }
            osw.flush()
            osw.close()
            os.close()
        }
        return httpConn
    }

    private fun Map<String, String>.createQueryString() =
        takeIf { isNotEmpty() }?.map {
            "${it.key}=${it.value}"
        }?.reduce { acc, s -> "$acc&$s" } ?: ""

    private val String.validatedBaseUrl
    get() = if(this.endsWith('/')) this else "$this/"
}