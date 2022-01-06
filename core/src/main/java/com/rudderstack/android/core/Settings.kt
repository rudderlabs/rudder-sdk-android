/*
 * Creator: Debanjan Chatterjee on 30/12/21, 1:26 PM Last modified: 29/12/21, 5:30 PM
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

package com.rudderstack.android.core

/**
 * Settings to be applied to the sdk.
 * To change the settings, create a copy and apply.
 * ```
 * val newSettings = settings.copy(options = settings.options.newBuilder().withExternalIds(mapOf()).build(),
 *  trackLifecycleEvents = true)
 * ```
 *
 * @property options Global [RudderOptions] for all plugins
 * @property dataPlaneUrl Base url to hit for cloud mode
 * @property flushQueueSize Max elements to be stored before a flush. Once it passes this threshold,
 * flush is triggered
 * @property maxFlushInterval Max time (in millis) to wait for a flush, even if the queue size hasn't
 * passed the threshold
 * @property controlPlaneUrl Config plane url to get the write key
 * @property trackLifecycleEvents Will track activity lifecycle if set to true, else false
 * @property recordScreenViews Will record screen views if true.
 */
data class Settings(
    val options: RudderOptions = RudderOptions.default(),
    val dataPlaneUrl: String = DATA_PLANE_URL,
    val flushQueueSize: Int = FLUSH_QUEUE_SIZE,
    val maxFlushInterval: Long = MAX_FLUSH_INTERVAL,
    val controlPlaneUrl: String = CONTROL_PLANE_URL,
    //available in android only
//    val trackLifecycleEvents: Boolean = false,
//    val recordScreenViews: Boolean = false,
) {

    companion object {
        /**
         * Default values for settings
         */
        // default base url or rudder-backend-server
        private const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"

        // default flush queue size for the events to be flushed to server
        private const val FLUSH_QUEUE_SIZE = 30

        // default timeout for event flush
        // if events are registered and flushQueueSize is not reached
        // events will be flushed to server after maxFlushInterval millis
        private const val MAX_FLUSH_INTERVAL = 10 * 1000L //10 seconds

        // config-plane url to get the config for the writeKey
        private const val CONTROL_PLANE_URL = "https://api.rudderlabs.com"

    }

}