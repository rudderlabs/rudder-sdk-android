/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
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

package com.rudderstack.rudderjsonadapter

/**
 * A default common adapter for different json parsers.
 * A concrete implementation is available as different adapters for
 * Gson, Jackson and Moshi.
 * For custom serialization and deserialization type adapters need to be provided
 * in each separate implementation module of JsonAdapter.
 *
 */
interface JsonAdapter {
    /**
     * Deserialize json as generic Parameterized class
     *
     * @param T The type of Generic Class. Adapters will try to parse given json into an object of type T
     * @param json The json string
     * @param typeAdapter A RudderTypeAdapter object with T as type. For details
     * @see RudderTypeAdapter
     * @return object of type T or null if parsing is unsuccessful
     */
    fun <T> readJson(json: String, typeAdapter: RudderTypeAdapter<T>): T?

    /**
     * Serialize an object to json
     *
     * @param T The type of the given object
     * @param obj The object that is to be converted
     * @return the serialized json
     */
    fun <T : Any> writeToJson(obj: T): String?

    /**
     * Serialize parameterized objects to json
     *
     * @param T The type of obj
     * @param obj The object to serialize
     * @param typeAdapter A RudderTypeAdapter object with Type T
     * @return The serialized json
     */
    fun <T : Any> writeToJson(obj: T, typeAdapter: RudderTypeAdapter<T>?): String?

    /**
     * Deserialize json into an object of type T
     *
     * @param T the type of object to deserialize to
     * @param json The input json
     * @param resultClass The class of object to deserialize to
     * @return The deserialized object or null if serialization fails
     */
    fun <T : Any> readJson(json: String, resultClass: Class<T>): T?

    /**
     * Convert a map to object
     *
     * @param T The type of class to convert to
     * @param map A map that contains proper keys matching T
     * @param resultClass The type of object expected
     * @return An object of type T if conversion is successful
     */
    fun <T : Any> readMap(map: Map<String, Any>, resultClass: Class<T>): T?
}
