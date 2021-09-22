package com.rudderstack.android.web

import android.util.MalformedJsonException
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.*

class WebServiceImpl internal constructor(
    private val baseUrl: String,
    private val jsonAdapter: JsonAdapter,
    private val defaultTimeout: Int = 10000,
    private val executor: ExecutorService = Executors.newCachedThreadPool()
) : WebService {

    private var _interceptor: HttpInterceptor? = null

    private enum class HttpMethod {
        POST,
        GET
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

    @Throws(Throwable::class)
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
                ?: throw MalformedJsonException("Json adapter not able to parse response body")
        })
    }

    @Throws(Throwable::class)
    private fun <T> httpCall(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        type: HttpMethod,
        typeAdapter: RudderTypeAdapter<T>
    ): HttpResponse<T> {
        return rawHttpCall(headers, query, body, endpoint, type, deserializer = { json ->
            jsonAdapter.readJson( json,typeAdapter)
                ?: throw MalformedJsonException("Json adapter not able to parse response body")
        })
    }

    @Throws(Throwable::class)
    private fun <T> rawHttpCall(
        headers: Map<String, String>?,
        query: Map<String, String>?,
        body: String?,
        endpoint: String,
        type: HttpMethod,
        deserializer: (String) -> T?
    ): HttpResponse<T> {
//        try {

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
        /*} catch (ex : Exception) {
//            RudderLogger.logError(ex)
            ex.printStackTrace()

        }*/

    }

    private fun createHttpConnection(
        endpoint: String,
        headers: Map<String, String>?, type: HttpMethod,
        query: Map<String, String>?,
        body: String?, defaultTimeout: Int,
        onHttpConnectionCreated: (HttpURLConnection) -> HttpURLConnection
    ): HttpURLConnection {
        //the url to hit
        val urlStr = "$baseUrl$endpoint" + query?.createQueryString()?.let {    //add query params
            if (it.isNotEmpty()) "?$it" else ""
        } ?: ""
        val url = URL(urlStr)
        // get connection object
        var httpConnection = url.openConnection() as HttpURLConnection
        // set connection object to return output
        httpConnection.doOutput = true
        httpConnection.connectTimeout = defaultTimeout
        //set headers
        headers?.iterator()?.forEach { headerEntry ->
            httpConnection.setRequestProperty(headerEntry.key, headerEntry.value)
        }

        //  set content type for network request if not present
        if (headers?.containsKey("Content-Type") == false)
            httpConnection.setRequestProperty("Content-Type", "application/json")
        // set authorization header
        /*httpConnection.setRequestProperty(
            "Authorization",
            String.format(Locale.US, "Basic %s", this.authHeaderString)
        )*/
        // set anonymousId header for definitive routing
//            httpConnection.setRequestProperty("AnonymousId", this.anonymousIdHeaderString)
        // set request method
        httpConnection.requestMethod = when (type) {
            HttpMethod.POST -> "GET"
            HttpMethod.GET -> "POST"
        }
        httpConnection = onHttpConnectionCreated.invoke(httpConnection)
        // get output stream and write payload content
        val os = httpConnection.outputStream
        val osw = OutputStreamWriter(os, "UTF-8")


        body?.apply {
            osw.write(this)
        }
        osw.flush()
        osw.close()
        os.close()

        return httpConnection
    }

    private fun Map<String, String>.createQueryString() =
        takeIf { isNotEmpty() }?.map {
            "${it.key}=${it.value}"
        }?.reduce { acc, s -> "$acc&$s" } ?: ""


}