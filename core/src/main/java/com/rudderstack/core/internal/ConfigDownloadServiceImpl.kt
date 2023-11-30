/*
 * Creator: Debanjan Chatterjee on 01/02/22, 1:54 PM Last modified: 31/01/22, 7:23 PM
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

import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.State
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import com.rudderstack.web.WebServiceFactory
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference

internal class ConfigDownloadServiceImpl(
    writeKey : String
) : ConfigDownloadService {
    private val encodedWriteKey: AtomicReference<String?> = AtomicReference()
    private val webService: AtomicReference<WebService?> = AtomicReference()
    private val currentConfigurationAtomic = AtomicReference<Configuration?>()
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

    private fun Configuration.initializeWebService() {
        webService.set(WebServiceFactory.getWebService(
            controlPlaneUrl,
            jsonAdapter = jsonAdapter, executor = networkExecutor
        ))
    }

    init {
        ConfigurationsState.subscribe(configSubscriber)
    }

    private val controlPlaneWebService = AtomicReference<WebService?>()

//                String configUrl = rudderConfig.getControlPlaneUrl() +
//                "sourceConfig?p=android&v="+Constants.RUDDER_LIBRARY_VERSION+"&bv="+android.os.Build.VERSION.SDK_INT;

    private var ongoingConfigFuture : Future<HttpResponse<RudderServerConfig>>? = null
    private var lastRudderServerConfig : RudderServerConfig? = null
    private var lastErrorMsg : String? = null
    override fun download(
        platform: String,
        libraryVersion: String,
        osVersion: String,
        retryStrategy: RetryStrategy,
        callback: (success : Boolean, RudderServerConfig?,
                   lastErrorMsg : String?) -> Unit
    ) {
        currentConfiguration?.apply {
            networkExecutor.submit {
                retryStrategy.perform({
                    ongoingConfigFuture = controlPlaneWebService.get()?.get(
                        mapOf(
                            "Content-Type" to "application/json",
                            "Authorization" to String.format(Locale.US, "Basic %s", encodedWriteKey)
                        ), mapOf(
                            "p" to platform, "v" to libraryVersion, "bv" to osVersion
                        ), "sourceConfig", RudderServerConfig::class.java
                    )
                    val response = ongoingConfigFuture?.get()
                    lastRudderServerConfig = response?.body
                    lastErrorMsg = response?.errorBody ?: response?.error?.message
                    return@perform (ongoingConfigFuture?.get()?.status ?: -1) == 200
                }) {
                    callback.invoke(it, lastRudderServerConfig, lastErrorMsg)
                }
            }
        }

    }

    override fun shutDown() {
        ConfigurationsState.removeObserver(configSubscriber)
        webService.get()?.shutdown()
        webService.set(null)
        currentConfigurationAtomic.set(null)
        encodedWriteKey.set(null)
        try {

            ongoingConfigFuture?.cancel(true)

        }catch (ex: Exception){
            // Ignore the exception
        }
    }
}