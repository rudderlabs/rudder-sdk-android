package com.rudderstack.android.ruddermetricsreporterandroid.internal.di

import android.content.Context

/**
 * A dependency module which accesses the application context object, falling back to the supplied
 * context if it is the base context.
 */
internal class ContextModule(
    appContext: Context
) : DependencyModule() {

    val ctx: Context = when (appContext.applicationContext) {
        null -> appContext
        else -> appContext.applicationContext
    }
}
