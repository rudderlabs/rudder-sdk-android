/*
 * Creator: Debanjan Chatterjee on 23/12/21, 4:07 PM Last modified: 23/12/21, 4:07 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

import com.rudderstack.android.core.Plugin
import com.rudderstack.android.core.RudderOptions
import com.rudderstack.android.models.Message

/**
 * Alters flow and adds values to [Message] depending on options.
 * Manipulates the list of destination plugins based on options
 *
 *
 * @param options
 */
internal class RudderOptionPlugin(private val options: RudderOptions) : Plugin {
    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message()
//        msg.integrations
        return chain.proceed(msg)
    }
}