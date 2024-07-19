/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:30 pm Last modified: 05/06/23, 5:52 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.ruddermetricsreporterandroid.internal.error

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import com.rudderstack.android.ruddermetricsreporterandroid.error.BreadcrumbType
import com.rudderstack.android.ruddermetricsreporterandroid.error.CrashFilter
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DebugLogger
import com.rudderstack.android.ruddermetricsreporterandroid.internal.NoopLogger

data class ImmutableConfig(
    val libraryMetadata: LibraryMetadata,
    val projectPackages: Collection<String>,
    val enabledBreadcrumbTypes: Set<BreadcrumbType>?,
    val discardClasses: Collection<String>,
    val crashFilter: CrashFilter?,
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
        return shouldDiscardByReleaseStage() || shouldDiscardByErrorClass(exc) ||
            shouldDiscardByCrashFilter(exc)
    }

    private fun shouldDiscardByCrashFilter(exc: Throwable): Boolean {
        return crashFilter?.shouldKeep(exc) == false
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
    appInfo: ApplicationInfo? = null,
): ImmutableConfig {
    return ImmutableConfig(
        libraryMetadata = config.libraryMetadata,
        discardClasses = config.discardClasses.toSet(),
        enabledReleaseStages = config.enabledReleaseStages?.toSet(),
        projectPackages = config.projectPackages.toSet(),
        releaseStage = config.releaseStage,
        logger = config.logger!!,
        maxBreadcrumbs = config.maxBreadcrumbs,
        maxPersistedEvents = config.maxPersistedEvents,
        enabledBreadcrumbTypes = config.enabledBreadcrumbTypes?.toSet(),
        packageInfo = packageInfo,
        appInfo = appInfo,
        crashFilter = config.crashFilter,
    )
}
internal fun sanitiseConfiguration(
    appContext: Context,
    configuration: Configuration,
): ImmutableConfig {
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

    if (configuration.libraryMetadata.versionCode.isEmpty() || configuration.libraryMetadata.versionCode == "0") {
        configuration.libraryMetadata = configuration.libraryMetadata.copy(
            versionCode = (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    @Suppress("DEPRECATION")
                    packageInfo?.longVersionCode?.toInt() ?: 0
                } else {
                    packageInfo?.versionCode
                }
                ).toString(),
        )
    }

    // Set sensible defaults if project packages not already set
    if (configuration.projectPackages.isEmpty()) {
        configuration.projectPackages = setOf<String>(packageName)
    }

    return convertToImmutableConfig(
        configuration,
        packageInfo,
        appInfo,
    )
}

internal const val RELEASE_STAGE_DEVELOPMENT = "development"
internal const val RELEASE_STAGE_PRODUCTION = "production"
