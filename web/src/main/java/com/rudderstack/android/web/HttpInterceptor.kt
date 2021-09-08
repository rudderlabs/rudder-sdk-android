package com.rudderstack.android.web

import java.net.HttpURLConnection

interface HttpInterceptor {
    /**
     * TODO
     *
     * @param connection could be cast to HttpsURLConnection, if ssl is used
     */
    fun intercept(connection:HttpURLConnection): HttpURLConnection
}