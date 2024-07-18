package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.utilities.androidStorage
import com.rudderstack.android.utilities.contextState
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.android.utilities.initializeSessionManagement
import com.rudderstack.android.utilities.processNewContext
import com.rudderstack.android.utilities.setAnonymousId
import com.rudderstack.core.Analytics
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.models.createContext

/**
 * This plugin is used to reinstate the cache data in V2 SDK. In case no
 * cached data preset for v2, we check if the sourceId is present in the V1
 * cached server config, then the data is migrated to V2 SDK Servers the
 * following purposes: Reinstate anonymous id, or create new one if absent
 * Reinstate user id Initialise session management for the user Reinstate
 * traits and external ids
 */
internal class ReinstatePlugin : InfrastructurePlugin {

    override lateinit var analytics: Analytics

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        setReinstated(false)
        reinstate()
    }

    private fun setReinstated(isReinstated: Boolean) {
        synchronized(this) {
            if (isReinstated) {
                analytics.applyInfrastructureClosure {
                    if (this is DataUploadService)
                        this.resume()
                }
            } else {
                analytics.applyInfrastructureClosure {
                    if (this is DataUploadService)
                        this.pause()
                }
            }
        }
    }

    private fun reinstate() {
        if (isV2DataAvailable()) {
            reinstateV2FromCache()
            setReinstated(true)
            return
        }
        migrateV1DataIfAvailable()
        if (analytics.currentConfigurationAndroid?.anonymousId == null) {
            analytics.currentConfigurationAndroid?.fillDefaults()
            setReinstated(true)
            return
        }
    }

    private fun ConfigurationAndroid.fillDefaults() {
        analytics?.setAnonymousId(AndroidUtils.generateAnonymousId(collectDeviceId, application))
        analytics?.initializeSessionManagement(
            analytics?.androidStorage?.sessionId,
            analytics?.androidStorage?.lastActiveTimestamp
        )
    }

    private fun reinstateV2FromCache() {
        val anonId = analytics.androidStorage.anonymousId ?: analytics.currentConfigurationAndroid?.let {
            AndroidUtils.generateAnonymousId(
                it.collectDeviceId,
                it.application
            )
        }
        val context = analytics.androidStorage.context
        context?.let {
            analytics.processNewContext(context)
        }
        if (anonId != null)
            analytics.setAnonymousId(anonId)
        analytics.initializeSessionManagement(
            analytics.androidStorage.sessionId, analytics.androidStorage.lastActiveTimestamp
        )
    }

    private fun isV2DataAvailable(): Boolean {
        return !analytics.androidStorage.anonymousId.isNullOrEmpty() ||
                !analytics.androidStorage.userId.isNullOrEmpty() ||
                !analytics.contextState?.value.isNullOrEmpty()
    }

    private fun migrateV1DataIfAvailable() {
        // migrate user id/ anon id
        analytics.setUserIdFromV1()
        analytics.migrateAnonymousIdFromV1()
        analytics.migrateOptOutFromV1()
        analytics.migrateContextFromV1()
        //we do not store v1 advertising id
        analytics.androidStorage.resetV1AdvertisingId()
        analytics.migrateSession()
        analytics.migrateV1LifecycleProperties()
        analytics.androidStorage.migrateV1StorageToV2 {
            setReinstated(true)
        }
        analytics.androidStorage.deleteV1SharedPreferencesFile()
        analytics.androidStorage.deleteV1ConfigFiles()
    }

    private fun Analytics.migrateSession() {
        initializeSessionManagement(
            androidStorage.v1SessionId,
            androidStorage.v1LastActiveTimestamp
        )
        resetV1SessionValues()
    }

    private fun Analytics.migrateV1LifecycleProperties() {
        migrateV1Build()
        migrateV1Version()
    }

    private fun Analytics.setUserIdFromV1() {
        val traits = androidStorage.v1Traits
        val userId = traits?.get("userId") as? String ?: traits?.get("id") as? String
        if (userId.isNullOrEmpty() || !this.androidStorage.userId.isNullOrEmpty()) return
        androidStorage.setUserId(userId)
    }

    private fun Analytics.migrateAnonymousIdFromV1() {
        currentConfigurationAndroid?.apply {
            (androidStorage.v1AnonymousId
                ?: getV1AnonymousIdFromTraits()
                ?: AndroidUtils.generateAnonymousId(
                    collectDeviceId, application
                )).let {
                logger.error(log = "Unable to migrate anonymousId from V1. Generating new anonymousId")
                analytics.setAnonymousId(it)
            }
            androidStorage.resetV1AnonymousId()
        }
    }

    private fun Analytics.getV1AnonymousIdFromTraits(): String? {
        val traits = androidStorage.v1Traits
        return traits?.get("anonymousId") as? String
    }

    private fun Analytics.migrateV1Build() {
        androidStorage.v1Build?.let {
            androidStorage.setBuild(it)
        }
        androidStorage.resetV1Build()
    }

    private fun Analytics.resetV1SessionValues() {
        androidStorage.resetV1SessionId()
        androidStorage.resetV1SessionLastActiveTimestamp()
    }

    private fun Analytics.migrateV1Version() {
        androidStorage.v1VersionName?.let {
            androidStorage.setVersionName(it)
        }
        androidStorage.resetV1Version()
    }


    private fun Analytics.migrateOptOutFromV1() {
        val optOut = androidStorage.v1OptOut
        if (!optOut || this.androidStorage.isOptedOut) return //only relevant if optout is true
        analytics.optOut(true)
        androidStorage.resetV1OptOut()
    }

    private fun Analytics.migrateContextFromV1() {
        createContext(
            traits = androidStorage.v1Traits, externalIds = androidStorage.v1ExternalIds
        ).let {
            processNewContext(it)
            androidStorage.resetV1Traits()
            androidStorage.resetV1ExternalIds()
        }
    }
}
