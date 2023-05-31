package com.rudderstack.android.ruddermetricsreporterandroid.internal

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.rudderstack.android.ruddermetricsreporterandroid.BreadcrumbType
import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import java.io.File

data class ImmutableConfig(
    val projectPackages: Collection<String>,
    val enabledBreadcrumbTypes: Set<BreadcrumbType>?,
    val appVersion: String?,
    val discardClasses: Collection<String>,
    val versionCode: Int?,
    val logger: Logger,
    val maxBreadcrumbs: Int,
    val maxPersistedEvents: Int,
    val enabledReleaseStages: Collection<String>?,
    val releaseStage: String?,
    // results cached here to avoid unnecessary lookups in Client.
    val packageInfo: PackageInfo?,
    val appInfo: ApplicationInfo?,
) {
    /**
     * Returns whether the given throwable should be discarded
     * based on the automatic data capture settings in [Configuration].
     */
    fun shouldDiscardError(exc: Throwable): Boolean {
        return shouldDiscardByReleaseStage() || shouldDiscardByErrorClass(exc)
    }

    /**
     * Returns whether the given error should be discarded
     * based on the automatic data capture settings in [Configuration].
     */
    fun shouldDiscardError(errorClass: String?): Boolean {
        return shouldDiscardByReleaseStage() || shouldDiscardByErrorClass(errorClass)
    }

    /**
     * Returns whether breadcrumbs with the given type should be discarded or not.
     */
    fun shouldDiscardBreadcrumb(type: BreadcrumbType): Boolean {
        return enabledBreadcrumbTypes != null && !enabledBreadcrumbTypes.contains(type)
    }

    /**
     * Returns whether errors/sessions should be discarded or not based on the enabled
     * release stages.
     */
    fun shouldDiscardByReleaseStage(): Boolean {
        return enabledReleaseStages != null && !enabledReleaseStages.contains(releaseStage)
    }

    /**
     * Returns whether errors with the given errorClass should be discarded or not.
     */
    @VisibleForTesting
    internal fun shouldDiscardByErrorClass(errorClass: String?): Boolean {
        return discardClasses.contains(errorClass)
    }

    /**
     * Returns whether errors should be discarded or not based on the errorClass, as deduced
     * by the Throwable's class name.
     */
    @VisibleForTesting
    internal fun shouldDiscardByErrorClass(exc: Throwable): Boolean {
        return exc.safeUnrollCauses().any { throwable ->
            val errorClass = throwable.javaClass.name
            shouldDiscardByErrorClass(errorClass)
        }
    }
}
@JvmOverloads
internal fun convertToImmutableConfig(
    config: Configuration,
    packageInfo: PackageInfo? = null,
    appInfo: ApplicationInfo? = null
): ImmutableConfig {
//    val errorTypes = when {
//        config.autoDetectErrors -> config.enabledErrorTypes.copy()
//        else -> ErrorTypes(false)
//    }

    return ImmutableConfig(
        discardClasses = config.discardClasses.toSet(),
        enabledReleaseStages = config.enabledReleaseStages?.toSet(),
        projectPackages = config.projectPackages.toSet(),
        releaseStage = config.releaseStage,
        appVersion = config.appVersion,
        versionCode = config.versionCode,
        logger = config.logger!!,
        maxBreadcrumbs = config.maxBreadcrumbs,
        maxPersistedEvents = config.maxPersistedEvents,
        enabledBreadcrumbTypes = config.enabledBreadcrumbTypes?.toSet(),
        packageInfo = packageInfo,
        appInfo = appInfo,
    )
}
internal fun sanitiseConfiguration(
    appContext: Context,
    configuration: Configuration): ImmutableConfig {
    val packageName = appContext.packageName
    val packageManager = appContext.packageManager
    val packageInfo = runCatching { packageManager.getPackageInfo(packageName, 0) }.getOrNull()
    val appInfo = runCatching {
        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    }.getOrNull()

    // populate releaseStage
    if (configuration.releaseStage == null) {
        configuration.releaseStage = when {
            appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) -> RELEASE_STAGE_DEVELOPMENT
            else -> RELEASE_STAGE_PRODUCTION
        }
    }

    // if the user has set the releaseStage to production manually, disable logging
    if (configuration.logger == null || configuration.logger == DebugLogger) {
        val releaseStage = configuration.releaseStage
        val loggingEnabled = RELEASE_STAGE_PRODUCTION != releaseStage

        if (loggingEnabled) {
            configuration.logger = DebugLogger
        } else {
            configuration.logger = NoopLogger
        }
    }

    if (configuration.versionCode == null || configuration.versionCode == 0) {
        configuration.versionCode =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            packageInfo?.longVersionCode?.toInt() ?: 0
        } else packageInfo?.versionCode
    }

    // Set sensible defaults if project packages not already set
    if (configuration.projectPackages.isEmpty()) {
        configuration.projectPackages = setOf<String>(packageName)
    }

    return convertToImmutableConfig(
        configuration,
        packageInfo,
        appInfo
    )
}


internal const val RELEASE_STAGE_DEVELOPMENT = "development"
internal const val RELEASE_STAGE_PRODUCTION = "production"
