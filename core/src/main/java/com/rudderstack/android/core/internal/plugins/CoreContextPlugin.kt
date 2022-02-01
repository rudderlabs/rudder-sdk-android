/*
 * Creator: Debanjan Chatterjee on 21/01/22, 1:47 PM Last modified: 21/01/22, 1:47 PM
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

import com.rudderstack.android.core.Analytics
import com.rudderstack.android.core.Plugin
import com.rudderstack.android.models.Message


internal class CoreContextPlugin : Plugin {
    private lateinit var version : String

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        version = analytics.version
    }
    override fun intercept(chain: Plugin.Chain): Message {
        TODO("Not yet implemented")
    }

}