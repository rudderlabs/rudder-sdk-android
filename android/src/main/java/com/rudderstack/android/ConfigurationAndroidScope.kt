package com.rudderstack.android

import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.core.Base64Generator
import com.rudderstack.core.ConfigurationScope
import com.rudderstack.core.RudderLogger
import com.rudderstack.core.RudderOption
import java.util.concurrent.ExecutorService

sealed class ConfigurationAndroidMinimalScope(protected val currentConfiguration: ConfigurationAndroid) :
    ConfigurationScope(currentConfiguration) {

    var anonymousId: String? = currentConfiguration.anonymousId
    var userId: String? = currentConfiguration.userId
    var advertisingId: String? = currentConfiguration.advertisingId
    var deviceToken: String? = currentConfiguration.deviceToken
    var trackAutoSession: Boolean = currentConfiguration.trackAutoSession
    var sessionTimeoutMillis: Long = currentConfiguration.sessionTimeoutMillis
    var autoCollectAdvertisingId: Boolean = currentConfiguration.autoCollectAdvertId

    override fun build(): ConfigurationAndroid = ConfigurationAndroid(
        super.build(),
        application = currentConfiguration.application,
        anonymousId = anonymousId,
        userId = userId,
        advertisingId = advertisingId,
        deviceToken = deviceToken,
        trackAutoSession = trackAutoSession,
        sessionTimeoutMillis = sessionTimeoutMillis,
        autoCollectAdvertId = autoCollectAdvertisingId,
        trackLifecycleEvents = currentConfiguration.trackLifecycleEvents,
        recordScreenViews = currentConfiguration.recordScreenViews,
        isPeriodicFlushEnabled = currentConfiguration.isPeriodicFlushEnabled,
        multiProcessEnabled = currentConfiguration.multiProcessEnabled,
        defaultProcessName = currentConfiguration.defaultProcessName,
        rudderLogger = currentConfiguration.rudderLogger,
        collectDeviceId = currentConfiguration.collectDeviceId,
        advertisingIdFetchExecutor = currentConfiguration.advertisingIdFetchExecutor
    )

}

class ConfigurationAndroidScope(configurationAndroid: ConfigurationAndroid) :
    ConfigurationAndroidMinimalScope(configurationAndroid)

class ConfigurationAndroidInitializationScope(configurationAndroid: ConfigurationAndroid) :
    ConfigurationAndroidMinimalScope(configurationAndroid) {
    //These are yet to be supported.
    /*var multiProcessEnabled: Boolean = configurationAndroid.multiProcessEnabled
    var defaultProcessName: String? = configurationAndroid.defaultProcessName*/

    var trackLifecycleEvents: Boolean = configurationAndroid.trackLifecycleEvents

    var recordScreenViews: Boolean = configurationAndroid.recordScreenViews

    var isPeriodicFlushEnabled: Boolean = configurationAndroid.isPeriodicFlushEnabled


    var advertisingIdFetchExecutor: ExecutorService =
        configurationAndroid.advertisingIdFetchExecutor


    var logLevel = RudderLogger.DEFAULT_LOG_LEVEL
    override fun build(): ConfigurationAndroid {
        currentConfiguration.rudderLogger.activate(logLevel)
        return ConfigurationAndroid(
            configuration = super.build(),
            application = currentConfiguration.application,
            anonymousId = anonymousId,
            userId = userId,
            advertisingId = advertisingId,
            deviceToken = deviceToken,
            trackAutoSession = trackAutoSession,
            sessionTimeoutMillis = sessionTimeoutMillis,
            autoCollectAdvertId = autoCollectAdvertisingId,
            trackLifecycleEvents = trackLifecycleEvents,
            recordScreenViews = recordScreenViews,
            isPeriodicFlushEnabled = isPeriodicFlushEnabled,
            advertisingIdFetchExecutor = advertisingIdFetchExecutor,
        )
    }

}

