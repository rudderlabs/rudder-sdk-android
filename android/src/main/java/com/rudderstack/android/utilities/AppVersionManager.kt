package com.rudderstack.android.utilities

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Logger
import com.rudderstack.models.AppVersion

internal class AppVersionManager(
    private val application: Application?,
    private val analyticsStorage: AndroidStorage,
    logger: Logger,
) {

    companion object {
        const val DEFAULT_VERSION_CODE = -1
        const val DEFAULT_VERSION_NAME = ""
    }

    private val previousVersionCode: Int? = analyticsStorage.versionCode
    private val previousVersionName: String? = analyticsStorage.versionName
    private var currentVersionCode: Int? = null
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
            currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
            previousVersionCode = previousVersionCode ?: DEFAULT_VERSION_CODE,
            previousVersionName = previousVersionName ?: DEFAULT_VERSION_NAME,
            currentVersionCode = currentVersionCode ?: DEFAULT_VERSION_CODE,
            currentVersionName = currentVersionName ?: DEFAULT_VERSION_NAME,
        )
    }

    fun updateAppVersionInStorage() {
        currentVersionName?.let { analyticsStorage.setVersionName(it) }
        currentVersionCode?.let { analyticsStorage.setVersionCode(it) }
    }
}
