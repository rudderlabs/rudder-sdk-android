/*
 * Creator: Debanjan Chatterjee on 24/01/22, 8:13 PM Last modified: 24/01/22, 8:13 PM
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

package com.rudderstack.core

import com.rudderstack.models.RudderServerConfig

/**
 * Download config for SDK.
 * Config aids in usage of device mode plugins.
 * Do not add this plugin to Analytics using [Analytics.addInfrastructurePlugin] method.
 * This [InfrastructurePlugin] should be sent as constructor params to [Analytics] instance.
 *
 */
interface ConfigDownloadService : InfrastructurePlugin {
    /**
     * Fetches the config from the server
     */
    fun download(
        callback: (success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit
    )

    /**
     * These listeners are attached with an optional replay argument.
     * replay specifies how many old events will be broadcasted to the listener.
     * Making it 0 will make the listener listen to only future downloads.
     *
     */
    fun addListener(listener: Listener, replay: Int)
    fun removeListener(listener: Listener)

    @FunctionalInterface
    fun interface Listener{
        fun onDownloaded(success: Boolean)
    }
}