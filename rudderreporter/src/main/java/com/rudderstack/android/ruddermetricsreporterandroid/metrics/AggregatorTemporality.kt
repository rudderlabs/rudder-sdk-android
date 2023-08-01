/*
 * Creator: Debanjan Chatterjee on 13/06/23, 11:00 pm Last modified: 13/06/23, 11:00 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.metrics
/**
 * Describes the time period over which measurements are aggregated.
 *
 */
enum class AggregatorTemporality {
    /** Measurements are aggregated since the previous collection. */
    DELTA,
    /** Measurements are aggregated over the lifetime of the instrument. */
    CUMULATIVE
}