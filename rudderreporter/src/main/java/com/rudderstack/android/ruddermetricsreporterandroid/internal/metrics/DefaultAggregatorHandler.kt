/*
 * Creator: Debanjan Chatterjee on 17/06/23, 5:14 pm Last modified: 17/06/23, 5:14 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal.metrics

import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.AggregatorHandler
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Labels
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongGauge
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType

class DefaultAggregatorHandler(private val reservoir: Reservoir) : AggregatorHandler {
    override fun LongCounter.recordMetric(value: Long) {
        recordMetric(value, Labels.of())
    }

    override fun LongCounter.recordMetric(value: Long, attributes: Labels) {
        reservoir.insertOrIncrement(MetricModel(name, MetricType.COUNTER,
            value, attributes))
    }

    override fun LongGauge.recordMetric(value: Long) {
        recordMetric(value, Labels.of())
    }

    override fun LongGauge.recordMetric(value: Long, attributes: Labels) {
        reservoir.insertOrIncrement(MetricModel(name, MetricType.GAUGE,
            value, attributes))
    }
}