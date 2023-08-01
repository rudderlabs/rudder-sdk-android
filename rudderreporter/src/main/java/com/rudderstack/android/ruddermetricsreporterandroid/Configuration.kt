/*
 * Creator: Debanjan Chatterjee on 26/06/23, 1:08 pm Last modified: 09/06/23, 5:32 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid

import android.content.Context
import com.rudderstack.android.ruddermetricsreporterandroid.error.BreadcrumbType
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DebugLogger
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MetadataState

class Configuration(var libraryMetadata: LibraryMetadata) {
    @JvmField
    internal val metadataState: MetadataState = MetadataState()

    var releaseStage: String? = null
    var launchDurationMillis: Long = DEFAULT_LAUNCH_CRASH_THRESHOLD_MS

    var sendLaunchCrashesSynchronously: Boolean = true
    var appType: String? = "android"
    var logger: Logger? = DebugLogger
        set(value) {
            field = value ?: DebugLogger
        }
//    var delivery: Delivery? = null
    var maxBreadcrumbs: Int = DEFAULT_MAX_BREADCRUMBS
    var maxPersistedEvents: Int = DEFAULT_MAX_PERSISTED_EVENTS
    var maxPersistedSessions: Int = DEFAULT_MAX_PERSISTED_SESSIONS
    var maxReportedThreads: Int = DEFAULT_MAX_REPORTED_THREADS
    var maxStringValueLength: Int = DEFAULT_MAX_STRING_VALUE_LENGTH

    var discardClasses: Set<String> = emptySet()
    var enabledReleaseStages: Set<String>? = null
    var enabledBreadcrumbTypes: Set<BreadcrumbType>? = null
    var projectPackages: Set<String> = emptySet()

    fun addMetadata(section: String, value: Map<String, Any?>) =
        metadataState.addMetadata(section, value)
    fun addMetadata(section: String, key: String, value: Any?) =
        metadataState.addMetadata(section, key, value)
    fun clearMetadata(section: String) = metadataState.clearMetadata(section)
    fun clearMetadata(section: String, key: String) = metadataState.clearMetadata(section, key)
    fun getMetadata(section: String) = metadataState.getMetadata(section)
    fun getMetadata(section: String, key: String) = metadataState.getMetadata(section, key)

    companion object {
        private const val DEFAULT_MAX_BREADCRUMBS = 100
        private const val DEFAULT_MAX_PERSISTED_SESSIONS = 128
        private const val DEFAULT_MAX_PERSISTED_EVENTS = 32
        private const val DEFAULT_MAX_REPORTED_THREADS = 200
        private const val DEFAULT_LAUNCH_CRASH_THRESHOLD_MS: Long = 5000
        private const val DEFAULT_MAX_STRING_VALUE_LENGTH = 10000

        @JvmStatic
        fun load(context: Context): Configuration = load(context)

    }
}