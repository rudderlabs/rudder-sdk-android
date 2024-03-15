package com.rudderstack.android

import androidx.annotation.VisibleForTesting
import com.rudderstack.core.Analytics
import java.util.concurrent.ConcurrentHashMap

/**
 * AnalyticsRegistry is a singleton object responsible for managing and providing instances of the Analytics class.
 * It maintains a mapping between write keys and Analytics instances using concurrent hash maps.
 *
 * The class provides methods for registering new Analytics instances with unique write keys,
 * as well as retrieving instances based on write keys.
 *
 * Usage:
 * - To register a new Analytics instance, use the [register] method, providing a write key and the Analytics instance.
 * - To retrieve an Analytics instance, use the [getInstance] method, passing the write key.
 *
 * Note: The class is marked as internal, indicating that it is intended for use within the same module and should not be accessed
 * from outside the module.
 */
internal object AnalyticsRegistry {

    private val writeKeyToInstance: ConcurrentHashMap<String, Analytics> = ConcurrentHashMap()

    /**
     * Registers a new Analytics instance with the provided write key and Analytics instance.
     * If an instance with the same write key already exists, it will not be overwritten.
     *
     * @param writeKey The unique identifier associated with the Analytics instance.
     * @param analytics The Analytics instance to be registered.
     */
    fun register(writeKey: String, analytics: Analytics) {
        writeKeyToInstance.putIfAbsent(writeKey, analytics)
    }

    /**
     * Retrieves an Analytics instance based on the provided write key.
     *
     * @param writeKey The write key associated with the desired Analytics instance.
     * @return The Analytics instance if found, otherwise null.
     */
    fun getInstance(writeKey: String): Analytics? {
        return writeKeyToInstance[writeKey]
    }

    /**
     * Clears the mapping of write keys to Analytics instances.
     * Note: This method is intended for use in testing scenarios only.
     */
    @VisibleForTesting
    fun clear() {
        writeKeyToInstance.clear()
    }
}
