/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:30 pm Last modified: 05/06/23, 5:52 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal.error

import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.DependencyModule
import java.util.concurrent.ConcurrentHashMap

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
