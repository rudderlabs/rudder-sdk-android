package com.rudderstack.core

import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The `Configuration` class defines the settings and parameters used to configure the RudderStack analytics SDK.
 * This class is open for inheritance to allow for customization and extension.
 *
 */
open class Configuration(
    open val jsonAdapter: JsonAdapter,
    open val options: RudderOption = RudderOption(),
    open val flushQueueSize: Int = FLUSH_QUEUE_SIZE,
    open val maxFlushInterval: Long = MAX_FLUSH_INTERVAL,
    // changing the value post source config download has no effect
    open val shouldVerifySdk: Boolean = false,
    open val gzipEnabled: Boolean = true,
    // changing the value post source config download has no effect
    open val sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
    open val dataPlaneUrl: String = DATA_PLANE_URL,
    open val controlPlaneUrl: String = CONTROL_PLANE_URL,
    open val logger: Logger = KotlinLogger(),
    open val analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
    open val networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
    open val base64Generator: Base64Generator = RudderUtils.defaultBase64Generator,
) {
    companion object {
        // default flush queue size for the events to be flushed to server
        const val FLUSH_QUEUE_SIZE = 30

        // default timeout for event flush
        // if events are registered and flushQueueSize is not reached
        // events will be flushed to server after maxFlushInterval millis
        const val MAX_FLUSH_INTERVAL = 10 * 1000L //10 seconds
        const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"
        const val CONTROL_PLANE_URL = "https://api.rudderstack.com/"
    }
}
