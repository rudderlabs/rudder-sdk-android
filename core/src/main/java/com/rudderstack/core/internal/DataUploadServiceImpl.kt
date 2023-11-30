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
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpInterceptor
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import com.rudderstack.web.WebServiceFactory
import java.net.HttpURLConnection
import java.net.http.HttpRequest
import java.util.*
import java.util.concurrent.atomic.AtomicReference

internal class DataUploadServiceImpl(
    writeKey: String
) : DataUploadService {
    private val encodedWriteKey: AtomicReference<String?> = AtomicReference()
    private val webService: AtomicReference<WebService?> = AtomicReference()
    private val currentConfigurationAtomic = AtomicReference<Configuration?>()
    private var headers = mutableMapOf<String, String>()

    private val currentConfiguration
        get() = currentConfigurationAtomic.get()
    private val configSubscriber: State.Observer<Configuration> =
        State.Observer<Configuration> { state ->
            state?.apply {
                encodedWriteKey.set(base64Generator.generateBase64(writeKey))
                initializeWebService()
                currentConfigurationAtomic.set(this)
            }
        }
    private val interceptor = HttpInterceptor {
        if(headers.isNotEmpty()) {
            synchronized(this) {
                headers.forEach { (key, value) ->
                    it.setRequestProperty(key, value)
                }
            }
        }
        it
    }
    init {
        ConfigurationsState.subscribe(configSubscriber)
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
        currentConfiguration?.apply {
            if (networkExecutor.isShutdown || networkExecutor.isTerminated) return
            val batchBody = mapOf<String, Any>(
                "sentAt" to RudderUtils.timeStamp, "batch" to data
            ).let {
                if (extraInfo.isNullOrEmpty()) it else it + extraInfo //adding extra info data
            }.let {
                jsonAdapter.writeToJson(it, object : RudderTypeAdapter<Map<String, Any>>() {})
            }
            webService.get()?.post(mapOf("Content-Type" to "application/json",
                "Authorization" to String.format(Locale.US, "Basic %s", encodedWriteKey),
                /**/), null, batchBody, "v1/batch", String::class.java) {
                callback.invoke(it)
            }
        }
    }

    override fun uploadSync(
        data: List<Message>, extraInfo: Map<String, String>?
    ): HttpResponse<out Any>? {
        return currentConfiguration?.let { config ->
            val batchBody = mapOf<String, Any>(
                "sentAt" to RudderUtils.timeStamp, "batch" to data
            ).let {
                if (extraInfo.isNullOrEmpty()) it else it + extraInfo //adding extra info data
            }.let {
                config.jsonAdapter.writeToJson(it, object : RudderTypeAdapter<Map<String, Any>>()
                {})
            }
            webService.get()?.post(mapOf("Content-Type" to "application/json",
                "Authorization" to String.format(Locale.US, "Basic %s", encodedWriteKey)/*,
                "anonymousId" to (ConfigurationsState.value?.anonymousId?.let {
                    base64Generator.generateBase64(
                        it
                    )
                } ?: encodedWriteKey)*/), null, batchBody, "v1/batch", String::class.java)?.get()
        }
    }


    override fun shutdown() {
        ConfigurationsState.removeObserver(configSubscriber)
        webService.get()?.shutdown()
        webService.set(null)
        currentConfigurationAtomic.set(null)
        encodedWriteKey.set(null)
    }


}