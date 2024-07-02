package com.rudderstack.android.ruddermetricsreporterandroid.internal.di

import com.rudderstack.android.ruddermetricsreporterandroid.internal.getActivityManager

/**
 * A dependency module which provides a reference to Android system services.
 */
internal class SystemServiceModule(
    contextModule: ContextModule,
) : DependencyModule() {

    val activityManager = contextModule.ctx.getActivityManager()
}
