/*
 * Creator: Debanjan Chatterjee on 11/01/22, 12:10 PM Last modified: 11/01/22, 12:10 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

package com.rudderstack.core.internal

import com.rudderstack.core.*
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.models.Message
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpInterceptor
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import com.rudderstack.web.WebServiceFactory
import java.net.HttpURLConnection
import java.net.http.HttpRequest
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class DataUploadServiceImpl @JvmOverloads constructor(
    private val writeKey: String,
    private val jsonAdapter: JsonAdapter,
    webService: WebService? = null
) : DataUploadService {
    private val encodedWriteKey: AtomicReference<String?> = AtomicReference()
    private val webService: AtomicReference<WebService?> = AtomicReference(webService)
    private val currentConfigurationAtomic = AtomicReference<Configuration?>()
    private var headers = mutableMapOf<String, String>()

    private val _isPaused = AtomicBoolean(false)
    private val currentConfiguration
        get() = currentConfigurationAtomic.get()

    private val interceptor = HttpInterceptor {
        if (headers.isNotEmpty()) {
            synchronized(this) {
                headers.forEach { (key, value) ->
                    it.setRequestProperty(key, value)
                }
            }
        }
        it
    }

    private fun Configuration.initializeWebService() {
        if (webService.get() == null) webService.set(
            WebServiceFactory.getWebService(
                dataPlaneUrl, jsonAdapter, executor = networkExecutor
            ).also {
                it.setInterceptor(interceptor)
            }
        )
    }

    override fun addHeaders(headers: Map<String, String>) {
        synchronized(this) {
            this.headers += headers
        }
    }

    //    private val configurationState: State<Configuration> =
    override fun upload(
        data: List<Message>,
        extraInfo: Map<String, String>?,
        callback: (response: HttpResponse<out Any>) -> Unit
    ) {
        if (_isPaused.get()) {
            callback(HttpResponse(-1, null, "Upload Service is Paused", null))
            return
        }

        currentConfiguration?.apply {
            if (networkExecutor.isShutdown || networkExecutor.isTerminated) return
            val batchBody = createBatchBody(data, extraInfo)
            webService.get()?.post(
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to String.format(Locale.US, "Basic %s", encodedWriteKey),
                ), null, batchBody, "v1/batch", String::class.java, gzipEnabled
            ) {
                callback.invoke(it)
            }
        }
    }



    override fun uploadSync(
        data: List<Message>, extraInfo: Map<String, String>?
    ): HttpResponse<out Any>? {
        if (_isPaused.get()) return null
        return currentConfiguration?.let { config ->
            val batchBody = config.createBatchBody(data, extraInfo)
            webService.get()?.post(
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to String.format(Locale.US, "Basic %s", encodedWriteKey)
                ), null, batchBody, "v1/batch",
                String::class.java, isGzipEnabled = config.gzipEnabled
            )?.get()
        }
    }
    private fun Configuration.createBatchBody(
        data: List<Message>,
        extraInfo: Map<String, String>?
    ) : String?{
        val sentAt = RudderUtils.timeStamp
        return mapOf<String, Any>(
            "sentAt" to sentAt, "batch" to data.map {
                it.sentAt = sentAt
                it
            }
        ).let {
            if (extraInfo.isNullOrEmpty()) it else it + extraInfo //adding extra info data
        }.let {
            jsonAdapter.writeToJson(it, object : RudderTypeAdapter<Map<String, Any>>() {})
        }
    }
    override fun setup(analytics: Analytics) {
        //no-op
    }

    override fun updateConfiguration(configuration: Configuration) {
        encodedWriteKey.set(configuration.base64Generator.generateBase64(writeKey))
        configuration.initializeWebService()
        currentConfigurationAtomic.set(configuration)
    }

    override fun pause() {
        _isPaused.set(true)
    }

    override fun resume() {
        _isPaused.set(false)

    }

    override fun shutdown() {
        webService.get()?.shutdown()
        webService.set(null)
        currentConfigurationAtomic.set(null)
        encodedWriteKey.set(null)
    }
}