/*
 * Creator: Debanjan Chatterjee on 09/06/23, 7:28 pm Last modified: 09/06/23, 7:28 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Meter

interface Metrics {
    fun getMeter(): Meter

    @Deprecated("Use [RudderReporter.syncer] instead")
    fun getSyncer(): Syncer

    /**
     * Enables or disables recording of metrics. However unless shut down, already recorded
     * events will reach server
     *
     * @param enable
     */
    fun enable(enable: Boolean)

    /**
     * Shuts down the metrics reporter. No more events including errors will be sent to
     * server, however errors will be recorded. Do not use this if you're notifying errors
     *
     */
    @Deprecated("Use [RudderReporter.shutdown] instead")
    fun shutdown()
    fun getLongCounter(name: String): LongCounter = getMeter().longCounter(name)
}
