/*
 * Creator: Debanjan Chatterjee on 02/11/23, 6:26 pm Last modified: 02/11/23, 6:26 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.SnapshotCreator
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.util.UUID

internal class DefaultSnapshotCreator(private val libraryMetadata: LibraryMetadata,
                                      private val apiVersion: Int, private val jsonAdapter:
                                      JsonAdapter): SnapshotCreator {
    companion object {
        private const val SOURCE_KEY = "source"
        private const val METRICS_KEY = "metrics"
        private const val ERROR_KEY = "errors"
        private const val VERSION_KEY = "version"
        private const val MESSAGE_ID_KEY = "message_id"

    }
    override fun createSnapshot(metrics: List<MetricModel<out Number>>, errorEvents: List<String>):
            Snapshot? {
        val requestMap = HashMap<String, Any?>()
        requestMap[METRICS_KEY] = metrics
        if (errorEvents.isNotEmpty())
            requestMap[ERROR_KEY] = ErrorModel(libraryMetadata, errorEvents).toMap(jsonAdapter)
        requestMap[SOURCE_KEY] = libraryMetadata

        requestMap[VERSION_KEY] = apiVersion.toString()
        val messageId = UUID.randomUUID().toString()
        requestMap[MESSAGE_ID_KEY] = messageId
        return jsonAdapter.writeToJson(requestMap,
            object: RudderTypeAdapter<Map<String, Any?>>() {})?.let {
            Snapshot(messageId, it)
        }
    }
}