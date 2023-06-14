/*
 * Creator: Debanjan Chatterjee on 14/06/23, 3:31 pm Last modified: 14/06/23, 3:31 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.metrics.Reservoir

class DefaultReservoir : Reservoir{
    override fun set(cursor: Reservoir.Cursor) {
        TODO("Not yet implemented")
    }

    override fun getAllMetric(): List<Reservoir.Cursor> {
        TODO("Not yet implemented")
    }

    override fun getMetricsFirst(limit: Int): List<Reservoir.Cursor> {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun removeFirst(limit: Int) {
        TODO("Not yet implemented")
    }

}