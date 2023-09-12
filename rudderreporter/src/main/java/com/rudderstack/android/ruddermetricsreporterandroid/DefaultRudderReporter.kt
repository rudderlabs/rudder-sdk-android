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
import com.rudderstack.android.ruddermetricsreporterandroid.error.DefaultErrorClient
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorClient
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BackgroundTaskService
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Connectivity
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ConnectivityCompat
import com.rudderstack.android.ruddermetricsreporterandroid.internal.CustomDateAdapterMoshi
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
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DefaultRudderReporter(
    private val _metrics: Metrics,
    private val _errorClient: ErrorClient?,
    override val syncer: Syncer,
) : RudderReporter {

    private var connectivity: Connectivity? = null
    private var backgroundTaskService: BackgroundTaskService? = null

    @JvmOverloads
    constructor(
        context: Context,
        baseUrl: String,
        configuration: Configuration,
        jsonAdapter: JsonAdapter,
        isMetricsEnabled: Boolean = true,
        isErrorEnabled: Boolean = true,
        networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
        backgroundTaskService: BackgroundTaskService? = null,
        useContentProvider: Boolean = false,
    ) : this(
        ContextModule(context),
        baseUrl,
        configuration,
        jsonAdapter,
        isMetricsEnabled, isErrorEnabled,
        networkExecutor,
        backgroundTaskService ?: BackgroundTaskService(),
        useContentProvider,
    )

    internal constructor(
        contextModule: ContextModule,
        baseUrl: String,
        configuration: Configuration,
        jsonAdapter: JsonAdapter,
        isMetricsEnabled: Boolean = true,
        isErrorEnabled: Boolean = true,
        networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
        backgroundTaskService: BackgroundTaskService? = null,
        useContentProvider: Boolean = false,
    ) : this(
        contextModule,
        MemoryTrimState(),
        baseUrl,
        configuration,
        ConfigModule(contextModule, configuration),
        jsonAdapter,
        networkExecutor,
        backgroundTaskService ?: BackgroundTaskService(),
        useContentProvider,
        isMetricsEnabled,
        isErrorEnabled
    )

    constructor(
        context: Context,
        reservoir: Reservoir,
        configuration: Configuration,
        uploadMediator: UploadMediator,
        jsonAdapter: JsonAdapter,
        isMetricsEnabled: Boolean = true,
        isErrorEnabled: Boolean = true,
        backgroundTaskService: BackgroundTaskService? = null
    ) : this(
        ContextModule(context),
        reservoir,
        configuration,
        uploadMediator,
        jsonAdapter,
        MemoryTrimState(),
        isMetricsEnabled,
        isErrorEnabled,
        backgroundTaskService
    )


    internal constructor(
        contextModule: ContextModule,
        memoryTrimState: MemoryTrimState,
        baseUrl: String,
        configuration: Configuration,
        configModule: ConfigModule,
        jsonAdapter: JsonAdapter,
        networkExecutor: ExecutorService,
        backgroundTaskService: BackgroundTaskService,
        useContentProvider: Boolean,
        isMetricsAggregatorEnabled: Boolean,
        isErrorEnabled: Boolean
    ) : this(
        contextModule,
        DefaultReservoir(contextModule.ctx, useContentProvider),
        configuration,
        DefaultUploadMediator(configModule, baseUrl, jsonAdapter, networkExecutor),
        jsonAdapter,
        memoryTrimState,
        isMetricsAggregatorEnabled,
        isErrorEnabled,
        backgroundTaskService
    )

    internal constructor(
        contextModule: ContextModule,
        reservoir: Reservoir,
        configuration: Configuration,
        uploadMediator: UploadMediator,
        jsonAdapter: JsonAdapter,
        memoryTrimState: MemoryTrimState,
        isMetricsEnabled: Boolean = true,
        isErrorEnabled: Boolean = true,
        backgroundTaskService: BackgroundTaskService? = null
    ) : this(
        contextModule,
        reservoir,
        configuration,
        ConfigModule(contextModule, configuration),
        DefaultSyncer(reservoir, uploadMediator),
        jsonAdapter,
        memoryTrimState,
        isMetricsEnabled,
        isErrorEnabled,
        backgroundTaskService
    )

    private constructor(
        contextModule: ContextModule,
        reservoir: Reservoir,
        configuration: Configuration,
        configModule: ConfigModule,
        syncer: Syncer,
        jsonAdapter: JsonAdapter,
        memoryTrimState: MemoryTrimState,
        isMetricsEnabled: Boolean = true,
        isErrorEnabled: Boolean = true,
        backgroundTaskService: BackgroundTaskService? = null
    ):this(contextModule, reservoir, configuration, configModule, syncer, jsonAdapter.manipulate(),
        memoryTrimState,
        ConnectivityCompat(contextModule.ctx, RudderReporterNetworkChangeCallback(syncer)),
        isMetricsEnabled, isErrorEnabled, backgroundTaskService)

    private constructor(
        contextModule: ContextModule,
        reservoir: Reservoir,
        configuration: Configuration,
        configModule: ConfigModule,
        syncer: Syncer,
        jsonAdapter: JsonAdapter,
        memoryTrimState: MemoryTrimState,
        connectivity: Connectivity,
        isMetricsEnabled: Boolean = true,
        isErrorEnabled: Boolean = true,
        backgroundTaskService: BackgroundTaskService? = null
    ): this(
        DefaultMetrics(DefaultAggregatorHandler(reservoir, isMetricsEnabled), syncer),
        DefaultErrorClient(
            contextModule, configuration, configModule, DataCollectionModule(
                contextModule,
                configModule,
                SystemServiceModule(contextModule),
                backgroundTaskService ?: BackgroundTaskService(),
                connectivity,
                memoryTrimState
            ), reservoir, jsonAdapter, memoryTrimState, isErrorEnabled
        ),
        syncer
    ) {
        this.connectivity = connectivity
        this.backgroundTaskService = backgroundTaskService
    }

    init {
        connectivity?.registerForNetworkChanges()
    }
    override val metrics: Metrics get() = _metrics

    override val errorClient: ErrorClient
        get() = _errorClient ?: throw IllegalStateException(
            "ErrorClient is not initialized. " + "Using deprecated constructor?"
        )

    override fun shutdown() {
        syncer.stopScheduling()
        backgroundTaskService?.shutdown()
        connectivity?.unregisterForNetworkChanges()
    }


    //call unregister on shutdown
    internal class RudderReporterNetworkChangeCallback(private val syncer: Syncer) :
        NetworkChangeCallback {
        override fun invoke(hasConnection: Boolean, networkState: String) {

            if (hasConnection) {
                try {
                    syncer.flushAllMetrics()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    companion object{
        internal fun JsonAdapter.manipulate(): JsonAdapter {
            if(this is MoshiAdapter){
                add(CustomDateAdapterMoshi())
            }
            return this
        }
    }
}


