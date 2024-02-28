package com.rudderstack.android

import com.rudderstack.core.Analytics
import java.util.concurrent.ConcurrentHashMap

internal object AnalyticsInstanceRegistry {

    private val instances: ConcurrentHashMap<String, Analytics> = ConcurrentHashMap()

    fun register(instanceName: String, analytics: Analytics) {
        instances.putIfAbsent(instanceName, analytics)
    }

    fun unRegister(instanceName: String) {
        instances.remove(instanceName)
    }

    fun getInstance(instanceName: String): Analytics? {
        return instances[instanceName]
    }
}
