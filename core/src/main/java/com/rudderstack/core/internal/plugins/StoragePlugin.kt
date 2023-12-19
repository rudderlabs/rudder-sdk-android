/*
 * Creator: Debanjan Chatterjee on 28/12/21, 8:17 PM Last modified: 28/12/21, 8:17 PM
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

package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Plugin
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.models.Message

/**
 * Adds [Message] to repository for further processing.
 * Used for cloud mode destinations.
 * Saves user related data in case of identify messages and
 *
 */
internal class StoragePlugin : Plugin {
    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()
        ConfigurationsState.value?.storage?.saveMessage(message.copy())

        return chain.proceed(message)
    }
}