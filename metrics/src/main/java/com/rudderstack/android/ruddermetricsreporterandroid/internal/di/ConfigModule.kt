package com.rudderstack.android.ruddermetricsreporterandroid.internal.di

import com.rudderstack.android.ruddermetricsreporterandroid.internal.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Connectivity
import com.rudderstack.android.ruddermetricsreporterandroid.internal.sanitiseConfiguration

/**
 * A dependency module which constructs the configuration object that is used to alter
 * Bugsnag's default behaviour.
 */
internal class ConfigModule(
    contextModule: ContextModule,
    configuration: Configuration
) : DependencyModule() {

    val config = sanitiseConfiguration(contextModule.ctx, configuration)
}
