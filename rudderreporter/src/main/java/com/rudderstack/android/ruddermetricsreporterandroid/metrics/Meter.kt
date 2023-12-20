/*
 * Creator: Debanjan Chatterjee on 14/06/23, 11:11 am Last modified: 14/06/23, 11:11 am
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

interface Meter {
    fun longCounter(name: String): LongCounter
    //we will not be supporting gauges for now
//    fun longGauge(name: String): LongGauge
//    fun shortGauge(name: String?): ShortGauge
}