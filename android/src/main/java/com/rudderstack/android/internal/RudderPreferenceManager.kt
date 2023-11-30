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

internal object RudderPreferenceManager  {

    // keys
    private const val RUDDER_PREFS = "rl_prefs"
    private const val RUDDER_SERVER_CONFIG_KEY = "rl_server_config"
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


    private lateinit var preferences: SharedPreferences
    fun initialize(application: Application) {
        preferences = application.getSharedPreferences(RUDDER_PREFS, Context.MODE_PRIVATE)
    }
    val lastUpdatedTime: Long
        get() = preferences.getLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY, -1)

    fun updateLastUpdatedTime() {
        preferences.edit().putLong(RUDDER_SERVER_CONFIG_LAST_UPDATE_KEY, System.currentTimeMillis())
            .apply()
    }

    val traits: String?
        get() = preferences.getString(RUDDER_TRAITS_KEY, null)

    fun saveTraits(traitsJson: String?) {
        preferences.edit().putString(RUDDER_TRAITS_KEY, traitsJson).apply()
    }

    val buildVersionCode: Int
        get() = preferences.getInt(RUDDER_APPLICATION_INFO_KEY, -1)

    fun saveBuildVersionCode(versionCode: Int) {
        preferences.edit().putInt(RUDDER_APPLICATION_INFO_KEY, versionCode).apply()
    }

    val externalIds: String?
        get() = preferences.getString(RUDDER_EXTERNAL_ID_KEY, null)

    fun saveExternalIds(externalIdsJson: String?) {
        preferences.edit().putString(RUDDER_EXTERNAL_ID_KEY, externalIdsJson).apply()
    }

    fun clearExternalIds() {
        preferences.edit().remove(RUDDER_EXTERNAL_ID_KEY).apply()
    }

    fun saveAnonymousId(anonymousId: String?) {
        preferences.edit().putString(RUDDER_ANONYMOUS_ID_KEY, anonymousId).apply()
    }

    val anonymousId: String?
        get() = preferences.getString(RUDDER_ANONYMOUS_ID_KEY, null)
    fun saveUserId(userId: String?) {
        preferences.edit().putString(RUDDER_USER_ID_KEY, userId).apply()
    }

    val userId: String?
        get() = preferences.getString(RUDDER_USER_ID_KEY, null)

    fun saveOptStatus(optStatus: Boolean) {
        preferences.edit().putBoolean(RUDDER_OPT_STATUS_KEY, optStatus).apply()
    }

    val optStatus: Boolean
        get() = preferences.getBoolean(RUDDER_OPT_STATUS_KEY, false)

    fun updateOptInTime() {
        preferences.edit().putLong(RUDDER_OPT_IN_TIME_KEY, System.currentTimeMillis()).apply()
    }

    fun updateOptOutTime() {
        preferences.edit().putLong(RUDDER_OPT_OUT_TIME_KEY, System.currentTimeMillis()).apply()
    }

    val optInTime: Long
        get() = preferences.getLong(RUDDER_OPT_IN_TIME_KEY, -1)
    val optOutTime: Long
        get() = preferences.getLong(RUDDER_OPT_OUT_TIME_KEY, -1)

    fun savePeriodicWorkRequestId(periodicWorkRequestId: String?) {
        preferences.edit().putString(RUDDER_PERIODIC_WORK_REQUEST_ID_KEY, periodicWorkRequestId)
            .apply()
    }

    val periodicWorkRequestId: String?
        get() = preferences.getString(RUDDER_PERIODIC_WORK_REQUEST_ID_KEY, null)

}