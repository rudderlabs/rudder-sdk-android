/*
 * Creator: Debanjan Chatterjee on 14/06/23, 10:32 am Last modified: 13/06/23, 11:35 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.metrics

sealed interface Counter<T> {
    val name: String

    /**
     * Records a value.
     *
     *
     * @param value The increment amount. MUST be non-negative.
     */
    fun add(value: T)

    /**
     * Records a value with a set of attributes.
     *
     * @param value The increment amount. MUST be non-negative.
     * @param attributes A set of attributes to associate with the value.
     */
    fun add(value: T, attributes: Attributes?)

}

class LongCounter internal constructor(
    override val name: String,
    _aggregatorHandle: AggregatorHandler,
    _reservoir: Reservoir
) : Counter<Long> {
    private val aggregatorHandler = _aggregatorHandle
    private val reservoir = _reservoir
    override fun add(value: Long) {
        TODO("Not yet implemented")
    }

    override fun add(value: Long, attributes: Attributes?) {
        TODO("Not yet implemented")
    }

}