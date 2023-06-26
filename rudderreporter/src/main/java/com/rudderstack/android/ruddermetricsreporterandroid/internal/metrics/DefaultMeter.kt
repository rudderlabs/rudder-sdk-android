/*
 * Creator: Debanjan Chatterjee on 14/06/23, 12:07 pm Last modified: 14/06/23, 12:03 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.metrics.AggregatorHandler
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongGauge
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Meter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.ShortGauge

class DefaultMeter(private val aggregatorHandler: AggregatorHandler) : Meter {
    override fun longCounter(name: String): LongCounter {
        return LongCounter(name, aggregatorHandler)
    }

}