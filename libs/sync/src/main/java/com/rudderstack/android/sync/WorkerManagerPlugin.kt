package com.rudderstack.android.sync

import android.app.Application
import com.rudderstack.android.sync.internal.registerWorkManager
import com.rudderstack.android.sync.internal.unregisterWorkManager
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin

abstract class WorkerManagerPlugin : InfrastructurePlugin {

    private var application: Application? = null
    private var analyticsIdentifier: String? = null

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        analyticsIdentifier = analytics.writeKey
        val currentConfig = analytics.currentConfigurationAndroid
        if (currentConfig?.isPeriodicFlushEnabled != true) {
            currentConfig?.logger?.error(
                log = "Halting Worker manager plugin initialization since isPeriodicFlushEnabled configuration is false"
            )
            return
        }
        currentConfig.apply {
            this@WorkerManagerPlugin.application = this.application
            application.registerWorkManager(
                analytics, workManagerAnalyticsFactoryClassName
            )
        }
    }

    override fun shutdown() {
        application?.unregisterWorkManager(analyticsIdentifier ?: return)
        analyticsIdentifier = null
    }

    /**
     * Internal classes are not supported.
     * This is because instantiating an inner class requires the parent class instance.
     * It's not worth it to try finding an instance in Heap. Cause this approach might conflict
     * with garbage collector
     */
    abstract val workManagerAnalyticsFactoryClassName: Class<out WorkManagerAnalyticsFactory>
}
