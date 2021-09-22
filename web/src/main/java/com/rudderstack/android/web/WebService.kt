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