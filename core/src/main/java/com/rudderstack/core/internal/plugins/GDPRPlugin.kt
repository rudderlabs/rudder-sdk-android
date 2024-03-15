/*
 * Creator: Debanjan Chatterjee on 05/01/22, 8:08 PM Last modified: 05/01/22, 8:08 PM
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
import com.rudderstack.core.Plugin
import com.rudderstack.models.Message

/**
 * If opted out, msg won't go forward, will return from here.
 *
 */
internal class GDPRPlugin : Plugin {

    private var _analytics: Analytics? = null
    override fun intercept(chain: Plugin.Chain): Message {
        val isOptOut = _analytics?.storage?.isOptedOut ?: false
        return if (isOptOut) {
            chain.message()
        } else {
            chain.proceed(chain.message())
        }
    }

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
    }
}
