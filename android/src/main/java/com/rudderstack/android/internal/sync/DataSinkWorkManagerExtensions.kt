/*
 * Creator: Debanjan Chatterjee on 15/08/22, 12:52 AM Last modified: 15/08/22, 12:52 AM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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
import androidx.work.*
import androidx.work.multiprocess.RemoteWorkManager
import com.rudderstack.android.RudderAnalytics
import com.rudderstack.core.Analytics
import com.rudderstack.core.Settings
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

internal fun Application.createSinkAnalytics() = latestConfig?.let {
    RudderAnalytics(
        this,
        it.writeKey,
        Settings(),
        it.jsonAdapter,
        dataPlaneUrl = it.dataPlaneUrl,
        controlPlaneUrl = it.controlPlaneUrl,
        logger = it.logger,
        defaultProcessName = it.processName,
        multiProcessEnabled = it.processName != null
    )
}

private var latestConfig: RudderWorkerConfig? = null

private val constraints by lazy {
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
private val sinkWorker by lazy {
    PeriodicWorkRequestBuilder<RudderSyncWorker>(
        REPEAT_INTERVAL_IN_MINS, TimeUnit.MINUTES
    ).setInitialDelay(REPEAT_INTERVAL_IN_MINS, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .addTag(WORK_MANAGER_TAG)
        .build()
}
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<HANDLE MEMORY LEAK>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<block flush works after shutdown>>>>>>>>>>>>>>>>>>>>>>>>>>
internal fun Application.registerWorkManager(
    analytics: Analytics,
    rudderWorkerConfig: RudderWorkerConfig
) {
    //if analytics object has changed, shutting it down is not this method's responsibility
    analyticsRef = WeakReference(analytics)
    latestConfig = rudderWorkerConfig

    Configuration.Builder().also {
        // if process name is available, this is a multi-process app
        rudderWorkerConfig.processName?.apply {
            it.setDefaultProcessName(this)
        }
        rudderWorkerConfig.networkExecutorService?.apply {
            it.setExecutor(this)
        }
    }
//        .takeIf { rudderWorkerConfig.processName != null || rudderWorkerConfig.networkExecutorService != null }
        ?.let {
            WorkManager.initialize(this, it.build())
        }
    if (rudderWorkerConfig.processName != null) {
        RemoteWorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE,
            sinkWorker
        )
    }
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE,
        sinkWorker
    )

}
