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

import android.content.Context
import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.androidStorage
import com.rudderstack.android.contextState
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.processNewContext
import com.rudderstack.android.setAnonymousId
import com.rudderstack.android.setUserId
import com.rudderstack.android.utilities.initializeSessionManagement
import com.rudderstack.android.utilities.isV1SavedServerConfigContainsSourceId
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.models.createContext
import com.rudderstack.models.traits
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This plugin is used to reinstate the cache data in V2 SDK.
 * In case no cached data preset for v2, we check if the sourceId is present in the V1 cached
 * server config, then the data is migrated to V2 SDK
 * Servers the following purposes:
 * Reinstate anonymous id, or create new one if absent
 * Reinstate user id
 * Initialise session management for the user
 * Reinstate traits and external ids
 */
private const val RUDDER_SERVER_FILE_NAME_V1 = "RudderServerConfig"

internal class ReinstatePlugin : InfrastructurePlugin {
    private var _analytics: Analytics? = null
    private var sourceId: String? = null
    private val _isReinstated = AtomicBoolean(false)
    private fun setReinstated(isReinstated: Boolean){
        synchronized(_isReinstated) {
            _isReinstated.set(isReinstated)
            if (isReinstated){
                _analytics?.applyInfrastructureClosure {
                    if(this is DataUploadService)
                        this.resume()
                }
            }else{
                _analytics?.applyInfrastructureClosure {
                    if(this is DataUploadService)
                        this.pause()
                }
            }
        }
    }

    override fun setup(analytics: Analytics) {
        _analytics = analytics
        setReinstated(false)
    }

    override fun updateRudderServerConfig(config: RudderServerConfig) {
        val sourceId = config.source?.sourceId ?: return
        this.sourceId = sourceId
        reinstate()
    }

    override fun updateConfiguration(configuration: Configuration) {
        reinstate()
    }

    private fun reinstate() {
        if (_isReinstated.get()) return
        synchronized(this) {
            val config = _analytics?.currentConfigurationAndroid ?: return
            if (isV2DataAvailable()) {
                setReinstated(true)
                reinstateV2FromCache()
                return
            }
            if (!config.shouldVerifySdk) {
                setReinstated(true)
                fillDefaults()
                return
            }
            val sourceId = this.sourceId ?: return

            migrateV1DataIfAvailable(config.application, sourceId, config)

            setReinstated(true)
        }
    }

    private fun fillDefaults() {
        _analytics?.setAnonymousId(AndroidUtils.getDeviceId())
        _analytics?.initializeSessionManagement(_analytics?.androidStorage?.sessionId,
            _analytics?.androidStorage?.lastActiveTimestamp)
    }

    private fun reinstateV2FromCache() {
        val userId = _analytics?.androidStorage?.userId
        val anonId = _analytics?.androidStorage?.anonymousId ?: AndroidUtils.getDeviceId()
        val context = _analytics?.androidStorage?.context
        context?.let {
            _analytics?.processNewContext(context)
        }
        userId?.let {
            _analytics?.setUserId(it)
        }
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

    private fun migrateV1DataIfAvailable(
        context: Context, sourceId: String, configurationAndroid: ConfigurationAndroid
    ) {
            val isV1DataAvailable =
                context.isV1SavedServerConfigContainsSourceId(RUDDER_SERVER_FILE_NAME_V1, sourceId)
            if(!isV1DataAvailable) return
            // migrate user id/ anon id
            _analytics?.setUserIdFromV1()
            _analytics?.migrateAnonymousIdFromV1()
            _analytics?.migrateOptOutFromV1()
                _analytics?.migrateContextFromV1()
            _analytics?.androidStorage?.migrateV1StorageToV2Sync()

            _analytics?.initializeSessionManagement(
                _analytics?.androidStorage?.v1SessionId, _analytics?.androidStorage?.v1LastActiveTimestamp
            )
    }

    private fun Analytics.setUserIdFromV1() {
        val traits = androidStorage.v1Traits
        val userId = traits?.get("userId") as? String ?: traits?.get("id") as? String
        if (userId.isNullOrEmpty() || !this.androidStorage.userId.isNullOrEmpty()) return
        _analytics?.setUserId(userId)
    }

    private fun Analytics.migrateAnonymousIdFromV1() {
        (androidStorage.v1AnonymousId?:AndroidUtils.getDeviceId()).let { _analytics?.setAnonymousId(it) }
        androidStorage.resetV1AnonymousId()
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

    private fun shouldMigrateContext(): Boolean {
        val context = _analytics?.contextState?.value ?: return false
        return context.isEmpty() || context.traits.isNullOrEmpty()
    }

    override fun shutdown() {
        _analytics = null
        this.sourceId = null
    }

}
