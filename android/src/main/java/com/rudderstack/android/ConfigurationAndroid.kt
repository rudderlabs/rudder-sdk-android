package com.rudderstack.android

import android.app.Application
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Base64Generator
import com.rudderstack.core.Configuration
import com.rudderstack.core.Logger
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.RudderOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val DEFAULT_ANDROID_DATAPLANE_URL = "https://hosted.rudderlabs.com"
const val DEFAULT_ANDROID_CONTROLPLANE_URL = "https://api.rudderlabs.com"
const val GZIP_ENABLED: Boolean = true
const val SHOULD_VERIFY_SDK: Boolean = true
const val TRACK_LIFECYCLE_EVENTS = true
const val RECORD_SCREEN_VIEWS = true
const val IS_PERIODIC_FLUSH_ENABLED = false
const val AUTO_COLLECT_ADVERT_ID = true
const val MULTI_PROCESS_ENABLED = false
const val USE_CONTENT_PROVIDER = false
const val DEFAULT_FLUSH_QUEUE_SIZE = 30
const val DEFAULT_MAX_FLUSH_INTERVAL = 10 * 1000L
const val SESSION_TIMEOUT: Long = 300000
const val AUTO_SESSION_TRACKING = true
val DEFAULT_PROCESS_NAME: String? = null

data class ConfigurationAndroid @JvmOverloads constructor(
    val application: Application,
    val anonymousId: String? = null,
    val userId: String? = null,
    val advertisingId: String? = null,
    val deviceToken: String? = null,
    val advertisingIdFetchExecutor: ExecutorService? = null,
    val trackLifecycleEvents: Boolean = TRACK_LIFECYCLE_EVENTS,
    val recordScreenViews: Boolean = RECORD_SCREEN_VIEWS,
    val isPeriodicFlushEnabled: Boolean = IS_PERIODIC_FLUSH_ENABLED,
    val autoCollectAdvertId: Boolean = AUTO_COLLECT_ADVERT_ID,
    val multiProcessEnabled: Boolean = MULTI_PROCESS_ENABLED,
    val defaultProcessName: String? = DEFAULT_PROCESS_NAME,
    val trackAutoSession: Boolean = AUTO_SESSION_TRACKING,
    val sessionTimeoutMillis: Long = SESSION_TIMEOUT,
    override val options: RudderOptions = RudderOptions.defaultOptions(),
    override val flushQueueSize: Int = DEFAULT_FLUSH_QUEUE_SIZE,
    override val maxFlushInterval: Long = DEFAULT_MAX_FLUSH_INTERVAL,
    override val shouldVerifySdk: Boolean = SHOULD_VERIFY_SDK,
    override val gzipEnabled: Boolean = GZIP_ENABLED,
    override val sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
    override val logger: Logger = AndroidLogger,
    override val analyticsExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
    override val networkExecutor: ExecutorService = Executors.newCachedThreadPool(),
    override val base64Generator: Base64Generator = AndroidUtils.defaultBase64Generator(),
    override val dataPlaneUrl: String = DEFAULT_ANDROID_DATAPLANE_URL,
    override val controlPlaneUrl: String = DEFAULT_ANDROID_CONTROLPLANE_URL,
) : Configuration {

    companion object {

        @JvmStatic
        val processName: String? = DEFAULT_PROCESS_NAME

        @JvmStatic
        fun create(application: Application, storage: AndroidStorage? = null): ConfigurationAndroid {
            return ConfigurationAndroid(application = application, trackAutoSession = storage?.trackAutoSession ?: false)
        }
    }
}