/*
 * Creator: Debanjan Chatterjee on 18/03/24, 6:20 pm Last modified: 18/03/24, 6:11 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.android.sync.internal

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rudderstack.android.sync.WorkManagerAnalyticsFactory
import com.rudderstack.core.Analytics

//import androidx.work.Worker
//import androidx.work.WorkerParameters

/**
 * Syncs the data at an interval with rudder server
 *
 */
internal class RudderSyncWorker(
    appContext: Context, workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    companion object {
        internal const val WORKER_ANALYTICS_FACTORY_KEY = "WORKER_ANALYTICS_FACTORY_KEY"
        internal const val WORKER_ANALYTICS_INSTANCE_KEY = "WORKER_ANALYTICS_INSTANCE_KEY"
    }

    override fun doWork(): Result {
        Log.e("RudderSyncWorker", "doWork")
        (applicationContext as? Application)?.let {
            val analyticsInstanceKey =
                inputData.getString(WORKER_ANALYTICS_INSTANCE_KEY) ?: return Result.failure()
            val weakSinkAnalytics = getAnalytics(analyticsInstanceKey)
            if (weakSinkAnalytics?.isShutdown == true) {
                weakSinkAnalytics.logger.warn(log = "Cannot do work. Analytics instance is " +
                                                    "already shutdown")
                return Result.failure()
            }
            val sinkAnalytics = (weakSinkAnalytics ?: createSinkAnalytics())
            val success = sinkAnalytics?.blockingFlush()
            sinkAnalytics?.logger?.debug(log = "Data upload through worker. success: $success")
            if (weakSinkAnalytics == null) sinkAnalytics?.shutdown()
            Log.e("RudderSyncWorker", "result :$success")

            return if (success == true) Result.success() else Result.failure()
        }
        return Result.failure()
    }

    private fun createSinkAnalytics(): Analytics? {
        val analyticsFactoryClassName = inputData.getString(WORKER_ANALYTICS_FACTORY_KEY)
        return analyticsFactoryClassName?.let {
            val analyticsFactory = Class.forName(it).newInstance() as WorkManagerAnalyticsFactory
            analyticsFactory.createAnalytics(applicationContext as Application)
        }

    }

}