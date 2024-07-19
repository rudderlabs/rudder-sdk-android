package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.utilities.applyConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.InfrastructurePlugin

internal class AnonymousIdHeaderPlugin : InfrastructurePlugin {

    override lateinit var analytics: Analytics
    private var dataUploadService: DataUploadService? = null

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        dataUploadService = analytics.dataUploadService
    }

    override fun updateConfiguration(configuration: Configuration) {
        if (configuration !is ConfigurationAndroid) return
        val anonId = configuration.anonymousId ?: AndroidUtils.generateAnonymousId().also {
            analytics.applyConfigurationAndroid {
                copy(anonymousId = it)
            }
        }
        dataUploadService?.addHeaders(mapOf("Anonymous-Id" to anonId))
    }

    override fun shutdown() {
        dataUploadService = null
    }
}
