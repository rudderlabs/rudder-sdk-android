/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:30 pm Last modified: 05/06/23, 5:52 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal.error

import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import com.rudderstack.android.ruddermetricsreporterandroid.error.Breadcrumb
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BaseObservable
import com.rudderstack.android.ruddermetricsreporterandroid.internal.StateEvent
import java.util.concurrent.atomic.AtomicInteger

/**
 * Stores breadcrumbs added to the [Client] in a ring buffer. If the number of breadcrumbs exceeds
 * the maximum configured limit then the oldest breadcrumb in the ring buffer will be overwritten.
 *
 * When the breadcrumbs are required for generation of an event a [List] is constructed and
 * breadcrumbs added in the order of their addition.
 */
internal class BreadcrumbState(
    private val maxBreadcrumbs: Int,
//    private val callbackState: CallbackState,
    private val logger: Logger
) : BaseObservable() {

    /*
     * We use the `index` as both a pointer to the tail of our ring-buffer, and also as "cheat"
     * semaphore. When the ring-buffer is being copied - the index is set to a negative number,
     * which is an invalid array-index. By masking the `expected` value in a `compareAndSet` with
     * `validIndexMask`: the CAS operation will only succeed if it wouldn't interrupt a concurrent
     * `copy()` call.
     */
    private val validIndexMask: Int = Int.MAX_VALUE

    private val store = arrayOfNulls<Breadcrumb?>(maxBreadcrumbs)
    private val index = AtomicInteger(0)

    fun add(breadcrumb: Breadcrumb) {
        if (maxBreadcrumbs == 0 /*|| !callbackState.runOnBreadcrumbTasks(breadcrumb, logger)*/) {
            return
        }

        // store the breadcrumb in the ring buffer
        val position = getBreadcrumbIndex()
        store[position] = breadcrumb

        updateState {
            // use direct field access to avoid overhead of accessor method
            StateEvent.AddBreadcrumb(
                breadcrumb.name,
                breadcrumb.type,
                // an encoding of milliseconds since the epoch
                "t${breadcrumb.timestamp.time}",
                breadcrumb.metadata ?: mutableMapOf()
            )
        }
    }

    /**
     * Retrieves the index in the ring buffer where the breadcrumb should be stored.
     */
    private fun getBreadcrumbIndex(): Int {
        while (true) {
            val currentValue = index.get() and validIndexMask
            val nextValue = (currentValue + 1) % maxBreadcrumbs
            if (index.compareAndSet(currentValue, nextValue)) {
                return currentValue
            }
        }
    }

    /**
     * Creates a copy of the breadcrumbs in the order of their addition.
     */
    fun copy(): List<Breadcrumb> {
        if (maxBreadcrumbs == 0) {
            return emptyList()
        }

        // Set a negative value that stops any other thread from adding a breadcrumb.
        // This handles reentrancy by waiting here until the old value has been reset.
        var tail = -1
        while (tail == -1) {
            tail = index.getAndSet(-1)
        }

        try {
            val result = arrayOfNulls<Breadcrumb>(maxBreadcrumbs)
            store.copyInto(result, 0, tail, maxBreadcrumbs)
            store.copyInto(result, maxBreadcrumbs - tail, 0, tail)
            return result.filterNotNull()
        } finally {
            index.set(tail)
        }
    }


}
