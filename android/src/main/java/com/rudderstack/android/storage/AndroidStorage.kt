package com.rudderstack.android.storage

import com.rudderstack.core.Storage
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.MessageContext

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
    val v1Build: Int?
    val versionName: String?
    val v1VersionName: String?
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

    fun resetV1Build()
    fun resetV1Version()
    fun resetV1SessionId()
    fun resetV1SessionLastActiveTimestamp()
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

    fun deleteV1SharedPreferencesFile()
    fun deleteV1ConfigFiles()
}
