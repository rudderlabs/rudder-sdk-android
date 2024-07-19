package com.rudderstack.android.providers

import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.Storage

fun provideAnalytics(
    writeKey: String,
    configuration: Configuration,
    dataUploadService: DataUploadService? = null,
    configDownloadService: ConfigDownloadService? = null,
    storage: Storage? = null,
    initializationListener: ((success: Boolean, message: String?) -> Unit)? = null,
    shutdownHook: (Analytics.() -> Unit)? = null
) = Analytics(
    writeKey = writeKey,
    configuration = configuration,
    storage = storage,
    initializationListener = initializationListener,
    dataUploadService = dataUploadService,
    configDownloadService = configDownloadService,
    shutdownHook = shutdownHook
)
