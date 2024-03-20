package com.rudderstack.android.internal.infrastructure

import android.app.Application
import com.rudderstack.android.androidStorage
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.utilities.AppVersion
import com.rudderstack.android.utilities.AppVersionManager
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.Logger

class AppInstallUpdateTrackerPlugin : InfrastructurePlugin {

    private var analytics: Analytics? = null
    private lateinit var appVersion: AppVersion

    companion object {
        private const val PREVIOUS_VERSION = "previous_version"
        private const val PREVIOUS_BUILD = "previous_build"
        private const val VERSION = "version"
        private const val BUILD = "build"

        const val EVENT_NAME_APPLICATION_INSTALLED = "Application Installed"
        const val EVENT_NAME_APPLICATION_UPDATED = "Application Updated"
    }

    override fun setup(analytics: Analytics) {
        this.analytics = analytics
        initialiseAppVersion(analytics.androidStorage, analytics.logger, analytics.currentConfigurationAndroid?.application)
        if (this.analytics?.currentConfigurationAndroid?.trackLifecycleEvents == true) {
            trackApplicationStatus()
        }
    }

    private fun initialiseAppVersion(analyticsStorage: AndroidStorage, logger: Logger, application: Application?) {
        val appVersionManager = AppVersionManager(application, analyticsStorage, logger)
        appVersionManager.updateAppVersionInStorage()
        this.appVersion = appVersionManager.getAppVersionInfo()
    }

    private fun trackApplicationStatus() {
        if (this.appVersion.isApplicationInstalled()) {
            sendApplicationInstalledEvent()
        } else if(this.appVersion.isApplicationUpdated()) {
            sendApplicationUpdatedEvent()
        }
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
