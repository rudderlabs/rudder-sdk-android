/*
 * Creator: Debanjan Chatterjee on 05/01/22, 8:09 PM Last modified: 05/01/22, 8:09 PM
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

import com.rudderstack.android.core.Plugin
import com.rudderstack.android.models.Message

/**
 * Adds version of SDK to message
 *
 */
internal class VersionPlugin : Plugin {
    override fun intercept(chain: Plugin.Chain): Message {
        TODO("Not yet implemented")
    }
}