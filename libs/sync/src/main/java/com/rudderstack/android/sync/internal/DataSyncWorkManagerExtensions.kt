/*
 * Creator: Debanjan Chatterjee on 18/03/24, 6:22 pm Last modified: 04/01/24, 5:47 pm
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
import androidx.annotation.VisibleForTesting
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.multiprocess.RemoteWorkManager
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.sync.WorkManagerAnalyticsFactory
import com.rudderstack.core.Analytics
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Aids in data syncing to server with help of work manager.
 * Only works if work manager dependency is added externally
 * Also, if multi-process support is needed, set the default process name.
 * For multi-process support, the following dependency is expected
 * Implementation "androidx.work:work-multiprocess:2.5.x"
 */
private const val WORK_MANAGER_TAG = "rudder_sync"
private const val WORK_NAME = "rudder_sync_work"
private const val REPEAT_INTERVAL_IN_MINS = 15L
private var analyticsRefMap = ConcurrentHashMap<String, WeakReference<Analytics>>()

private fun Analytics.generateKeyForLabel(label: String) = addKeyToLabel(label, writeKey)
private fun addKeyToLabel(label: String, key: String) = "${label}_$key"
internal fun getAnalytics(key: String) = analyticsRefMap[key]?.get()


private val constraints by lazy {
    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
}

private fun Analytics.syncWorker(
    workManagerAnalyticsFactoryClassName: Class<out WorkManagerAnalyticsFactory>
) = PeriodicWorkRequestBuilder<RudderSyncWorker>(
    REPEAT_INTERVAL_IN_MINS, TimeUnit.MINUTES
).setInitialDelay(REPEAT_INTERVAL_IN_MINS, TimeUnit.MINUTES).setConstraints(constraints)
    .setInputData(
        workerInputData(workManagerAnalyticsFactoryClassName)
    ).addTag(generateKeyForLabel(WORK_MANAGER_TAG)).build()

@VisibleForTesting
internal fun Analytics.workerInputData(workManagerAnalyticsFactoryClassName: Class<out
WorkManagerAnalyticsFactory>) =
    Data.Builder().putString(
        RudderSyncWorker.WORKER_ANALYTICS_FACTORY_KEY, workManagerAnalyticsFactoryClassName.name
    ).putString(RudderSyncWorker.WORKER_ANALYTICS_INSTANCE_KEY, writeKey).build()

internal fun Application.registerWorkManager(
    analytics: Analytics, workManagerAnalyticsFactoryClass: Class<out WorkManagerAnalyticsFactory>
) {
    analytics.logger.debug(log = "Initializing work manager")
    if (getAnalytics(analytics.writeKey)?.takeUnless { it.isShutdown } != null) {
        analytics.logger.debug(log = "Work manager already initialized")
        return
    }

    analyticsRefMap[analytics.writeKey] = WeakReference(analytics)

    Configuration.Builder().also {
        // if process name is available, this is a multi-process app
        analytics.currentConfigurationAndroid?.defaultProcessName?.apply {
            it.setDefaultProcessName(this)
        }
        analytics.currentConfigurationAndroid?.networkExecutor?.apply {
            it.setExecutor(this)
        }
    }
    if (analytics.currentConfigurationAndroid?.multiProcessEnabled == true) {
        RemoteWorkManager.getInstance(this).enqueueUniquePeriodicWork(
            analytics.generateKeyForLabel(WORK_NAME),
            ExistingPeriodicWorkPolicy.KEEP,
            analytics.syncWorker(workManagerAnalyticsFactoryClass)
        )
    } else {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            analytics.generateKeyForLabel(WORK_NAME),
            ExistingPeriodicWorkPolicy.KEEP,
            analytics.syncWorker(workManagerAnalyticsFactoryClass)
        )
    }

}

fun Application.unregisterWorkManager(writeKey: String) {
    analyticsRefMap[writeKey]?.clear()
    analyticsRefMap.remove(writeKey)
    WorkManager.getInstance(this)
        .cancelAllWorkByTag(addKeyToLabel(WORK_MANAGER_TAG, writeKey))
    RemoteWorkManager.getInstance(this)
        .cancelAllWorkByTag(addKeyToLabel(WORK_MANAGER_TAG, writeKey))
}
