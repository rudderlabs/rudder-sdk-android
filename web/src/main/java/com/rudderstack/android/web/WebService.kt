package com.rudderstack.android.web

import java.util.concurrent.Future

interface WebService {

    fun <T> get(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        endpoint: String
    ): Future<HttpResponse<T>>

    fun <T> post(headers: Map<String, String>?,query: Map<String, String>?, body: Any?, endpoint: String): Future<HttpResponse<T>>

    fun setInterceptor(httpInterceptor: HttpInterceptor)
}