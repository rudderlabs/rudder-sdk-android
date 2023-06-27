/*
 * Creator: Debanjan Chatterjee on 23/06/23, 6:06 pm Last modified: 23/06/23, 6:06 pm
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
package com.rudderstack.android.ruddermetricsreporterandroid

import android.content.Context
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BackgroundTaskService
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Connectivity
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ConnectivityCompat
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DataCollectionModule
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultMetrics
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultReservoir
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultSyncer
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultUploadMediator
import com.rudderstack.android.ruddermetricsreporterandroid.internal.NetworkChangeCallback
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ConfigModule
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ContextModule
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.SystemServiceModule
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MemoryTrimState
import com.rudderstack.android.ruddermetricsreporterandroid.internal.metrics.DefaultAggregatorHandler
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.AggregatorHandler
import com.rudderstack.rudderjsonadapter.JsonAdapter

class RudderReporter(
    private val aggregatorHandler: AggregatorHandler,
    private val syncer: Syncer
) {

    private var backgroundTaskService: BackgroundTaskService?= null

    constructor(reservoir: Reservoir, syncer: Syncer) : this(
        DefaultAggregatorHandler(reservoir),
        syncer
    )

    constructor(reservoir: Reservoir, uploadMediator: UploadMediator) : this(
        reservoir,
        DefaultSyncer(reservoir, uploadMediator)
    )

    constructor(
        context: Context,
        baseUrl: String,
        configuration: Configuration,
        jsonAdapter: JsonAdapter,
        backgroundTaskService: BackgroundTaskService? = null,
        useContentProvider: Boolean = false,
    ) : this(
        ContextModule(context), ConnectivityCompat(context, RudderReporterNetworkChangeCallback()),
        MemoryTrimState(), baseUrl, configuration, jsonAdapter,
        backgroundTaskService ?: BackgroundTaskService(), useContentProvider
    )

    internal constructor(
        contextModule: ContextModule,
        connectivity: Connectivity,
        memoryTrimState: MemoryTrimState,
        baseUrl: String,
        configuration: Configuration,
        jsonAdapter: JsonAdapter,
        backgroundTaskService: BackgroundTaskService,
        useContentProvider: Boolean,
    ) : this(
        contextModule,
        connectivity,
        memoryTrimState,
        baseUrl,
        ConfigModule(contextModule, configuration),
        jsonAdapter,
        backgroundTaskService,
        useContentProvider
    )

    internal constructor(
        contextModule: ContextModule,
        connectivity: Connectivity,
        memoryTrimState: MemoryTrimState,
        baseUrl: String,
        configModule: ConfigModule,
        jsonAdapter: JsonAdapter,
        backgroundTaskService: BackgroundTaskService,
        useContentProvider: Boolean,
    ) : this(
        DefaultReservoir(
            contextModule.ctx,
            useContentProvider,
            backgroundTaskService.databaseExecutor
        ),
        DefaultUploadMediator(
            /*DataCollectionModule(
                contextModule,
                configModule,
                SystemServiceModule(contextModule),
                backgroundTaskService, connectivity,
                memoryTrimState
            ),*/ configModule, baseUrl, jsonAdapter, backgroundTaskService.ioExecutor
        )
    ) {
        this.backgroundTaskService = backgroundTaskService
    }

    val metrics: Metrics = DefaultMetrics(aggregatorHandler, syncer)

    fun shutdown() {
        metrics.shutdown()
        backgroundTaskService?.shutdown()
    }

    internal class RudderReporterNetworkChangeCallback : NetworkChangeCallback {
        override fun invoke(hasConnection: Boolean, networkState: String) {
//            val data: MutableMap<String, Any> = HashMap()
//            data["hasConnection"] = hasConnection
//            data["networkState"] = networkState
//            leaveAutoBreadcrumb("Connectivity changed", BreadcrumbType.STATE, data)
            if (hasConnection) {
//                    eventStore.flushAsync();
            }
//            return null
        }
    }
}


