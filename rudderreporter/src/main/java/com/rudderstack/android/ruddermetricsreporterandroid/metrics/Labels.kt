/*
 * Creator: Debanjan Chatterjee on 13/06/23, 7:32 pm Last modified: 13/06/23, 7:32 pm
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
 *//*


package com.rudderstack.android.ruddermetricsreporterandroid.metrics

class Labels private constructor(attributesMap: Map<String, String>) {
    private val _attributeMap = HashMap<String, String>()
    val data: Map<String, String>
        get() = _attributeMap

    companion object {
        fun of(vararg attributes: Pair<String, String>): Labels {
            return of(mapOf(*attributes))
        }

        fun of(attributesMap: Map<String, String>): Labels {
            return Labels(attributesMap)
        }
    }

    init {
        _attributeMap.putAll(attributesMap)
    }

    override fun equals(other: Any?): Boolean {
        return other is Labels && other._attributeMap == _attributeMap
    }

    override fun hashCode(): Int {
        return _attributeMap.hashCode()
    }

    override fun toString(): String {
        return "Labels{_attributeMap=$_attributeMap}"
    }
}*/
