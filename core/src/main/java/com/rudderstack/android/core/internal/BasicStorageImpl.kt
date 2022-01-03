/*
 * Creator: Debanjan Chatterjee on 30/12/21, 6:25 PM Last modified: 30/12/21, 6:25 PM
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

internal class BasicStorageImpl : Storage {
    override fun saveMessage(vararg messages: Message) {
        TODO("Not yet implemented")
    }

    override fun deleteMessages(messages: List<Message>) {
        TODO("Not yet implemented")
    }

    override fun cacheContext(context: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override val context: Map<String, String>
        get() = TODO("Not yet implemented")

    override fun saveOptOut(optOut: Boolean) {
        TODO("Not yet implemented")
    }

    override val isOptedOut: Boolean
        get() = TODO("Not yet implemented")
    override val optOutTime: Long
        get() = TODO("Not yet implemented")
    override val optInTime: Long
        get() = TODO("Not yet implemented")
}