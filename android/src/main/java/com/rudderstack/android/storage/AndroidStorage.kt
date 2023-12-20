/*
 * Creator: Debanjan Chatterjee on 27/11/23, 1:07 pm Last modified: 27/11/23, 12:56 pm
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

package com.rudderstack.android.storage

import com.rudderstack.core.Storage
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext

interface AndroidStorage : Storage {
    val anonymousId: String?
    val userId: String?

    /**
     * Platform specific implementation of caching context. This can be done locally too.
     *
     * @param context A map representing the context. Refer to [Message]
     */
    fun cacheContext(context: MessageContext)


    /**
     * Retrieve the cached context
     */
    val context: MessageContext?
    fun setAnonymousId(anonymousId: String)
    fun setUserId(userId: String)
}