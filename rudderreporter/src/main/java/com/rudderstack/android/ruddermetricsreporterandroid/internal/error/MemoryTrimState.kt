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

import android.content.ComponentCallbacks2
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BaseObservable
import com.rudderstack.android.ruddermetricsreporterandroid.internal.StateEvent

internal class MemoryTrimState : BaseObservable() {
    var isLowMemory: Boolean = false
    var memoryTrimLevel: Int? = null

    val trimLevelDescription: String get() = descriptionFor(memoryTrimLevel)

    fun updateMemoryTrimLevel(newTrimLevel: Int?): Boolean {
        if (memoryTrimLevel == newTrimLevel) {
            return false
        }

        memoryTrimLevel = newTrimLevel
        return true
    }

    fun emitObservableEvent() {
        updateState {
            StateEvent.UpdateMemoryTrimEvent(
                isLowMemory,
                memoryTrimLevel,
                trimLevelDescription
            )
        }
    }

    private fun descriptionFor(memoryTrimLevel: Int?) = when (memoryTrimLevel) {
        null -> "None"
        ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "Complete"
        ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "Moderate"
        ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "Background"
        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI hidden"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "Running critical"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "Running low"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "Running moderate"
        else -> "Unknown ($memoryTrimLevel)"
    }
}
