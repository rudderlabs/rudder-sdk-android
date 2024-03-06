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

package com.rudderstack.android.internal.plugins

import android.content.Context
import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.androidStorage
import com.rudderstack.android.applyConfigurationAndroid
import com.rudderstack.android.contextState
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.processNewContext
import com.rudderstack.android.setAnonymousId
import com.rudderstack.android.utilities.initializeSessionManagement
import com.rudderstack.android.utilities.isV1SavedServerConfigContainsSourceId
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.models.createContext
import com.rudderstack.models.traits
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean


private const val RUDDER_SERVER_FILE_NAME_V1 = "RudderServerConfig"

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
class ReinstatePlugin : Plugin {
    private var _analytics: Analytics? = null
    private var sourceId: String? = null
    private val isReinstated = AtomicBoolean(false)
    private val stackedChainsTillReinstate = LinkedBlockingQueue<Plugin.Chain>()
    override fun intercept(chain: Plugin.Chain): Message {
        if(!isReinstated.get()){
            stackedChainsTillReinstate.add(chain)
            return chain.message()
        }
        processStackedMessages()
        return chain.proceed(chain.message())
    }

    private fun processStackedMessages() {
        if(!isReinstated.get()) return
        synchronized(this) {
            while (stackedChainsTillReinstate.isNotEmpty()) {
                val stackedChain = stackedChainsTillReinstate.poll() ?: continue
                stackedChain.proceed(stackedChain.message())
            }
        }
    }

    override fun setup(analytics: Analytics) {
        _analytics = analytics
    }

    override fun updateRudderServerConfig(config: RudderServerConfig) {
        val sourceId = config.source?.sourceId ?: return
        this.sourceId = sourceId
        reinstate()
        processStackedMessages()
    }

    override fun updateConfiguration(configuration: Configuration) {
        reinstate()
    }

    private fun reinstate() {
        if (isReinstated.get()) return
        val config = _analytics?.currentConfigurationAndroid ?: return
        if (isV2DataAvailable()) {
            isReinstated.set(true)
            reinstateV2FromCache(config)
            return
        }
        if (!config.shouldVerifySdk){
            defaults(config)
            isReinstated.set(true)
            return
        }
        val sourceId = this.sourceId ?: return

        migrateV1DataIfAvailable(config.application, sourceId, config)
        isReinstated.set(true)
    }

    private fun defaults(configurationAndroid: ConfigurationAndroid) {
        _analytics?.setAnonymousId(AndroidUtils.getDeviceId(configurationAndroid.application))
        _analytics?.initializeSessionManagement(_analytics?.androidStorage?.sessionId,
            _analytics?.androidStorage?.lastActiveTimestamp)
    }

    private fun reinstateV2FromCache(configurationAndroid: ConfigurationAndroid) {
        val userId = _analytics?.androidStorage?.userId
        val anonId = _analytics?.androidStorage?.anonymousId ?: AndroidUtils.getDeviceId(
            configurationAndroid.application
        )
        val context = _analytics?.androidStorage?.context
        val optOut = _analytics?.androidStorage?.isOptedOut ?: false
        _analytics?.applyConfigurationAndroid {
            copy(
                anonymousId = anonId, isOptOut = optOut, userId = userId
            )
        }
        _analytics?.processNewContext(context ?: createContext())
        _analytics?.initializeSessionManagement(
            _analytics?.androidStorage?.sessionId, _analytics?.androidStorage?.lastActiveTimestamp
        )
    }

    private fun isV2DataAvailable(): Boolean {
        return !_analytics?.androidStorage?.anonymousId.isNullOrEmpty() ||
               !_analytics?.androidStorage?.userId.isNullOrEmpty() ||
               !_analytics?.contextState?.value.isNullOrEmpty()
    }

    override fun onShutDown() {
        _analytics = null
        this.sourceId = null
    }
    private fun migrateV1DataIfAvailable(
        context: Context, sourceId: String, configurationAndroid: ConfigurationAndroid
    ) {
        configurationAndroid.analyticsExecutor.execute {
            val isV1DataAvailable =
                context.isV1SavedServerConfigContainsSourceId(RUDDER_SERVER_FILE_NAME_V1, sourceId)
            if(!isV1DataAvailable) return@execute
            // migrate user id/ anon id
            _analytics?.setUserIdFromV1()
            _analytics?.migrateAnonymousIdFromV1()
            _analytics?.migrateOptOutFromV1()
            if (shouldMigrateContext()) {
                _analytics?.migrateContextFromV1()
            }

            _analytics?.initializeSessionManagement(
                _analytics?.androidStorage?.v1SessionId, _analytics?.androidStorage?.v1LastActiveTimestamp
            )
        }
    }

    private fun Analytics.setUserIdFromV1() {
        val traits = androidStorage.v1Traits
        val userId = traits?.get("userId") as? String ?: traits?.get("id") as? String
        if (userId.isNullOrEmpty() || !this.androidStorage.userId.isNullOrEmpty()) return
        _analytics?.identify(userId)
    }

    private fun Analytics.migrateAnonymousIdFromV1() {
        val anonymousId = androidStorage.v1AnonymousId
        if (anonymousId.isNullOrEmpty() || !this.androidStorage.anonymousId.isNullOrEmpty()) return
        _analytics?.setAnonymousId(anonymousId)
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

}