/*
 * Creator: Debanjan Chatterjee on 19/06/23, 8:16 pm Last modified: 19/06/23, 8:16 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.Source
import com.rudderstack.android.ruddermetricsreporterandroid.Uploader
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.WebServiceFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class DefaultUploader(
    dataCollectionModule: DataCollectionModule,
    baseUrl: String,
    source : Source,
    networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
    private val jsonAdapter: JsonAdapter,
    private val apiVersion : Int = 1
) : Uploader {
    private val deviceDataCollector: DeviceDataCollector
    private val webService = WebServiceFactory.getWebService(baseUrl, jsonAdapter, executor = networkExecutor)
    private val source = source.serialize(jsonAdapter)
    init {
        deviceDataCollector = dataCollectionModule.deviceDataCollector
    }

    override fun upload(metrics: List<MetricModel<Number>>, error: ErrorModel,
                        callback: (success : Boolean) -> Unit) {
        val requestMap = createRequestMap(metrics, error)
        webService.post(null,null, jsonAdapter.writeToJson(requestMap), METRICS_ENDPOINT,
            object : RudderTypeAdapter<Map<*,*>>(){}){
            (it.status in 200..299).apply(callback)
        }
    }

    private fun createRequestMap(metrics: List<MetricModel<Number>>, error: ErrorModel): Map<String, Any?> {
        val requestMap = HashMap<String, Any?>()
        requestMap[DEVICE_KEY] = deviceDataCollector.generateDeviceWithState(System.currentTimeMillis()).serialize(jsonAdapter)
        requestMap[METRICS_KEY] = metrics.map { it.serialize(jsonAdapter) }
        requestMap[ERROR_KEY] = error.serialize(jsonAdapter)
        requestMap[SOURCE_KEY] = source
        requestMap[VERSION_KEY] = apiVersion
        return requestMap
    }
    companion object{
        private const val DEVICE_KEY = "device"
        private const val SOURCE_KEY = "source"
        private const val METRICS_KEY = "metrics"
        private const val ERROR_KEY = "error"
        private const val VERSION_KEY = "version"
        private const val METRICS_ENDPOINT = "sdkmetrics"
    }
}