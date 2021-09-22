package com.rudderstack.android.web

data class HttpResponse<T>(val status: Int, val body : T?, val errorBody: String?)
