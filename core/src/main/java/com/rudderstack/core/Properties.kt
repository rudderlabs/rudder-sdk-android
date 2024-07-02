package com.rudderstack.core

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
fun Map<String, Any>.addCurrency(value: String) = this + Pair("currency", value)

/**
 * Can be used in [Analytics.track] as
 * ```kotlin
 *  Analytics.track("dummy_event", mapOf("some_property", value).addCurrency("dollars").addRevenue("500"))
 * ```
 *
 * @param value The revenue generated.
 */
fun Map<String, Any>.addRevenue(value: String) = this + Pair("revenue", value)

fun Map<String, Any>.addCategory(category: String) = this + ("category" to category)

/**
 * Used for adding advertisingId to context
 *
 * @param advertisingId The advertising id associated to the application
 */
fun Map<String, String>.putAdvertisingId(advertisingId: String) = this + ("advertisingId" to advertisingId)
fun Map<String, String>.putDeviceToken(advertisingId: String) = this + ("advertisingId" to advertisingId)


fun Map<String, Any?>.with(vararg keyPropertyPair: Pair<String, Any?>) = this + keyPropertyPair
