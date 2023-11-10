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

import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot
import com.rudderstack.android.ruddermetricsreporterandroid.UploadMediator
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ConfigModule
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.WebServiceFactory
import java.util.concurrent.ExecutorService

internal class DefaultUploadMediator(
    baseUrl: String,
    jsonAdapter: JsonAdapter,
    networkExecutor: ExecutorService,
    private val isGzipEnabled : Boolean = true
) : UploadMediator {
//    private val deviceDataCollector: DeviceDataCollector
    private val webService = WebServiceFactory.getWebService(baseUrl, jsonAdapter,
        executor = networkExecutor)


/*    override fun upload(metrics: List<MetricModel<out Number>>, error: ErrorModel,
                        callback: (success : Boolean) -> Unit) {
        webService.post(null,null, BatchOperator.createBatchJson(
            metrics, error, configModule.config.libraryMetadata, apiVersion, jsonAdapter)
        , METRICS_ENDPOINT,
            object : RudderTypeAdapter<Map<*,*>>(){}, isGzipEnabled){

            (it.status in 200..299).apply(callback)
        }
    }*/


    companion object{
        private const val METRICS_ENDPOINT = "sdkmetrics"
    }

    override fun upload(snapshot: Snapshot, callback: (success: Boolean) -> Unit) {
        webService.post(null,null, snapshot.snapshot, METRICS_ENDPOINT,
            object : RudderTypeAdapter<Map<*,*>>(){}, isGzipEnabled){

            (it.status in 200..299).apply(callback)
        }
    }
}