package com.rudderstack.android.ruddermetricsreporterandroid.internal

import android.content.Context
import com.rudderstack.android.ruddermetricsreporterandroid.error.BreadcrumbType
import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MetadataState

class Configuration {
    @JvmField
    internal val metadataState: MetadataState = MetadataState()

    var appVersion: String? = null
    var versionCode: Int? = 0
    var releaseStage: String? = null
    var launchDurationMillis: Long = DEFAULT_LAUNCH_CRASH_THRESHOLD_MS

    var sendLaunchCrashesSynchronously: Boolean = true
    var appType: String? = "android"
    var logger: Logger? = DebugLogger
        set(value) {
            field = value ?: DebugLogger
        }
//    var delivery: Delivery? = null
    var maxBreadcrumbs: Int = DEFAULT_MAX_BREADCRUMBS
    var maxPersistedEvents: Int = DEFAULT_MAX_PERSISTED_EVENTS
    var maxPersistedSessions: Int = DEFAULT_MAX_PERSISTED_SESSIONS
    var maxReportedThreads: Int = DEFAULT_MAX_REPORTED_THREADS
    var maxStringValueLength: Int = DEFAULT_MAX_STRING_VALUE_LENGTH

    var discardClasses: Set<String> = emptySet()
    var enabledReleaseStages: Set<String>? = null
    var enabledBreadcrumbTypes: Set<BreadcrumbType>? = null
    var projectPackages: Set<String> = emptySet()

    fun addMetadata(section: String, value: Map<String, Any?>) =
        metadataState.addMetadata(section, value)
    fun addMetadata(section: String, key: String, value: Any?) =
        metadataState.addMetadata(section, key, value)
    fun clearMetadata(section: String) = metadataState.clearMetadata(section)
    fun clearMetadata(section: String, key: String) = metadataState.clearMetadata(section, key)
    fun getMetadata(section: String) = metadataState.getMetadata(section)
    fun getMetadata(section: String, key: String) = metadataState.getMetadata(section, key)

    companion object {
        private const val DEFAULT_MAX_BREADCRUMBS = 100
        private const val DEFAULT_MAX_PERSISTED_SESSIONS = 128
        private const val DEFAULT_MAX_PERSISTED_EVENTS = 32
        private const val DEFAULT_MAX_REPORTED_THREADS = 200
        private const val DEFAULT_LAUNCH_CRASH_THRESHOLD_MS: Long = 5000
        private const val DEFAULT_MAX_STRING_VALUE_LENGTH = 10000

        @JvmStatic
        fun load(context: Context): Configuration = load(context)

    }
}