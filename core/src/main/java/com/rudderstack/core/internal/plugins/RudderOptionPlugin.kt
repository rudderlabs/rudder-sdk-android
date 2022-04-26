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

package com.rudderstack.core.internal.plugins

import com.rudderstack.models.*
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOptions
import com.rudderstack.core.internal.minusWrtKeys

/**
 * Alters flow and adds values to [Message] depending on options.
 * Manipulates the list of destination plugins based on options
 *
 * Individual plugin option takes precedence over "All"
 * For eg. If "All" is set to true, but a certain integration is set to false,
 * that integration will be dumped and not used.
 * Likewise if "All" is set to false but a particular integration is set to true, we use it.
 *
 * In case of [MessageContext] values of [Message.context] and [RudderOptions] are merged together.
 *
 * @param options
 */
internal class RudderOptionPlugin(private val options: RudderOptions) : Plugin {

    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message().let { oldMsg ->
            oldMsg.copy(
                context = oldMsg.updateContext(options),
            )
        }
        val validIntegrations = validIntegrations()
        msg.integrations = validIntegrations
//        val destinationPlugins = chain.plugins.filterIsInstance<DestinationPlugin<*>>()
//        val normalPlugins = chain.plugins - destinationPlugins
        return chain.plugins.takeIf {
            it.isNotEmpty()
        }?.let { plugins ->
            val validPlugins = filterWithAllowedDestinationPlugins(
                plugins,
                msg.integrations ?: mapOf()
            )
            return chain.with(validPlugins).proceed(msg)
        } ?: chain.proceed(msg)

    }

    /**
     * This filter approves any plugin that's either not a destination plugin, or is
     * an allowed destination
     *
     * @param plugins the plugin list to filter
     * @param integrations the map that contains relevant information
     * @return the valid [Plugin] list
     */
    private fun filterWithAllowedDestinationPlugins(
        plugins: List<Plugin>,
        integrations: Map<String, Boolean>
    ): List<Plugin> {
        return plugins.filter {
            it !is DestinationPlugin<*> || integrations.getOrDefault(
                it.name,
                integrations.getOrDefault("All", true)//if destination name is not present
            )
        }

    }

    private fun validIntegrations(): Map<String, Boolean> {
        return options.integrations.ifEmpty { mapOf("All" to true) }

    }

    private fun Message.updateContext(options: RudderOptions): MessageContext? {
        //external ids can be present in both message or options.
        //save concatenated external ids, if present with both message and options
        val messageExternalIds = context?.externalIds
        val updatedExternalIds: List<Map<String, String>>? =
            when {
                options.externalIds.isEmpty() -> {
                    messageExternalIds
                }
                messageExternalIds == null -> {
                    options.externalIds
                }
                else -> {//preference is given to external ids in Message
                    val extraIdsInOptions =
                        options.externalIds minusWrtKeys messageExternalIds
                    //adding these to messageExternalIds gives our required ids
                    messageExternalIds + extraIdsInOptions
                }
            }

        return createContext(context?.traits, updatedExternalIds, context?.customContexts)
    }
}