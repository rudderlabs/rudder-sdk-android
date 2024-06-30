/*
 * Creator: Debanjan Chatterjee on 28/12/21, 4:32 PM Last modified: 28/12/21, 4:25 PM
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

package com.rudderstack.core

private const val TYPE = "type"
private const val ID = "id"

/**
 * Can be set as global or customised options for each message.
 * If no customised option is set for a message, global options will be used.
 * Users can pass any object as values, but in case of complex classes, please check with the
 * serializer/deserializer adapter being used
 * @property externalIds External ids can be used for assigning extra ids for different destinations
 * on the transformer side.
 * @property integrations The integrations to which these messages will be delivered. If empty, the
 * message will be delivered to all destinations added to Analytics.
 * @property customContexts Custom context elements that are going to be sent with message
 */
class RudderOption {
    val integrations: Map<String, Boolean>
        get() = _integrations
    private val _integrations = mutableMapOf<String, Boolean>()
    val customContexts: Map<String, Any>
        get() = _customContexts
    private val _customContexts = mutableMapOf<String, Any>()

    val externalIds: List<Map<String, String>>
        get() = _externalIds
    private val _externalIds = mutableListOf<Map<String, String>>()

    fun putExternalId(type: String, id: String): RudderOption {
        val existingExternalIdIndex =
            _externalIds.indexOfFirst { it[TYPE]?.equals(type, ignoreCase = true) == true }

        if (existingExternalIdIndex != -1) {
            // If the type exists, update the id
            _externalIds[existingExternalIdIndex] =
                _externalIds[existingExternalIdIndex].toMutableMap().apply {
                    this[ID] = id
                }
        } else {
            // If the type does not exist, add a new externalId
            _externalIds.add(
                mapOf(
                    TYPE to type,
                    ID to id
                )
            )
        }
        return this
    }

    fun putIntegration(destinationKey: String, enabled: Boolean): RudderOption {
        _integrations[destinationKey] = enabled
        return this
    }

    fun putIntegration(destination: BaseDestinationPlugin<*>, enabled: Boolean): RudderOption {
        _integrations[destination.name] = enabled
        return this
    }

    fun putCustomContext(key: String, context: Map<String?, Any?>): RudderOption {
        _customContexts[key] = context
        return this
    }


    override fun equals(other: Any?): Boolean {
        return other is RudderOption &&
                other.externalIds == this.externalIds &&
                other.integrations == this.integrations &&
                other.customContexts == this.customContexts
    }

    override fun hashCode(): Int {
        return externalIds.hashCode() + integrations.hashCode() + customContexts.hashCode()
    }

}
