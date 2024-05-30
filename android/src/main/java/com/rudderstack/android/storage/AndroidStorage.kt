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
    val v1OptOut: Boolean
    val anonymousId: String?
    val userId: String?
    val sessionId: Long?
    val lastActiveTimestamp: Long?
    val advertisingId: String?
    val v1AnonymousId: String?
    val v1SessionId: Long?
    val v1LastActiveTimestamp: Long?
    val v1Traits: Map<String, Any?>?
    val v1ExternalIds: List<Map<String, String>>?
    val v1AdvertisingId: String?
    val trackAutoSession: Boolean
    val build: Int?
    val versionName: String?
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

    fun setSessionId(sessionId: Long)
    fun setTrackAutoSession(trackAutoSession : Boolean)
    fun saveLastActiveTimestamp(timestamp: Long)
    fun saveAdvertisingId(advertisingId: String)
    fun clearSessionId()
    fun clearLastActiveTimestamp()
    fun resetV1AnonymousId()
    fun resetV1OptOut()
    fun resetV1Traits()
    fun resetV1ExternalIds()
    fun resetV1AdvertisingId()
    fun setBuild(build: Int)
    fun setVersionName(versionName: String)

    /**
     * Migrate the v1 database to current v2 database
     *
     * @return true if v1 database exists else false
     */
    fun migrateV1StorageToV2Sync() : Boolean

    /**
     * Migrate the v1 database to current v2 database on a separate executor
     *
     * @param callback Callback with true if v1 database exists else false
     */
    fun migrateV1StorageToV2(callback: (Boolean) -> Unit)
}
