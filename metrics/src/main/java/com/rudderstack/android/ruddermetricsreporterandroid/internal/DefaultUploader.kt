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

import com.rudderstack.android.ruddermetricsreporterandroid.Uploader
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.rudderjsonadapter.JsonAdapter

internal class DefaultUploader(
    dataCollectionModule: DataCollectionModule,
    private val jsonAdapter: JsonAdapter
) : Uploader {
    private val deviceDataCollector: DeviceDataCollector

    init {
        deviceDataCollector = dataCollectionModule.deviceDataCollector
    }

    override fun upload(metrics: List<MetricModel<Number>>, error: ErrorModel) {
        val device = deviceDataCollector.generateDevice()
        val requestMap = createRequestMap(metrics, error)
    }

    fun createRequestMap(metrics: List<MetricModel<Number>>, error: ErrorModel): Map<String, Any> {
        val requestMap = HashMap<String, Any>()
        requestMap["device"] = deviceDataCollector.generateDevice()
        requestMap["metrics"] = metrics
        requestMap["error"] = error
        return requestMap
    }
}