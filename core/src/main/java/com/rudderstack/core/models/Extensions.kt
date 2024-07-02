@file:JvmName("MessageUtils")

package com.rudderstack.core.models

/**
 * To be used to extract traits from context of [Message]
 */
val MessageContext.traits: Map<String, Any?>?
    get() = get(Constants.TRAITS_ID) as? Map<String, Any?>?

/**
 * To be used to extract external ids from context of [Message]
 */
val MessageContext.externalIds: List<Map<String, String>>?
    get() = get(Constants.EXTERNAL_ID) as? List<Map<String, String>>?

/**
 * To be used to extract custom contexts from context of [Message]
 */
val MessageContext.customContexts: Map<String, Any>?
    get() = get(Constants.CUSTOM_CONTEXT_MAP_ID) as? Map<String, Any>?

fun MessageContext.withExternalIdsRemoved() = this - Constants.EXTERNAL_ID

/**
 * Priority is given to this context and not to the operand.
 * FOr eg, if the calling context and operand both have same keys in their traits,
 * the value assigned to the key is of the calling context one.
 *
 * @param context
 * @return
 */
infix fun MessageContext?.optAddContext(context: MessageContext?): MessageContext? {
    //this gets priority
    if (this == null) return context
    else if (context == null) return this
    val newTraits = context.traits?.let {
        (it - (this.traits?.keys ?: setOf()).toSet()) optAdd this.traits
    } ?: traits
    val newCustomContexts = context.customContexts?.let {
        (it - (this.customContexts?.keys ?: setOf()).toSet()) optAdd this.customContexts
    } ?: customContexts
    val newExternalIds = context.externalIds?.let { savedExternalIds ->
        val currentExternalIds = this.externalIds ?: emptyList()
        val filteredSavedExternalIds = savedExternalIds.filter { savedExternalId ->
            currentExternalIds.none { currentExternalId ->
                currentExternalId["type"] == savedExternalId["type"]
            }
        }
        currentExternalIds + filteredSavedExternalIds
    } ?: externalIds

    createContext(newTraits, newExternalIds, newCustomContexts).let {
        //add the extra info from both contexts
        val extraOperandContext = context - it.keys
        val extraThisContext = this - it.keys
        return it + extraOperandContext + extraThisContext
    }

}

private infix fun <K, V> Iterable<Map<K, V>>.minusWrtKeys(
    operand:
    Iterable<Map<K, V>>
): List<Map<K, V>> {
    operand.toSet().let { op ->
        return this.filterNot {
            op inWrtKeys it
        }
    }
}

/**
 * infix function to match "in" condition for a map inside a list of maps based on just keys.
 * ```
 *  val list = listOf(
 *  mapOf("1" to 1),
 *  mapOf("2" to 3),
 *  mapOf("4" to 4)
 *  )
 *  list inWrtKeys mapOf("2", "2") //returns true
 * ```
 * @param item The item to be checked
 * @return true if the map with same keys are present, false otherwise
 */
infix fun <K, V> Iterable<Map<K, V>>.inWrtKeys(item: Map<K, V>): Boolean {
    this.toSet().forEach {
        if (it.keys.containsAll(item.keys))
            return true
    }
    return false
}
