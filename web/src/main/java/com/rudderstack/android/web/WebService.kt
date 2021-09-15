package com.rudderstack.android.web

import java.util.concurrent.Future

interface WebService {

    fun <T> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String
    ): Future<HttpResponse<T>>

    fun <T> post(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: Any?,
        endpoint: String
    ): Future<HttpResponse<T>>

    fun <T> getAsync(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String, callback: (HttpResponse<T>) -> Unit
    )

    fun <T> postAsync(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: Any?,
        endpoint: String,
        callback: (HttpResponse<T>) -> Unit
    )

    fun setInterceptor(httpInterceptor: HttpInterceptor)
}