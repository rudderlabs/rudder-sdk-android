/*
 * Creator: Debanjan Chatterjee on 08/07/22, 11:06 AM Last modified: 08/07/22, 11:06 AM
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

package com.rudderstack.android.internal.plugins

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.AndroidUtils.isTv
import com.rudderstack.core.Plugin
import com.rudderstack.core.internal.optAdd
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.util.*
import java.util.concurrent.ExecutorService

/**
 * Sets the context specific to Android
 *
 * @property application The application is needed to generate the required values
 * @constructor
 * Initiates the values
 *
 * @param autoCollectAdvertId If false advertising id won't be collected.
 * @param advertisingIdCollectorExecutor The executor to collect advertising id. Can be null if [autoCollectAdvertisingId] is false,
 * but if [autoCollectAdvertisingId] is true and this param is false, advertisingId won't be collected.
 */
internal class AndroidContextPlugin(
    private val application: Application,
    autoCollectAdvertId: Boolean,
    private val advertisingIdCollectorExecutor: ExecutorService?,
    private val jsonAdapter: JsonAdapter
) : Plugin {
    //if true collects advertising id automatically
    internal var autoCollectAdvertisingId = autoCollectAdvertId
        set(value) {
            field = value
            if (value)
                collectAdvertisingId(application)
            else synchronized(this) {
                _advertisingId = null
            }
        }

    private var _advertisingId: String? = null
    private var _deviceToken: String? = null

    init {
        if (autoCollectAdvertId)
            collectAdvertisingId(application)
    }

    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message()
        return msg.copy()
    }

    /**
     * Overriding advertising id, will disable auto collection if it's on
     *
     * @param advertisingId
     */
    internal fun setAdvertisingId(advertisingId: String) {
        autoCollectAdvertisingId = false
        synchronized(this) {
            _advertisingId = advertisingId
        }
    }

    internal fun putDeviceToken(deviceToken: String) {
        synchronized(this) {
            _deviceToken = deviceToken
        }
    }

    private fun collectAdvertisingId(application: Application) {
        advertisingIdCollectorExecutor?.submit {
            val adId =
                getGooglePlayServicesAdvertisingID(application) ?: getAmazonFireAdvertisingID(
                    application
                )
            if (adId != null) {
                synchronized(this) {
                    _advertisingId = adId
                }
            }
        }
    }


    @Throws(Exception::class)
    private fun getGooglePlayServicesAdvertisingID(application: Application): String? {

        val advertisingInfo =
            Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
                .getMethod("getAdvertisingIdInfo", Context::class.java)
                .invoke(null, application)
                ?: return null
        val isLimitAdTrackingEnabled = advertisingInfo.javaClass
            .getMethod("isLimitAdTrackingEnabled").invoke(advertisingInfo) as? Boolean
        if (isLimitAdTrackingEnabled == true) {
//            RudderLogger.logDebug("Not collecting advertising ID because isLimitAdTrackingEnabled (Google Play Services) is true.")
//            this.deviceInfo.setAdTrackingEnabled(false)
            return null
        }
        return advertisingInfo.javaClass.getMethod("getId").invoke(advertisingInfo) as? String
    }

    @Throws(Exception::class)
    private fun getAmazonFireAdvertisingID(application: Application): String? {

        val contentResolver: ContentResolver = application.contentResolver
        val limitAdTracking = Settings.Secure.getInt(contentResolver, "limit_ad_tracking") != 0
        if (limitAdTracking) {
//            RudderLogger.logDebug("Not collecting advertising ID because limit_ad_tracking (Amazon Fire OS) is true.")
//            this.deviceInfo.setAdTrackingEnabled(false)
            return null
        }
        return Settings.Secure.getString(
            contentResolver,
            "advertising_id"
        )
    }

//    private fun prepareAndroidContext(application: Application): MessageContext {
//
//
//    }

    private fun getAppDetails(application: Application): String? {
        try {
            val packageName = application.packageName
            val packageManager = application.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                packageInfo.longVersionCode.toString() else
                packageInfo.versionCode.toString()
            return mapOf(
                "name" to packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                "build" to build,
                "namespace" to packageName,
                "version" to packageInfo.versionName
            ).let {
                jsonAdapter.writeToJson(it, RudderTypeAdapter {})
            }
        } catch (ex: PackageManager.NameNotFoundException) {
//            RudderLogger.logError(ex.cause)
        }
        return null
    }

    private fun getOsInfo(): String? {
        return mapOf("name" to "Android", "version" to Build.VERSION.RELEASE).let {
            jsonAdapter.writeToJson(it, RudderTypeAdapter {})
        }
    }

    private fun getScreenInfo(application: Application): String? {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            application.display
        } else {
            val manager = application.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            manager?.defaultDisplay
        }
        return if (display != null) {
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            mapOf(
                "density" to displayMetrics.densityDpi,
                "height" to displayMetrics.heightPixels,
                "width" to displayMetrics.widthPixels
            ).let {
                jsonAdapter.writeToJson(it, RudderTypeAdapter {})
            }
        } else null

    }

    private fun getDeviceInfo(application: Application): String? {
        return ((mapOf(
            "id" to AndroidUtils.getDeviceId(application),
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "name" to Build.DEVICE,
            "type" to "Android",
            "adTrackingEnabled" to autoCollectAdvertisingId
        ) optAdd (if (_deviceToken != null)
            mapOf("token" to _deviceToken) else null)) optAdd if (_advertisingId != null)
            mapOf("advertisingId" to _advertisingId) else null).let {
            jsonAdapter.writeToJson(it, RudderTypeAdapter {})

        }
    }

    private fun getRudderNetwork(application: Application): String? {
        try {
            // carrier name
            val carrier = if (!isTv(application)) {
                val telephonyManager =
                    application.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                if (telephonyManager != null) telephonyManager.networkOperatorName else "NA"
            } else null

            // wifi enabled
            val isWifiEnabled =
                (application.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.isWifiEnabled

            // bluetooth
            val isBluetoothEnabled =
                BluetoothAdapter.getDefaultAdapter()?.state == BluetoothAdapter.STATE_ON

            // cellular status
            val tm = application.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val isCellularEnabled =
                if (tm != null && tm.simState == TelephonyManager.SIM_STATE_READY) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                        Settings.Global.getInt(application.contentResolver, "mobile_data", 1) == 1
                    } else {
                        Settings.Secure.getInt(application.contentResolver, "mobile_data", 1) == 1
                    }
                } else null
            val networkMap = HashMap<String, String>()
            if (carrier != null) {
                networkMap["carrier"] = carrier
            }
            networkMap["wifi"] = isWifiEnabled.toString()
            networkMap["bluetooth"] = isBluetoothEnabled.toString()
            networkMap["cellular"] = isCellularEnabled.toString()
           return jsonAdapter.writeToJson(
               networkMap, RudderTypeAdapter{}
           )
        } catch (ex: java.lang.Exception) {
//            RudderLogger.logError(ex)
        }
       return null
    }

    private val userAgent
        get() = System.getProperty("http.agent")
    private val locale
        get() = Locale.getDefault().language + "-" + Locale.getDefault().country
    private val timeZone
        get() = TimeZone.getDefault().id

}