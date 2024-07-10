package com.rudderstack.core

import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The `Configuration` class defines the settings and parameters used to configure the RudderStack analytics SDK.
 * This class is open for inheritance to allow for customization and extension.
 *
 * @property jsonAdapter A `JsonAdapter` instance used for JSON serialization and deserialization.
 * @property options An instance of `RudderOption` which provides customizable options that can be set globally or on a per-message basis. Defaults to a new `RudderOption` instance.
 * @property flushQueueSize The size of the queue for events to be flushed to the server. Defaults to `FLUSH_QUEUE_SIZE` (30).
 * @property maxFlushInterval The maximum interval (in milliseconds) before events are flushed to the server if the queue size is not reached. Defaults to `MAX_FLUSH_INTERVAL` (10 seconds).
 * @property shouldVerifySdk A flag indicating whether the SDK should be verified. Changing this value post source config download has no effect. Defaults to `false`.
 * @property gzipEnabled A flag indicating whether GZIP compression is enabled for event uploads. Defaults to `true`.
 * @property sdkVerifyRetryStrategy A `RetryStrategy` instance defining the retry strategy for SDK verification. Changing this value post source config download has no effect. Defaults to exponential retry strategy.
 * @property dataPlaneUrl The URL of the data plane where events are sent. Defaults to `DATA_PLANE_URL`.
 * @property controlPlaneUrl The URL of the control plane for fetching the source config. Defaults to `CONTROL_PLANE_URL`.
 * @property logger A `Logger` instance for logging SDK activities. Defaults to an instance of `KotlinLogger`.
 * @property analyticsExecutor An `ExecutorService` for handling analytics tasks. Defaults to a single-thread executor.
 * @property networkExecutor An `ExecutorService` for handling network tasks. Defaults to a cached thread pool executor.
 * @property base64Generator A `Base64Generator` instance for encoding data in base64. Defaults to the `defaultBase64Generator` from `RudderUtils`.
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
