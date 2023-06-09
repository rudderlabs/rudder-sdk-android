package com.rudderstack.android.ruddermetricsreporterandroid.internal.di

import com.rudderstack.android.ruddermetricsreporterandroid.internal.getActivityManager
import com.rudderstack.android.ruddermetricsreporterandroid.internal.getStorageManager


/**
 * A dependency module which provides a reference to Android system services.
 */
internal class  SystemServiceModule(
    contextModule: ContextModule
) : DependencyModule() {

    val storageManager = contextModule.ctx.getStorageManager()
    val activityManager = contextModule.ctx.getActivityManager()
}
