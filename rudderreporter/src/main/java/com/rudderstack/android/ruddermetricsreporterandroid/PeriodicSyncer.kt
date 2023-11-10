/*
 * Creator: Debanjan Chatterjee on 22/06/23, 7:59 pm Last modified: 22/06/23, 7:54 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot

interface PeriodicSyncer {
    @Deprecated("Use startPeriodicSyncs instead")
    fun startScheduledSyncs(
        interval: Long, flushOnStart: Boolean, flushCount: Long
    )
    fun startPeriodicSyncs(
        interval: Long, flushOnStart: Boolean, flushCount: Long
    )
    //setting null will nullify the callback
    fun setCallback(callback: ((uploadedSnapshot: Snapshot,
                                success: Boolean) -> Unit)?)

    fun stopScheduling()

    fun flushAllMetrics()

}