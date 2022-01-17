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

package com.rudderstack.android.core.internal.plugins

import com.rudderstack.android.core.DestinationConfig
import com.rudderstack.android.core.Plugin
import com.rudderstack.android.core.State
import com.rudderstack.android.core.Storage
import com.rudderstack.android.core.internal.states.DestinationConfigState
import com.rudderstack.android.models.Message

/**
 * Will be executed just before the device destination plugins.
 * Will store messages till all factories are ready
 * After that reiterate the messages to the plugins
 */
internal class WakeupActionPlugin(
    private val storage: Storage,
    private val destConfigState: State<DestinationConfig> = DestinationConfigState
) : Plugin {
    override fun intercept(chain: Plugin.Chain): Message {
        return if(destConfigState.value?.allIntegrationsReady != true || storage.startupQueue.isNotEmpty()){
            storage.saveStartupMessageInQueue(chain.message())
            chain.message()
        }else{
           chain.proceed(chain.message())
        }
    }

}