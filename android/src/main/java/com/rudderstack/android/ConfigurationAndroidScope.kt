package com.rudderstack.android

import com.rudderstack.core.ConfigurationScope
import java.util.concurrent.ExecutorService

sealed class ConfigurationAndroidMinimalScope(configurationAndroid: ConfigurationAndroid) :
    ConfigurationScope(configurationAndroid) {
    protected val application = configurationAndroid.application
    var anonymousId: String? = configurationAndroid.anonymousId
    var userId: String? = configurationAndroid.userId
    var trackLifecycleEvents: Boolean = configurationAndroid.trackLifecycleEvents
    var recordScreenViews: Boolean = configurationAndroid.recordScreenViews
    var isPeriodicFlushEnabled: Boolean = configurationAndroid.isPeriodicFlushEnabled
    var autoCollectAdvertId: Boolean = configurationAndroid.autoCollectAdvertId
    var advertisingId: String? = configurationAndroid.advertisingId
    var deviceToken: String? = configurationAndroid.deviceToken
    var advertisingIdFetchExecutor : ExecutorService? = configurationAndroid.advertisingIdFetchExecutor
    var trackAutoSession: Boolean = configurationAndroid.trackAutoSession
    var sessionTimeoutMillis: Long = configurationAndroid.sessionTimeoutMillis


}
class ConfigurationAndroidScope(configurationAndroid: ConfigurationAndroid) : ConfigurationAndroidMinimalScope(configurationAndroid){
    override fun build(): ConfigurationAndroid {
        return ConfigurationAndroid.invoke(super.build(),
            application,
            anonymousId,
            userId,
            trackLifecycleEvents,
            recordScreenViews,
            isPeriodicFlushEnabled,
            autoCollectAdvertId,
            advertisingId = advertisingId,
            deviceToken = deviceToken,
            advertisingIdFetchExecutor = advertisingIdFetchExecutor,
            trackAutoSession = trackAutoSession,
            sessionTimeoutMillis = sessionTimeoutMillis)
    }
}
class ConfigurationAndroidInitializationScope(configurationAndroid: ConfigurationAndroid) : ConfigurationAndroidMinimalScope(configurationAndroid){
    //These are yet to be supported.
    /*var multiProcessEnabled: Boolean = configurationAndroid.multiProcessEnabled
    var defaultProcessName: String? = configurationAndroid.defaultProcessName*/
    override fun build(): ConfigurationAndroid {
        return ConfigurationAndroid.invoke(super.build(),
            application,
            anonymousId,
            userId,
            trackLifecycleEvents,
            recordScreenViews,
            isPeriodicFlushEnabled,
            autoCollectAdvertId,
//            multiProcessEnabled = multiProcessEnabled, //yet to be supported
//            defaultProcessName = defaultProcessName,
            advertisingId = advertisingId,
            deviceToken = deviceToken,
            advertisingIdFetchExecutor = advertisingIdFetchExecutor,
            trackAutoSession = trackAutoSession,
            sessionTimeoutMillis = sessionTimeoutMillis)
    }

}

