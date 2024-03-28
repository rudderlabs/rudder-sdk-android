/*
 * Creator: Debanjan Chatterjee on 08/01/24, 11:18 am Last modified: 08/01/24, 11:18 am
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

import com.rudderstack.android.contextState
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.processNewContext
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.models.MessageContext
import com.rudderstack.models.createContext
import com.rudderstack.models.customContexts
import com.rudderstack.models.externalIds
import com.rudderstack.models.traits
import com.rudderstack.models.updateWith

class ResetImplementationPlugin : InfrastructurePlugin {
    private var _analytics: Analytics? = null
    override fun setup(analytics: Analytics) {
        _analytics = analytics
    }
    private val contextState
    get() =_analytics?.contextState
    override fun reset() {

        _analytics?.processNewContext(contextState?.value?.updateWith(traits = mapOf(),
            externalIds = listOf()
        ) ?: createContext())
    }

    override fun shutdown() {
        //nothing to implement
    }

}