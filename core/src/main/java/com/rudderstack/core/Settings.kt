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

package com.rudderstack.core

/**
 * Settings to be applied to the sdk.
 * To change the settings, create a copy and apply.
 * ```
 * val newSettings = settings.copy(options = settings.options.newBuilder().withExternalIds(mapOf()).build(),
 *  trackLifecycleEvents = true)
 * ```
 *
 * @property options Global [RudderOptions] for all plugins
 * @property flushQueueSize Max elements to be stored before a flush. Once it passes this threshold,
 * flush is triggered
 * @property maxFlushInterval Max time (in millis) to wait for a flush, even if the queue size hasn't
 * passed the threshold
// * @property trackLifecycleEvents Will track activity lifecycle if set to true, else false
// * @property recordScreenViews Will record screen views if true.
 * @property isOptOut GDPR implementation. Data won't be sent if GDPR is true
 */
data class Settings @JvmOverloads constructor(
    val anonymousId : String? = null,
    val userId : String? = null,
    val options: RudderOptions = RudderOptions.default(),
    val flushQueueSize: Int = FLUSH_QUEUE_SIZE,
    val maxFlushInterval: Long = MAX_FLUSH_INTERVAL,
    val isOptOut : Boolean = false
) {

    constructor(flushQueueSize: Int,
                maxFlushInterval: Long) : this(null, flushQueueSize = flushQueueSize,
        maxFlushInterval = maxFlushInterval)
    constructor(isOptOut: Boolean) : this(null, isOptOut = isOptOut)
    companion object {
        /**
         * Default values for settings
         */

        // default flush queue size for the events to be flushed to server
        private const val FLUSH_QUEUE_SIZE = 30

        // default timeout for event flush
        // if events are registered and flushQueueSize is not reached
        // events will be flushed to server after maxFlushInterval millis
        private const val MAX_FLUSH_INTERVAL = 10 * 1000L //10 seconds


    }
    /**
     * Extension function for URL.
     * In case URL doesn't end with '/'
     * this will append it.
     */
    private val String.formattedUrl
        get() = if (this.endsWith('/')) this else "$this/"
    fun copy(flushQueueSize: Int,
             maxFlushInterval: Long) = copy(anonymousId = this.anonymousId, flushQueueSize = flushQueueSize,
        maxFlushInterval = maxFlushInterval)
    fun copy(isOptOut: Boolean) = copy(anonymousId = this.anonymousId, isOptOut = isOptOut)
    fun copy(userId: String?) = copy(anonymousId = this.anonymousId, userId = userId)
    fun copy(options: RudderOptions) = copy(anonymousId = this.anonymousId, options = options)
}