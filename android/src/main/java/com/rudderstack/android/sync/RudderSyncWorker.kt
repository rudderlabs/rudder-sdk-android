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

package com.rudderstack.android.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Syncs the data at an interval with rudder server
 *
 */
class RudderSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
//        val unsentData = RudderDatabase.getDao()
        return Result.success()
    }

}