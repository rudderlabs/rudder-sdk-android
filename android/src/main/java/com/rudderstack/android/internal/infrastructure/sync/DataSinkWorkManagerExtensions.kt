/*
 * Creator: Debanjan Chatterjee on 23/11/23, 6:20 pm Last modified: 21/11/23, 5:14 pm
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

package com.rudderstack.android.internal.infrastructure.sync

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.multiprocess.RemoteWorkManager
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Aids in data syncing to server with help of work manager.
 * Only works if work manager dependency is added externally
 * Also, if multi-process support is needed, set the default process name.
 * For multi-process support, the following dependency is expected
 * Implementation "androidx.work:work-multiprocess:2.5.x"
 */

private var analyticsRef: WeakReference<Analytics>? = null
private const val WORK_MANAGER_TAG = "rudder_sink"
private const val WORK_NAME = "rudder_sink_work"
private const val REPEAT_INTERVAL_IN_MINS = 15L

internal val Application.sinkAnalytics
    get() = analyticsRef?.get()?.takeUnless { it.isShutdown }


private val constraints by lazy {
    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
}
private fun sinkWorker(workManagerAnalyticsFactoryClassName: Class<out
WorkManagerAnalyticsFactory>) =
    PeriodicWorkRequestBuilder<RudderSyncWorker>(
        REPEAT_INTERVAL_IN_MINS, TimeUnit.MINUTES
    ).setInitialDelay(REPEAT_INTERVAL_IN_MINS, TimeUnit.MINUTES).setConstraints(constraints)
        .setInputData(Data.Builder().putString(
            RudderSyncWorker.WORKER_ANALYTICS_FACTORY_KEY,
            workManagerAnalyticsFactoryClassName.name
        ).build())
        .addTag(WORK_MANAGER_TAG).build()

//<<<<<<<<<<<<<<<<<<<<<<<<<<<<block flush works after shutdown>>>>>>>>>>>>>>>>>>>>>>>>>>
internal fun Application.registerWorkManager(
    analytics: Analytics, workManagerAnalyticsFactoryClass: Class<out WorkManagerAnalyticsFactory>
) {
    analytics.logger.debug(log = "Initializing work manager")
    //if analytics object has changed, shutting it down is not this method's responsibility
    analyticsRef = WeakReference(analytics)

    Configuration.Builder().also {
        // if process name is available, this is a multi-process app
        analytics.currentConfigurationAndroid?.defaultProcessName?.apply {
            it.setDefaultProcessName(this)
        }
        analytics.currentConfigurationAndroid?.networkExecutor?.apply {
            it.setExecutor(this)
        }
    }.let {
            WorkManager.initialize(this, it.build())
        }
    if (analytics.currentConfigurationAndroid?.multiProcessEnabled == true) {
        RemoteWorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, sinkWorker(workManagerAnalyticsFactoryClass)
        )
    } else WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, sinkWorker(workManagerAnalyticsFactoryClass)
    )

}
internal fun Application.unregisterWorkManager() {
    analyticsRef?.clear()
    analyticsRef = null
    WorkManager.getInstance(this).cancelAllWorkByTag(WORK_MANAGER_TAG)
    RemoteWorkManager.getInstance(this).cancelAllWorkByTag(WORK_MANAGER_TAG)
}
