/*
 * Creator: Debanjan Chatterjee on 25/03/22, 10:39 PM Last modified: 25/03/22, 10:39 PM
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

package com.rudderstack.core.internal

/**
 * Operator to optionally add a nullable [Map] to callee
 * usage
 * val completeMap =  mapOf("userId" to userID) optAdd someNullableMap
 *
 * @param value The nullable [Map] object to "add"
 * @return The "sum" of both maps if [value] is not null, else this
 */
internal infix fun<K,V> Map<K,V>.optAdd(value : Map<K,V>?) : Map<K,V>{
    return value?.let {
        this + value
    }?: this
}

/**
 * Operator to perform a block on it, only if it's not null
 * Usage
 *
 * var someNullableMap : Map<String,String>? = null
 * ...
 * fun someMethod(){
 * someNullableMap ifNotNull storage::saveMap
 * }
 *
 * ...
 * fun saveMap(map: Map<String, String>){
 *  ...
 * }
 *
 *
 * @param T The generic callee object which will serve as parameter to [block]
 * @param R The return type expected from [block]
 * @param block A lambda function that takes type T as parameter
 * @return The result of [block] applied to "this"
 */
internal infix fun <T,R> T?.ifNotNull(block : (T) -> R) : R?{
    return this?.let(block)
}

/**
 * Applies minus operation to two iterable containing maps, with respect to keys
 * ```
 * val a = mapOf("1" to 1)
 * val b = mapOf("2" to 3)
 * val c = mapOf("4" to 4)
 * val d = mapOf("2" to 5)

 * val list1 = listOf(a,b,c)
 * val list2 = listOf(d)
 *
 * val diff = list1 minusWrtKeys list2 //[{1=1}, {4=4}]
 *
 * ```
 *
 * @param operand
 * @return
 */

internal infix fun<K, V> Iterable<Map<K,V>>.minusWrtKeys(operand :
                                                  Iterable<Map<K, V>>) : List<Map<K, V>>{
    operand.toSet().let { op->
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
internal infix fun<K, V> Iterable<Map<K,V>>.inWrtKeys(item : Map<K, V>) : Boolean{
    this.toSet().forEach {
        if(it.keys.containsAll(item.keys))
            return true
    }
    return false
}