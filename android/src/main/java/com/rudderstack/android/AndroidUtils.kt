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
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Secure
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import com.rudderstack.core.Base64Generator
import com.rudderstack.models.android.RudderApp
import com.rudderstack.models.android.RudderContext
import com.rudderstack.models.android.RudderDeviceInfo
import com.rudderstack.models.android.RudderNetwork
import com.rudderstack.models.android.RudderOSInfo
import com.rudderstack.models.android.RudderScreenInfo
import com.rudderstack.models.android.RudderTraits
import java.io.UnsupportedEncodingException
import java.util.*


internal object AndroidUtils {
    fun getDeviceId(application: Application): String {

//        val androidId =
//            Settings.System.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
//        if (!TextUtils.isEmpty(androidId) && "9774d56d682e549c" != androidId && "unknown" != androidId && "000000000000000" != androidId) {
//            return androidId
//        }


        // If this still fails, generate random identifier that does not persist across installations
        return UUID.randomUUID().toString()
    }

    fun getWriteKeyFromStrings(context: Context): String? {
        val id = context.resources.getIdentifier(
            context.packageName, "string", "rudder_write_key"
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
    internal fun Context.isTv(): Boolean {
        val uiModeManager =
            applicationContext.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        return (uiModeManager != null && uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
    }

    internal fun defaultBase64Generator() = Base64Generator {
        Base64.encodeToString(
            String.format(Locale.US, "%s:", it).toByteArray(Charsets.UTF_8), Base64.NO_WRAP
        )
    }

    private fun Application.generateDeviceInfo(
        advertisingId: String?,
        deviceToken: String,
        collectDeviceId: Boolean
    ): RudderDeviceInfo {
        val deviceId = if (collectDeviceId) getDeviceId(this) else null
        return RudderDeviceInfo(
            deviceId = deviceId,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            name = Build.DEVICE,
            token = deviceToken,
            isAdTrackingEnabled = advertisingId != null
        ).also {
            if (advertisingId != null) it.advertisingId = advertisingId
        }
    }


    private val Application.rudderApp: RudderApp?
        get() = try {
            val packageName: String = this.packageName
            val packageManager: PackageManager = packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val build =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode.toString() else packageInfo.versionCode.toString()
            val name = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            val version = packageInfo.versionName
            RudderApp(
                name = name, version = version, build = build, nameSpace = packageName
            )
        } catch (ex: PackageManager.NameNotFoundException) {
//            ReportManager.reportError(ex)
//            RudderLogger.logError(ex.cause)
            null
        }
    private val Application.screen : RudderScreenInfo?
        get() = run {
            val manager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            return@run if (manager == null) {
                null
            }else {
                val display = manager.defaultDisplay
                val displayMetrics = DisplayMetrics()
                display.getMetrics(displayMetrics)
                RudderScreenInfo(
                     displayMetrics.densityDpi,
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels
                )
            }

        }

}