package com.rudderstack.core

import com.rudderstack.core.internal.KotlinLogger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val FLUSH_QUEUE_SIZE = 30
const val MAX_FLUSH_INTERVAL = 10 * 1000L

data class ConfigurationImpl @JvmOverloads constructor(
    override val options: RudderOptions = RudderOptions.defaultOptions(),
    override val flushQueueSize: Int = FLUSH_QUEUE_SIZE,
    override val maxFlushInterval: Long = MAX_FLUSH_INTERVAL,
    override val shouldVerifySdk: Boolean = false,
    override val gzipEnabled: Boolean = true,
    override val sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
    override val dataPlaneUrl: String = "https://hosted.rudderlabs.com",
    override val controlPlaneUrl: String = "https://api.rudderstack.com/",
    override val logger: Logger = KotlinLogger,
    override val analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
    override val networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
    override val base64Generator: Base64Generator = RudderUtils.defaultBase64Generator,
) : Configuration {

    companion object {
        @JvmStatic
        fun create(options: RudderOptions): ConfigurationImpl {
            return ConfigurationImpl()
        }
    }
}