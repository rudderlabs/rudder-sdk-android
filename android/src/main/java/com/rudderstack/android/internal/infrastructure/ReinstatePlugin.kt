/*
 * Creator: Debanjan Chatterjee on 19/02/24, 12:50 pm Last modified: 19/02/24, 12:50 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.utilities.androidStorage
import com.rudderstack.android.utilities.contextState
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.android.utilities.initializeSessionManagement
import com.rudderstack.android.utilities.processNewContext
import com.rudderstack.android.utilities.setAnonymousId
import com.rudderstack.android.utilities.setUserId
import com.rudderstack.core.Analytics
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.models.createContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This plugin is used to reinstate the cache data in V2 SDK. In case no
 * cached data preset for v2, we check if the sourceId is present in the V1
 * cached server config, then the data is migrated to V2 SDK Servers the
 * following purposes: Reinstate anonymous id, or create new one if absent
 * Reinstate user id Initialise session management for the user Reinstate
 * traits and external ids
 */
internal class ReinstatePlugin : InfrastructurePlugin {
    private var _analytics: Analytics? = null
    private val _isReinstated = AtomicBoolean(false)
    private fun setReinstated(isReinstated: Boolean) {
        synchronized(_isReinstated) {
            _isReinstated.set(isReinstated)
            if (isReinstated) {
                _analytics?.applyInfrastructureClosure {
                    if (this is DataUploadService)
                        this.resume()
                }
            } else {
                _analytics?.applyInfrastructureClosure {
                    if (this is DataUploadService)
                        this.pause()
                }
            }
        }
    }

    override fun setup(analytics: Analytics) {
        _analytics = analytics
        setReinstated(false)
        reinstate()
    }

    private fun reinstate() {
        if (isV2DataAvailable()) {
            reinstateV2FromCache()
            setReinstated(true)
            return
        }
        migrateV1DataIfAvailable()
        if (_analytics?.currentConfigurationAndroid?.anonymousId == null) {
            _analytics?.currentConfigurationAndroid?.fillDefaults()
            setReinstated(true)
            return
        }
    }

    private fun ConfigurationAndroid.fillDefaults() {
        _analytics?.setAnonymousId(AndroidUtils.generateAnonymousId(collectDeviceId, application))
        _analytics?.initializeSessionManagement(
            _analytics?.androidStorage?.sessionId,
            _analytics?.androidStorage?.lastActiveTimestamp
        )
    }

    private fun reinstateV2FromCache() {
        val userId = _analytics?.androidStorage?.userId
        val anonId = _analytics?.androidStorage?.anonymousId
            ?: _analytics?.currentConfigurationAndroid?.let {
                AndroidUtils.generateAnonymousId(
                    it.collectDeviceId,
                    it.application
                )
            }
        val context = _analytics?.androidStorage?.context
        context?.let {
            _analytics?.processNewContext(context)
        }
        userId?.let {
            _analytics?.setUserId(it)
        }
        if (anonId != null)
            _analytics?.setAnonymousId(anonId)
        _analytics?.initializeSessionManagement(
            _analytics?.androidStorage?.sessionId, _analytics?.androidStorage?.lastActiveTimestamp
        )
    }

    private fun isV2DataAvailable(): Boolean {
        return !_analytics?.androidStorage?.anonymousId.isNullOrEmpty() ||
                !_analytics?.androidStorage?.userId.isNullOrEmpty() ||
                !_analytics?.contextState?.value.isNullOrEmpty()
    }

    private fun migrateV1DataIfAvailable() {
        // migrate user id/ anon id
        _analytics?.setUserIdFromV1()
        _analytics?.migrateAnonymousIdFromV1()
        _analytics?.migrateOptOutFromV1()
        _analytics?.migrateContextFromV1()
        _analytics?.migrateV1AdvertisingId()
        _analytics?.initializeSessionManagement(
            _analytics?.androidStorage?.v1SessionId,
            _analytics?.androidStorage?.v1LastActiveTimestamp
        )
        _analytics?.resetV1SessionValues()
        _analytics?.migrateV1LifecycleProperties()
        _analytics?.androidStorage?.migrateV1StorageToV2 {
            setReinstated(true)
        }
        _analytics?.androidStorage?.deleteV1SharedPreferencesFile()
        _analytics?.androidStorage?.deleteV1ConfigFiles()
    }

    private fun Analytics.migrateV1LifecycleProperties() {
        migrateV1Build()
        migrateV1Version()
    }

    private fun Analytics.setUserIdFromV1() {
        val traits = androidStorage.v1Traits
        val userId = traits?.get("userId") as? String ?: traits?.get("id") as? String
        if (userId.isNullOrEmpty() || !this.androidStorage.userId.isNullOrEmpty()) return
        _analytics?.setUserId(userId)
    }

    private fun Analytics.migrateAnonymousIdFromV1() {
        currentConfigurationAndroid?.apply {
            (androidStorage.v1AnonymousId
                ?: getV1AnonymousIdFromTraits()
                ?: AndroidUtils.generateAnonymousId(
                    collectDeviceId, application
                )).let { _analytics?.setAnonymousId(it) }
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

    private fun Analytics.migrateV1AdvertisingId() {
        androidStorage.v1AdvertisingId?.let {
            androidStorage.saveAdvertisingId(it)
        }
        androidStorage.resetV1AdvertisingId()
    }

    private fun Analytics.migrateOptOutFromV1() {
        val optOut = androidStorage.v1OptOut
        if (!optOut || this.androidStorage.isOptedOut) return //only relevant if optout is true
        _analytics?.optOut(true)
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

    override fun shutdown() {
        _analytics = null
    }

}
