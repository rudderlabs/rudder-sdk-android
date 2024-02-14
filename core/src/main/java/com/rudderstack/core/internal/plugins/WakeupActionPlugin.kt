/*
 * Creator: Debanjan Chatterjee on 05/01/22, 8:10 PM Last modified: 05/01/22, 8:10 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.core.internal.states.DestinationConfigState
import com.rudderstack.models.Message
import java.lang.ref.WeakReference

/**
 * Must be added prior to destination plugins.
 * Will store messages till all factories are ready
 * After that reiterate the messages to the plugins
 */
internal class WakeupActionPlugin(
//    private val destConfigState: State<DestinationConfig> = DestinationConfigState
) : Plugin {

    private var _analytics: WeakReference<Analytics?> = WeakReference(null)
    override fun setup(analytics: Analytics) {
        _analytics = WeakReference(analytics)
    }

    private val storage
        get() = _analytics.get()?.storage
    private val Analytics.destinationConfigState: DestinationConfigState?
        get() = retrieveState()

    override fun intercept(chain: Plugin.Chain): Message {
        val destinationConfig = _analytics.get()?.destinationConfigState?.value

        val forwardChain =
            if (destinationConfig == null || !destinationConfig.allIntegrationsReady || storage?.startupQueue?.isNotEmpty() == true) {
                storage?.saveStartupMessageInQueue(chain.message())
                //remove all destination plugins that are not ready, for others the message flow is normal
                val validPlugins = chain.plugins.toMutableList().filterNot {
                    it is DestinationPlugin<*> && destinationConfig?.isIntegrationReady(it.name) != true
                }
                chain.with(validPlugins)
            } else chain
        return forwardChain.proceed(chain.message())

    }

    override fun onShutDown() {
        _analytics = WeakReference(null)
    }
}