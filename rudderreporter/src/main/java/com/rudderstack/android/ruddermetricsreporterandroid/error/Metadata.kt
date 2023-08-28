/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:31 pm Last modified: 05/06/23, 5:52 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

@file:Suppress("UNCHECKED_CAST")

package com.rudderstack.android.ruddermetricsreporterandroid.error

import com.rudderstack.android.ruddermetricsreporterandroid.JSerialize
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MetadataAware
import java.util.concurrent.ConcurrentHashMap

/**
 * A container for additional diagnostic information you'd like to send with
 * every error report.
 *
 * Diagnostic information is presented on your Bugsnag dashboard in tabs.
 */
data class Metadata @JvmOverloads constructor (
    internal val store: MutableMap<String, MutableMap<String, Any>> = ConcurrentHashMap()
) : MetadataAware{

    override fun addMetadata(section: String, value: Map<String, Any?>) {
        value.entries.forEach {
            addMetadata(section, it.key, it.value)
        }
    }

    override fun addMetadata(section: String, key: String, value: Any?) {
        if (value == null) {
            clearMetadata(section, key)
        } else {
            val tab = store[section] ?: ConcurrentHashMap()
            store[section] = tab
            insertValue(tab, key, value)
        }
    }

    private fun insertValue(map: MutableMap<String, Any>, key: String, newValue: Any) {
        var obj = newValue

        // only merge if both the existing and new value are maps
        val existingValue = map[key]
        if (existingValue != null && obj is Map<*, *>) {
            val maps = listOf(existingValue as Map<String, Any>, newValue as Map<String, Any>)
            obj = mergeMaps(maps)
        }
        map[key] = obj
    }

    override fun clearMetadata(section: String) {
        store.remove(section)
    }

    override fun clearMetadata(section: String, key: String) {
        val tab = store[section]
        tab?.remove(key)

        if (tab.isNullOrEmpty()) {
            store.remove(section)
        }
    }

    override fun getMetadata(section: String): Map<String, Any>? {
        return store[section]
    }

    override fun getMetadata(section: String, key: String): Any? {
        return getMetadata(section)?.get(key)
    }

    fun toMap(): MutableMap<String, MutableMap<String, Any>> {
        val copy = ConcurrentHashMap(store)

        // deep copy each section
        store.entries.forEach {
            copy[it.key] = ConcurrentHashMap(it.value)
        }
        return copy
    }

    companion object {
        @JvmStatic
        fun merge(vararg data: Metadata): Metadata {
            val stores = data.map { it.toMap() }
            return Metadata(mergeMaps(stores) as MutableMap<String, MutableMap<String, Any>>)
        }

        internal fun mergeMaps(data: List<Map<String, Any>>): MutableMap<String, Any> {
            val keys = data.flatMap { it.keys }.toSet()
            val result = ConcurrentHashMap<String, Any>()

            for (map in data) {
                for (key in keys) {
                    getMergeValue(result, key, map)
                }
            }
            return result
        }

        private fun getMergeValue(
            result: MutableMap<String, Any>,
            key: String,
            map: Map<String, Any>
        ) {
            val baseValue = result[key]
            val overridesValue = map[key]

            if (overridesValue != null) {
                if (baseValue is Map<*, *> && overridesValue is Map<*, *>) {
                    // Both original and overrides are Maps, go deeper
                    val first = baseValue as Map<String, Any>?
                    val second = overridesValue as Map<String, Any>?
                    result[key] = mergeMaps(listOf(first!!, second!!))
                } else {
                    result[key] = overridesValue
                }
            } else {
                if (baseValue != null) { // No collision, just use base value
                    result[key] = baseValue
                }
            }
        }
    }



//    fun trimMetadataStringsTo(maxStringLength: Int): TrimMetrics {
//        var stringCount = 0
//        var charCount = 0
//        store.forEach { entry ->
//            val stringAndCharCounts = StringUtils.trimStringValuesTo(
//                maxStringLength,
//                entry.value as MutableMap<String, Any?>
//            )
//
//            stringCount += stringAndCharCounts.itemsTrimmed
//            charCount += stringAndCharCounts.dataTrimmed
//        }
//        return TrimMetrics(stringCount, charCount)
//    }
}
