/*
 * Creator: Debanjan Chatterjee on 19/10/21, 4:02 PM Last modified: 19/10/21, 4:02 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.android.internal.sync

import android.app.Application
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
//import androidx.work.Worker
//import androidx.work.WorkerParameters
import com.rudderstack.core.Analytics
import com.rudderstack.core.Settings
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.lang.ref.WeakReference

/**
 * Syncs the data at an interval with rudder server
 * @param analytics - The Analytics object will be used in a weak reference to reduce memory leaks
 */
internal class RudderSyncWorker( /*analytics: Analytics,
                                 private val writeKey : String,
                                 private val settings: Settings,
                                 private val jsonAdapter: JsonAdapter,*/
                                 appContext: Context, workerParams: WorkerParameters
) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
//        .forceFlush()
        (applicationContext as? Application)?.sinkAnalytics?.blockingFlush()
        return Result.success()
    }

}