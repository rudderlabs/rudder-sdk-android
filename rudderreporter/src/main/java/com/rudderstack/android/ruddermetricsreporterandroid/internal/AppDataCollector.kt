package com.rudderstack.android.ruddermetricsreporterandroid.internal

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.ImmutableConfig
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MemoryTrimState

/**
 * Collects various data on the application state
 */
internal class AppDataCollector(
    appContext: Context,
    private val packageManager: PackageManager?,
    private val config: ImmutableConfig,
    private val activityManager: ActivityManager?,
    private val memoryTrimState: MemoryTrimState
) {


    var codeBundleId: String? = null

    private val packageName: String = appContext.packageName
    private var binaryArch: String? = null
    private val appName = getAppName()
    private val processName = findProcessName()
    private val releaseStage = config.releaseStage
    private val versionName = config.appVersion ?: config.packageInfo?.versionName
    private val installerPackage = getInstallerPackageName()

    private val bgWorkRestricted
        get() = isBackgroundWorkRestricted()

    fun generateApp(): App =
        App(config, binaryArch, packageName, releaseStage, versionName, codeBundleId)

    fun generateAppWithState(): AppWithState {
        return AppWithState(
            config, binaryArch, packageName, releaseStage, versionName, codeBundleId
        )
    }

    fun getAppDataMetadata(): MutableMap<String, Any?> {
        val map = HashMap<String, Any?>()
        map["name"] = appName
        map["lowMemory"] = memoryTrimState.isLowMemory
        map["memoryTrimLevel"] = memoryTrimState.trimLevelDescription
        bgWorkRestricted?.let {
            map["backgroundWorkRestricted"] = bgWorkRestricted
        }
        processName?.let {
            map["processName"] = it
        }
        populateRuntimeMemoryMetadata(map)

        processName?.let {
            map["processName"] = it
        }
        return map
    }

    private fun populateRuntimeMemoryMetadata(map: MutableMap<String, Any?>) {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        map["memoryUsage"] = totalMemory - freeMemory
        map["totalMemory"] = totalMemory
        map["freeMemory"] = freeMemory
        map["memoryLimit"] = runtime.maxMemory()
        map["installerPackage"] = installerPackage
    }

    /**
     * Checks whether the user has restricted the amount of work this app can do in the background.
     * https://developer.android.com/reference/android/app/ActivityManager#isBackgroundRestricted()
     */
    private fun isBackgroundWorkRestricted(): Boolean? {
        return if (activityManager == null || VERSION.SDK_INT < VERSION_CODES.P) {
            null
        } else if (activityManager.isBackgroundRestricted) {
            true // only return non-null value if true to avoid noise in error reports
        } else {
            null
        }
    }

    fun setBinaryArch(binaryArch: String) {
        this.binaryArch = binaryArch
    }

    /**
     * The name of the running Android app, from android:label in
     * AndroidManifest.xml
     */
    private fun getAppName(): String? {
        val copy = config.appInfo
        return when {
            packageManager != null && copy != null -> {
                packageManager.getApplicationLabel(copy).toString()
            }
            else -> null
        }
    }

    /**
     * The name of installer / vendor package of the app
     */
    fun getInstallerPackageName(): String? {
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.R)
                return packageManager?.getInstallSourceInfo(packageName)?.installingPackageName
            @Suppress("DEPRECATION")
            return packageManager?.getInstallerPackageName(packageName)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Finds the name of the current process, or null if this cannot be found.
     */
    @SuppressLint("PrivateApi")
    private fun findProcessName(): String? {
        return runCatching {
            when {
                VERSION.SDK_INT >= VERSION_CODES.P -> {
                    Application.getProcessName()
                }
                else -> {
                    // see https://stackoverflow.com/questions/19631894
                    val clz = Class.forName("android.app.ActivityThread")
                    val methodName = when {
                        VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2 -> "currentProcessName"
                        else -> "currentPackageName"
                    }

                    val getProcessName = clz.getDeclaredMethod(methodName)
                    getProcessName.invoke(null) as String
                }
            }
        }.getOrNull()
    }

    companion object {
//        internal val startTimeMs = SystemClock.elapsedRealtime()

        /**
         * Get the time in milliseconds since Bugsnag was initialized, which is a
         * good approximation for how long the app has been running.
         * Not required now.
         */
//        fun getDurationMs(): Long = SystemClock.elapsedRealtime() - startTimeMs
    }
}
