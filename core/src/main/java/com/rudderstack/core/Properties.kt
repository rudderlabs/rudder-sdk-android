/*
 * Creator: Debanjan Chatterjee on 04/01/22, 11:39 AM Last modified: 04/01/22, 11:39 AM
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

import com.rudderstack.models.*

/**
 * Utility extensions for simplifying APIs

**/
/**
 * Can be used in [Analytics.track] as
 * ```kotlin
 *  Analytics.track("dummy_event", mapOf("some_property", value).addCurrency("dollars"))
 * ```
 *
 * @param value The currency used.
 */
fun Map<String, Any>.addCurrency(value : String) = this + Pair("currency", value)

/**
 * Can be used in [Analytics.track] as
 * ```kotlin
 *  Analytics.track("dummy_event", mapOf("some_property", value).addCurrency("dollars").addRevenue("500"))
 * ```
 *
 * @param value The revenue generated.
 */
fun Map<String, Any>.addRevenue(value : String) = this + Pair("revenue", value)

fun Map<String, Any>.addCategory(category : String) = this + ("category" to category)

/**
 * Used for adding advertisingId to context
 *
 * @param advertisingId The advertising id associated to the application
 */
fun Map<String, String>.putAdvertisingId(advertisingId : String) = this + ("advertisingId" to advertisingId)
fun Map<String, String>.putDeviceToken(advertisingId : String) = this + ("advertisingId" to advertisingId)



fun Map<String, Any?>.with( vararg keyPropertyPair: Pair<String, Any?>) = this + keyPropertyPair
