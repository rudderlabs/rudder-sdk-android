package com.rudderstack.android.internal.infrastructure

import android.content.pm.PackageManager
import android.os.Build
import com.rudderstack.android.utilities.androidStorage
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.models.AppVersion
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin

private const val PREVIOUS_VERSION = "previous_version"
private const val PREVIOUS_BUILD = "previous_build"
private const val VERSION = "version"
private const val BUILD = "build"

private const val EVENT_NAME_APPLICATION_INSTALLED = "Application Installed"
private const val EVENT_NAME_APPLICATION_UPDATED = "Application Updated"

private const val DEFAULT_BUILD = -1
private const val DEFAULT_VERSION_NAME = ""

class AppInstallUpdateTrackerPlugin : InfrastructurePlugin {

    private var analytics: Analytics? = null
    private lateinit var appVersion: AppVersion

    override fun setup(analytics: Analytics) {
        this.analytics = analytics
        this.appVersion = getAppVersion(analytics)
        storeVersionNameAndBuild(analytics.androidStorage)
        if (this.analytics?.currentConfigurationAndroid?.trackLifecycleEvents == true) {
            trackApplicationStatus()
        }
    }

    private fun getAppVersion(analytics: Analytics): AppVersion {
        val previousBuild: Int? = analytics.androidStorage.build
        val previousVersionName: String? = analytics.androidStorage.versionName
        var currentBuild: Int? = null
        var currentVersionName: String? = null

        try {
            val packageName = analytics.currentConfigurationAndroid?.application?.packageName
            val packageManager: PackageManager? = analytics.currentConfigurationAndroid?.application?.packageManager
            val packageInfo = packageName?.let {
                packageManager?.getPackageInfo(it, 0)
            }

            currentVersionName = packageInfo?.versionName
            currentBuild = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toInt()
            } else {
                packageInfo?.versionCode
            }
        } catch (ex: PackageManager.NameNotFoundException) {
            analytics.logger.error(log = "Failed to get app version info: ${ex.message}")
        }

        return AppVersion(
            previousBuild = previousBuild ?: DEFAULT_BUILD,
            previousVersionName = previousVersionName ?: DEFAULT_VERSION_NAME,
            currentBuild = currentBuild ?: DEFAULT_BUILD,
            currentVersionName = currentVersionName ?: DEFAULT_VERSION_NAME,
        )
    }

    private fun storeVersionNameAndBuild(analyticsStorage: AndroidStorage) {
        analyticsStorage.setVersionName(this.appVersion.currentVersionName)
        analyticsStorage.setBuild(this.appVersion.currentBuild)
    }

    private fun trackApplicationStatus() {
        if (this.isApplicationInstalled()) {
            sendApplicationInstalledEvent()
        } else if (this.isApplicationUpdated()) {
            sendApplicationUpdatedEvent()
        }
    }

    private fun isApplicationInstalled(): Boolean {
        return this.appVersion.previousBuild == -1
    }

    private fun isApplicationUpdated(): Boolean {
        return this.appVersion.previousBuild != -1 && this.appVersion.previousBuild != this.appVersion.currentBuild
    }

    private fun sendApplicationInstalledEvent() {
        this.analytics?.logger?.debug(log = "Tracking Application Installed event")
        val trackProperties = mutableMapOf<String, Any>()
        trackProperties[VERSION] = this.appVersion.currentVersionName
        trackProperties[BUILD] = this.appVersion.currentBuild

        sendEvent(EVENT_NAME_APPLICATION_INSTALLED, trackProperties)
    }

    private fun sendApplicationUpdatedEvent() {
        this.analytics?.logger?.debug(log = "Tracking Application Updated event")
        val trackProperties = mutableMapOf<String, Any>()
        trackProperties[PREVIOUS_VERSION] = this.appVersion.previousVersionName
        trackProperties[PREVIOUS_BUILD] = this.appVersion.previousBuild
        trackProperties[VERSION] = this.appVersion.currentVersionName
        trackProperties[BUILD] = this.appVersion.currentBuild

        sendEvent(EVENT_NAME_APPLICATION_UPDATED, trackProperties)
    }

    private fun sendEvent(eventName: String, properties: Map<String, Any>) {
        analytics?.track {
            event(eventName)
            trackProperties {
                add(properties)
            }
        }
    }

    override fun shutdown() {
        analytics = null
    }
}
