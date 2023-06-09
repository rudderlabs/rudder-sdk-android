package com.rudderstack.android.ruddermetricsreporterandroid.internal

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.VisibleForTesting

internal class ManifestConfigLoader {

    companion object {
        // mandatory
        private const val RUDDER_IDENTIFIER = "com.rudderstack.android"

        // detection
        private const val AUTO_TRACK_SESSIONS = "$RUDDER_IDENTIFIER.AUTO_TRACK_SESSIONS"
        private const val SEND_THREADS = "$RUDDER_IDENTIFIER.SEND_THREADS"

        // app/project packages
        private const val APP_VERSION = "$RUDDER_IDENTIFIER.APP_VERSION"
        private const val VERSION_CODE = "$RUDDER_IDENTIFIER.VERSION_CODE"
        private const val RELEASE_STAGE = "$RUDDER_IDENTIFIER.RELEASE_STAGE"
        private const val ENABLED_RELEASE_STAGES = "$RUDDER_IDENTIFIER.ENABLED_RELEASE_STAGES"
        private const val DISCARD_CLASSES = "$RUDDER_IDENTIFIER.DISCARD_CLASSES"
        private const val PROJECT_PACKAGES = "$RUDDER_IDENTIFIER.PROJECT_PACKAGES"

        // misc
        private const val MAX_BREADCRUMBS = "$RUDDER_IDENTIFIER.MAX_BREADCRUMBS"
        private const val MAX_PERSISTED_EVENTS = "$RUDDER_IDENTIFIER.MAX_PERSISTED_EVENTS"
        private const val MAX_PERSISTED_SESSIONS = "$RUDDER_IDENTIFIER.MAX_PERSISTED_SESSIONS"
        private const val MAX_REPORTED_THREADS = "$RUDDER_IDENTIFIER.MAX_REPORTED_THREADS"
        private const val LAUNCH_CRASH_THRESHOLD_MS = "$RUDDER_IDENTIFIER.LAUNCH_CRASH_THRESHOLD_MS"
        private const val LAUNCH_DURATION_MILLIS = "$RUDDER_IDENTIFIER.LAUNCH_DURATION_MILLIS"
        private const val SEND_LAUNCH_CRASHES_SYNCHRONOUSLY = "$RUDDER_IDENTIFIER.SEND_LAUNCH_CRASHES_SYNCHRONOUSLY"
        private const val APP_TYPE = "$RUDDER_IDENTIFIER.APP_TYPE"
    }

    fun load(ctx: Context): Configuration {
        try {
            val packageManager = ctx.packageManager
            val packageName = ctx.packageName
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val data = ai.metaData
            return load(data)
        } catch (exc: Exception) {
            throw IllegalStateException("Bugsnag is unable to read config from manifest.", exc)
        }
    }

    /**
     * Populates the config with meta-data values supplied from the manifest as a Bundle.
     *
     * @param data   the manifest bundle
     */
    @VisibleForTesting
    internal fun load(data: Bundle?): Configuration {

        val config = Configuration()

        if (data != null) {
            loadAppConfig(config, data)

            // misc config
            with(config) {
                maxBreadcrumbs = data.getInt(MAX_BREADCRUMBS, maxBreadcrumbs)
                maxPersistedEvents = data.getInt(MAX_PERSISTED_EVENTS, maxPersistedEvents)
                maxPersistedSessions = data.getInt(MAX_PERSISTED_SESSIONS, maxPersistedSessions)
                maxReportedThreads = data.getInt(MAX_REPORTED_THREADS, maxReportedThreads)
                launchDurationMillis = data.getInt(
                    LAUNCH_CRASH_THRESHOLD_MS,
                    launchDurationMillis.toInt()
                ).toLong()
                launchDurationMillis = data.getInt(
                    LAUNCH_DURATION_MILLIS,
                    launchDurationMillis.toInt()
                ).toLong()
                sendLaunchCrashesSynchronously = data.getBoolean(
                    SEND_LAUNCH_CRASHES_SYNCHRONOUSLY,
                    sendLaunchCrashesSynchronously
                )
//                isAttemptDeliveryOnCrash = data.getBoolean(
//                    ATTEMPT_DELIVERY_ON_CRASH,
//                    isAttemptDeliveryOnCrash
//                )
            }
        }
        return config
    }

    private fun loadAppConfig(config: Configuration, data: Bundle) {
        with(config) {
            releaseStage = data.getString(RELEASE_STAGE, config.releaseStage)
            appVersion = data.getString(APP_VERSION, config.appVersion)
            appType = data.getString(APP_TYPE, config.appType)

            if (data.containsKey(VERSION_CODE)) {
                versionCode = data.getInt(VERSION_CODE)
            }
            if (data.containsKey(ENABLED_RELEASE_STAGES)) {
                enabledReleaseStages = getStrArray(data, ENABLED_RELEASE_STAGES, enabledReleaseStages)
            }
            discardClasses = getStrArray(data, DISCARD_CLASSES, discardClasses) ?: emptySet()
            projectPackages = getStrArray(data, PROJECT_PACKAGES, emptySet()) ?: emptySet()
//            redactedKeys = getStrArray(data, REDACTED_KEYS, redactedKeys) ?: emptySet()
        }
    }

    private fun getStrArray(
        data: Bundle,
        key: String,
        default: Set<String>?
    ): Set<String>? {
        val delimitedStr = data.getString(key)

        return when (val ary = delimitedStr?.split(",")) {
            null -> default
            else -> ary.toSet()
        }
    }
}
