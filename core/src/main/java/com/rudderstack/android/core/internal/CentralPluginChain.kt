/*
 * Creator: Debanjan Chatterjee on 28/12/21, 5:16 PM Last modified: 28/12/21, 4:54 PM
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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.Plugin
import com.rudderstack.android.models.Message

/**
 * A concrete plugin chain that carries the entire plugin chain: all application
 * plugins, database plugins, and finally the cloud destination.
 *
 */
internal class CentralPluginChain(
    private val message: Message,
    private val plugins: List<Plugin>,
    private val index: Int
) : Plugin.Chain {
    private var numberOfCalls = 1
    override fun message(): Message {
        return message
    }

    override fun proceed(message: Message): Message {
        if(plugins.size <= index)
            return message
        // a chain can be proceeded just once
        check(numberOfCalls ++ < 2){
            "proceed cannot be called on same chain twice"
        }
        // Call the next interceptor in the chain.
        val next = copy(index = index + 1, message = message)
        val plugin = plugins[index]

       return plugin.intercept(next)

    }

    internal fun copy(
        message: Message = this.message,
        plugins: List<Plugin> = this.plugins,
        index: Int = this.index
    ) = CentralPluginChain(message, plugins, index)
}