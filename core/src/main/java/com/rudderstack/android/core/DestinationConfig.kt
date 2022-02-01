/*
 * Creator: Debanjan Chatterjee on 13/01/22, 3:55 PM Last modified: 13/01/22, 3:55 PM
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

package com.rudderstack.android.core

/**
 * Stores device mode destinations state.
 * -- configurations used to activate sending data to destination sdks
 * Is not applicable for cloud mode.
 * Generally device mode destination sdks initialize asynchronously and sending data to those
 * should be halted till these sdks are ready.
 * Do not confuse this with sdk "enabled" This is for keeping track of initialization of
 * enabled destinations
 * @property integrationMap Denotes the integration along with it's integration state.
 */

internal class DestinationConfig(private val integrationMap: Map<String, Boolean> = hashMapOf()) {
    /**
     * Assigns a particular integration to it's state.
     * In case the integration is not present in the map, it's added along with it's state.
     *
     * @param integration The name of the integration. This should be same as [DestinationPlugin.name]
     * @param initialized true if the integration is initialized, false otherwise
     * @return A new [DestinationConfig] object
     */
    fun withIntegration(integration: String, initialized: Boolean = false): DestinationConfig {
        return withIntegrations(mapOf(integration to initialized))
    }

    /**
     * @see withIntegration
     *
     * @param integrationsMap Add new states of integrations
     * @return A new [DestinationConfig] object with updated integrations
     */
    fun withIntegrations(integrationsMap: Map<String, Boolean>): DestinationConfig {
        return copy(integrationMap + integrationsMap)
    }

    /**
     * Checks if the integration is ready.
     *
     * @param integration The name of the integration provided in [DestinationPlugin.name]
     * @return true if ready else false
     */
    fun isIntegrationReady(integration: String): Boolean {
        return integrationMap.getOrDefault(integration, false)
    }

    /**
     * Get names of integrations which are ready
     *
     * @return List of names of the integrations, matching to [DestinationPlugin.name]
     */
    fun getReadyIntegrations(): List<String> {
        return integrationMap.filter {
            it.value
        }.mapTo(ArrayList<String>(integrationMap.size)) {
            it.key
        }
    }

    /**
     * Check if all integrations are ready.
     */
    val allIntegrationsReady: Boolean
        get() = integrationMap.values.filter { !it }.isNullOrEmpty()

    private fun copy(integrationMap: Map<String, Boolean>) = DestinationConfig(integrationMap)

    /**
     * Remove the destination from destination config, so it won't be accounted for.
     *
     * @param plugin The name of the [DestinationPlugin]
     */
    fun removeIntegration(plugin: String): DestinationConfig {
        return copy(integrationMap.toMutableMap().apply { remove(plugin) })
    }

}