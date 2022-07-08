/*
 * Creator: Debanjan Chatterjee on 07/07/22, 4:44 PM Last modified: 07/07/22, 4:44 PM
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

package com.rudderstack.android

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import java.io.UnsupportedEncodingException
import java.util.*

internal object AndroidUtils {
    fun getDeviceId(application: Application): String {
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
    }
    internal fun getUTF8Length(message: String): Int {
        return try {
            message.toByteArray(charset("UTF-8")).size
        } catch (ex: UnsupportedEncodingException) {
//            RudderLogger.logError(ex);
            -1
        }
    }

    internal fun getUTF8Length(message: StringBuilder): Int {
        return getUTF8Length(message.toString())
    }

    internal fun isOnClassPath(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Returns whether the app is running on a TV device.
     *
     * @param context Any context.
     * @return Whether the app is running on a TV device.
     */
    internal fun isTv(context: Context): Boolean {
        val uiModeManager =
            context.applicationContext.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        return (uiModeManager != null
                && uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
    }
}