package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.DependencyModule

/**
 * A dependency module which constructs the objects that track state in Bugsnag. For example, this
 * class is responsible for creating classes which track the current breadcrumb/metadata state.
 */
internal class RudderErrorStateModule(
    cfg: ImmutableConfig,
    configuration: Configuration
) : DependencyModule() {

    val breadcrumbState = BreadcrumbState(cfg.maxBreadcrumbs, cfg.logger)

    val metadataState = copyMetadataState(configuration)

    private fun copyMetadataState(configuration: Configuration): MetadataState {
        // performs deep copy of metadata to preserve immutability of Configuration interface
        val orig = configuration.metadataState.metadata
        return configuration.metadataState.copy(metadata = orig.copy())
    }
}
