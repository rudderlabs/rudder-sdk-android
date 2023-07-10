/*
 * Creator: Debanjan Chatterjee on 23/06/23, 6:58 pm Last modified: 23/06/23, 6:58 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.Metrics
import com.rudderstack.android.ruddermetricsreporterandroid.Syncer
import com.rudderstack.android.ruddermetricsreporterandroid.internal.metrics.DefaultMeter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.AggregatorHandler
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Meter

class DefaultMetrics(private val aggregatorHandler: AggregatorHandler,
private val syncer: Syncer) : Metrics {
    override fun getMeter(): Meter {
        return DefaultMeter(aggregatorHandler)
    }

    override fun getSyncer(): Syncer {
        return syncer
    }

    override fun enable(enable: Boolean) {
        aggregatorHandler.enable(enable)
    }

    override fun shutdown() {
//        aggregatorHandler.shutdown()
        syncer.stopScheduling()
    }
}