/*
 * Creator: Debanjan Chatterjee on 30/12/21, 6:28 PM Last modified: 30/12/21, 6:28 PM
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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.Storage
import com.rudderstack.android.models.Message

/**
 * Storage Decorator that invokes the listener based on the threshold set, with respect to time or
 * data count
 *
 * @property storage Platform specific implementation of [Storage]
 */
internal class StorageDecorator(private val storage : Storage = BasicStorageImpl()) : Storage by storage {

    /**
     * Calls back with data when this time has elapsed after last callback
     *
     * @param time in milli seconds
     */
    internal fun setMaxInterval(time : Long){}

    /**
     * Calls back with data if this threshold is reached
     *
     * @param count data count in storage
     */
    internal fun setMaxCountThreshold(count :Int){}

    internal fun interface Listener{
        fun onDataChange(messages: List<Message>)
    }
}