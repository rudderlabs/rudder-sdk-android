/*
 * Creator: Debanjan Chatterjee on 11/01/22, 11:07 PM Last modified: 05/11/21, 7:42 PM
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

package com.rudderstack.core

import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

internal object Utils {
    // range constants
    /*const val MIN_CONFIG_REFRESH_INTERVAL = 1
    const val MAX_CONFIG_REFRESH_INTERVAL = 24
    const val MIN_SLEEP_TIMEOUT = 10
    const val MIN_FLUSH_QUEUE_SIZE = 1
    const val MAX_FLUSH_QUEUE_SIZE = 100*/
    internal const val MAX_EVENT_SIZE = 32 * 1024 // 32 KB
    internal const val MAX_BATCH_SIZE = 500 * 1024 // 500 KB
    val timeZone: String
        get() {
            val timeZone = TimeZone.getDefault()
            return timeZone.id
        }
    val timeStamp: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("GMT")
            return formatter.format(Date())
        }

    fun toDateString(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return date.let {
            formatter.format(it)
        }
    }

    /*fun getDeviceId(application: Application): String {
        if (Build.VERSION.SDK_INT >= 17) {
            val androidId =
                Settings.System.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
            if (!TextUtils.isEmpty(androidId)
                && "9774d56d682e549c" != androidId
                && "unknown" != androidId
                && "000000000000000" != androidId
            ) {
                return androidId
            }
        }

        // If this still fails, generate random identifier that does not persist across installations
        return UUID.randomUUID().toString()
    }

    fun getWriteKeyFromStrings(context: Context): String? {
        val id = context.resources.getIdentifier(
            context.packageName,
            "string",
            "rudder_write_key"
        )
        return if (id != 0) {
            context.resources.getString(id)
        } else {
            null
        }
    }*/

    fun getUTF8Length(message: String): Int {
        return try {
            message.toByteArray(charset("UTF-8")).size
        } catch (ex: UnsupportedEncodingException) {
//            RudderLogger.logError(ex);
            -1
        }
    }

    fun getUTF8Length(message: StringBuilder): Int {
        return getUTF8Length(message.toString())
    }

    fun isOnClassPath(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    enum class NetworkResponses {
        SUCCESS, ERROR, WRITE_KEY_ERROR
    }
}