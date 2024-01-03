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

import com.rudderstack.android.ruddermetricsreporterandroid.error.Metadata
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BaseObservable
import com.rudderstack.android.ruddermetricsreporterandroid.internal.StateEvent

internal data class MetadataState(val metadata: Metadata = Metadata()) :
    BaseObservable(),
    MetadataAware {

    override fun addMetadata(section: String, value: Map<String, Any?>) {
        metadata.addMetadata(section, value)
        notifyMetadataAdded(section, value)
    }

    override fun addMetadata(section: String, key: String, value: Any?) {
        metadata.addMetadata(section, key, value)
        notifyMetadataAdded(section, key, value)
    }

    override fun clearMetadata(section: String) {
        metadata.clearMetadata(section)
        notifyClear(section, null)
    }

    override fun clearMetadata(section: String, key: String) {
        metadata.clearMetadata(section, key)
        notifyClear(section, key)
    }

    private fun notifyClear(section: String, key: String?) {
        when (key) {
            null -> updateState { StateEvent.ClearMetadataSection(section) }
            else -> updateState { StateEvent.ClearMetadataValue(section, key) }
        }
    }

    override fun getMetadata(section: String) = metadata.getMetadata(section)
    override fun getMetadata(section: String, key: String) = metadata.getMetadata(section, key)

    /**
     * Fires the initial observable messages for all the metadata which has been added before an
     * Observer was added. This is used initially to populate the NDK with data.
     */
    fun emitObservableEvent() {
        val sections = metadata.store.keys

        for (section in sections) {
            val data = metadata.getMetadata(section)

            data?.entries?.forEach {
                notifyMetadataAdded(section, it.key, it.value)
            }
        }
    }

    private fun notifyMetadataAdded(section: String, key: String, value: Any?) {
        when (value) {
            null -> notifyClear(section, key)
            else -> updateState {
                StateEvent.AddMetadata(
                    section,
                    key,
                    metadata.getMetadata(section, key),
                )
            }
        }
    }

    private fun notifyMetadataAdded(section: String, value: Map<String, Any?>) {
        value.entries.forEach {
            updateState {
                StateEvent.AddMetadata(
                    section,
                    it.key,
                    metadata.getMetadata(section, it.key),
                )
            }
        }
    }
}
