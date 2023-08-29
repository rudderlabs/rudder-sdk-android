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

import android.util.Log
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.UploadMediator
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorModel
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ConfigModule
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.WebServiceFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class DefaultUploadMediator(
//    dataCollectionModule: DataCollectionModule,
    private val configModule: ConfigModule,
    baseUrl: String,
    private val jsonAdapter: JsonAdapter,
    networkExecutor: ExecutorService,
    private val apiVersion : Int = 1
) : UploadMediator {
//    private val deviceDataCollector: DeviceDataCollector
    private val webService = WebServiceFactory.getWebService(baseUrl, jsonAdapter,
        executor = networkExecutor)
//    private val libraryMetadataJson = configModule.config.libraryMetadata.serialize(jsonAdapter)
//    init {
//        deviceDataCollector = dataCollectionModule.deviceDataCollector
//    }

    override fun upload(metrics: List<MetricModel<out Number>>, error: ErrorModel,
                        callback: (success : Boolean) -> Unit) {
        val requestMap = createRequestMap(metrics, error)
        webService.post(null,null, jsonAdapter.writeToJson(requestMap,
            object: RudderTypeAdapter<Map<String, Any?>>() {}).also {
            println(it)
        }, METRICS_ENDPOINT,
            object : RudderTypeAdapter<Map<*,*>>(){}){

            (it.status in 200..299).apply(callback)
        }
    }

    private fun createRequestMap(metrics: List<MetricModel<out Number>>, error: ErrorModel): Map<String, Any?> {
        val requestMap = HashMap<String, Any?>()
//        requestMap[DEVICE_KEY] =
        requestMap[METRICS_KEY] = metrics
        requestMap[ERROR_KEY] = error
        requestMap[SOURCE_KEY] = configModule.config.libraryMetadata
                /*mapOf(
            DEVICE_KEY to deviceDataCollector.generateDeviceWithState(System.currentTimeMillis()),
            LIBRARY_METADATA_KEY to configModule.config.libraryMetadata
        )*/
        requestMap[VERSION_KEY] = apiVersion.toString()
        return requestMap
    }
//    private fun getSourceJsonFromDeviceAndLibrary(deviceJson: String?,
//                                                  libraryMetadataJson: String?): String? {
//        return jsonAdapter.writeToJson(
//            mapOf(
//                DEVICE_KEY to deviceJson,
//                LIBRARY_METADATA_KEY to libraryMetadataJson
//            ), object : RudderTypeAdapter<Map<*,*>>(){}
//        )
//    }
    companion object{
//        private const val DEVICE_KEY = "device"
//        private const val LIBRARY_METADATA_KEY = "libraryMetadata"
        private const val SOURCE_KEY = "source"
        private const val METRICS_KEY = "metrics"
        private const val ERROR_KEY = "error"
        private const val VERSION_KEY = "version"
        private const val METRICS_ENDPOINT = "sdkmetrics"
    }
}