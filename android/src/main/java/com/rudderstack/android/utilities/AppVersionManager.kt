package com.rudderstack.android.utilities

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Logger

internal class AppVersionManager(
    private val application: Application?,
    private val analyticsStorage: AndroidStorage,
    logger: Logger,
) {

    private val previousBuild: Int? = analyticsStorage.build
    private val previousVersionName: String? = analyticsStorage.versionName
    private var currentBuild: Int? = null
    private var currentVersionName: String? = null

    private val packageInfo: PackageInfo?
        get() {
            val packageName = application?.packageName
            val packageManager: PackageManager? = application?.packageManager
            return packageName?.let { packageManager?.getPackageInfo(it, 0) }
        }

    init {
        try {
            currentVersionName = packageInfo?.versionName
            currentBuild = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toInt()
            } else {
                packageInfo?.versionCode
            }
        } catch (ex: NameNotFoundException) {
            logger.error(log = "Failed to get app version info: ${ex.message}")
        }
    }

    fun getAppVersionInfo(): AppVersion {
        return AppVersion(
            previousBuild = previousBuild ?: AppVersion.DEFAULT_BUILD,
            previousVersionName = previousVersionName ?: AppVersion.DEFAULT_VERSION_NAME,
            currentBuild = currentBuild ?: AppVersion.DEFAULT_BUILD,
            currentVersionName = currentVersionName ?: AppVersion.DEFAULT_VERSION_NAME,
        )
    }

    fun updateAppVersionInStorage() {
        currentVersionName?.let { analyticsStorage.setVersionName(it) }
        currentBuild?.let { analyticsStorage.setBuild(it) }
    }
}
