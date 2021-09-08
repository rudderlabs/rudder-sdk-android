package com.rudderstack.android.web

class HttpResponse<T>(val status: Int, val body : T?, val errorBody: String?)
/*

    private var _status: Int = 0
    val httpStatusCode
        get() = _status

    private var _errorBody: String? = null
    val errorBody
        get() = _errorBody

    private var _body: T? = null
    val body
        get() = _body
*/
