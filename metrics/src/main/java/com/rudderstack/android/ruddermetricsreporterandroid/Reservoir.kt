/*
 * Creator: Debanjan Chatterjee on 14/06/23, 5:02 pm Last modified: 14/06/23, 5:02 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid

import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel

interface Reservoir {
    fun insertOrIncrement(metric: MetricModel<Number>)
    fun getAllMetricsSync(): List<MetricModel<Long>>
    fun getAllMetrics(callback : (List<MetricModel<out Number>>) -> Unit)

    fun getMetricsFirstSync(limit : Long): List<MetricModel<*>>
    fun getMetricsFirst(limit : Long, callback : (List<MetricModel<Number>>) -> Unit)
//    fun getMetricsAndErrorFirst(limit : Long, callback : (List<MetricModel<Number>>, List<ErrorModel>) -> Unit)
    fun getMetricsCount(callback : (Long) -> Unit)
    fun clear()
    fun resetFirst(limit: Long)
    fun reset()
    interface DataListener {
        /**
         * Called whenever there's a change in data count
         *
         */
        fun onDataChange()

    }
    //this is a combined response class
}