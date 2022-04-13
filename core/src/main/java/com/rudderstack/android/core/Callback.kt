/*
 * Creator: Debanjan Chatterjee on 10/04/22, 8:12 PM Last modified: 10/04/22, 8:12 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

import com.rudderstack.android.models.Message

/**
 * Callback invoked when the client library is done processing a message.
 *
 *
 * Methods may be called on background threads, implementations must implement their own
 * synchronization if needed. Implementations should also take care to make the methods
 * non-blocking.
 */
interface Callback {
    /**
     * Invoked when the message is successfully uploaded to Rudder.
     *
     *
     * Note: The Rudder HTTP API itself is asynchronous, so this doesn't indicate whether the
     * message was sent to all integrations or not — just that the message was sent to the Rudder API
     * and will be sent to integrations at a later time.
     */
    fun success(message: Message?)

    /**
     * Invoked when the library gives up on sending a message.
     *
     *
     * This could be due to exhausting retries, or other unexpected errors. Use the `throwable` provided to take further action.
     */
    fun failure(message: Message?, throwable: Throwable?)
}