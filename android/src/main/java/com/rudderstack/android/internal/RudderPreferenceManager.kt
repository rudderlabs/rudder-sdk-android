/*
 * Creator: Debanjan Chatterjee on 04/07/22, 4:45 PM Last modified: 04/07/22, 4:45 PM
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

package com.rudderstack.android.internal

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

// keys
private const val RUDDER_PREFS = "rl_prefs"
private const val RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY = "rl_server_last_updated"
private const val RUDDER_TRAITS_KEY = "rl_traits"
private const val RUDDER_APPLICATION_INFO_KEY = "rl_application_info_key"
private const val RUDDER_EXTERNAL_ID_KEY = "rl_external_id"
private const val RUDDER_OPT_STATUS_KEY = "rl_opt_status"
private const val RUDDER_OPT_IN_TIME_KEY = "rl_opt_in_time"
private const val RUDDER_OPT_OUT_TIME_KEY = "rl_opt_out_time"
private const val RUDDER_ANONYMOUS_ID_KEY = "rl_anonymous_id_key"
private const val RUDDER_USER_ID_KEY = "rl_user_id_key"
private const val RUDDER_PERIODIC_WORK_REQUEST_ID_KEY = "rl_periodic_work_request_key"
private const val RUDDER_SESSION_ID_KEY = "rl_session_id_key"
private const val RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY =
    "rl_last_event_timestamp_key"
internal class RudderPreferenceManager(application: Application,
    private val writeKey: String) {

    private val String.key: String
        get() = "$this-$writeKey"

    private lateinit var preferences: SharedPreferences
    private lateinit var preferencesV1: SharedPreferences
    init {
        preferences = application.getSharedPreferences(RUDDER_PREFS.key, Context.MODE_PRIVATE)
        preferencesV1 = application.getSharedPreferences(RUDDER_PREFS, Context.MODE_PRIVATE)
    }
    val lastUpdatedTime: Long
        get() = preferences.getLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY.key,
            -1)

    fun updateLastUpdatedTime() {
        preferences.edit().putLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY.key, System
            .currentTimeMillis())
            .apply()
    }

    val traits: String?
        get() = preferences.getString(RUDDER_TRAITS_KEY.key, null)

    fun saveTraits(traitsJson: String?) {
        preferences.edit().putString(RUDDER_TRAITS_KEY.key, traitsJson).apply()
    }

    val buildVersionCode: Int
        get() = preferences.getInt(RUDDER_APPLICATION_INFO_KEY.key, -1)

    fun saveBuildVersionCode(versionCode: Int) {
        preferences.edit().putInt(RUDDER_APPLICATION_INFO_KEY.key, versionCode).apply()
    }

    val externalIds: String?
        get() = preferences.getString(RUDDER_EXTERNAL_ID_KEY.key, null)

    fun saveExternalIds(externalIdsJson: String?) {
        preferences.edit().putString(RUDDER_EXTERNAL_ID_KEY.key, externalIdsJson).apply()
    }

    fun clearExternalIds() {
        preferences.edit().remove(RUDDER_EXTERNAL_ID_KEY.key).apply()
    }

    fun saveAnonymousId(anonymousId: String?) {
        preferences.edit().putString(RUDDER_ANONYMOUS_ID_KEY.key, anonymousId).apply()
    }
    fun saveSessionId(sessionId: Long?) {
        preferences.edit().putLong(RUDDER_SESSION_ID_KEY.key, sessionId?:-1L).apply()
    }
    fun clearSessionId() {
        preferences.edit().remove(RUDDER_SESSION_ID_KEY.key).apply()
    }
    val sessionId: Long
        get() = preferences.getLong(RUDDER_SESSION_ID_KEY.key, -1L)
    fun saveLastActiveTimestamp(lastActiveTimestamp: Long?) {
        preferences.edit().putLong(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY.key, lastActiveTimestamp
                                                                              ?: -1L).apply()
    }
    fun clearLastActiveTimestamp() {
        preferences.edit().remove(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY.key).apply()
    }
    val lastActiveTimestamp: Long
        get() = preferences.getLong(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY.key, -1L)

    val anonymousId: String?
        get() = preferences.getString(RUDDER_ANONYMOUS_ID_KEY.key, null)
    fun saveUserId(userId: String?) {
        preferences.edit().putString(RUDDER_USER_ID_KEY.key, userId).apply()
    }

    val userId: String?
        get() = preferences.getString(RUDDER_USER_ID_KEY.key, null)

    fun updateOptInTime() {
        preferences.edit().putLong(RUDDER_OPT_IN_TIME_KEY.key, System.currentTimeMillis()).apply()
    }

    fun updateOptOutTime() {
        preferences.edit().putLong(RUDDER_OPT_OUT_TIME_KEY.key, System.currentTimeMillis()).apply()
    }

    val optInTime: Long
        get() = preferences.getLong(RUDDER_OPT_IN_TIME_KEY.key, -1)
    val optOutTime: Long
        get() = preferences.getLong(RUDDER_OPT_OUT_TIME_KEY.key, -1)

    fun savePeriodicWorkRequestId(periodicWorkRequestId: String?) {
        preferences.edit().putString(RUDDER_PERIODIC_WORK_REQUEST_ID_KEY.key, periodicWorkRequestId)
            .apply()
    }

    fun resetV1AnonymousId() {
        preferencesV1.edit().remove(RUDDER_ANONYMOUS_ID_KEY).apply()
    }
    fun resetV1Traits() {
        preferencesV1.edit().remove(RUDDER_TRAITS_KEY).apply()
    }
    fun resetV1ExternalIds() {
        preferencesV1.edit().remove(RUDDER_EXTERNAL_ID_KEY).apply()
    }
    fun resetV1OptOut() {
        preferencesV1.edit().remove(RUDDER_OPT_STATUS_KEY).apply()
    }

    val periodicWorkRequestId: String?
        get() = preferences.getString(RUDDER_PERIODIC_WORK_REQUEST_ID_KEY.key, null)
    val v1AnonymousId
        get() = preferencesV1.getString(RUDDER_ANONYMOUS_ID_KEY, null)

    internal val v1ExternalIdsJson: String?
        get() =  preferencesV1.getString(RUDDER_EXTERNAL_ID_KEY, null)

    internal val v1Traits
        get() = preferencesV1.getString(RUDDER_TRAITS_KEY, null)

    internal val v1optOutStatus: Boolean
        get() = preferencesV1.getBoolean(RUDDER_OPT_STATUS_KEY, false)

    internal val v1LastActiveTimestamp: Long?
        get() = preferencesV1.getLong(RUDDER_SESSION_LAST_ACTIVE_TIMESTAMP_KEY, -1L).takeIf {
            it > 0
        }

    internal val v1SessionId : Long
        get() =  preferencesV1.getLong(RUDDER_SESSION_ID_KEY, -1)
}
