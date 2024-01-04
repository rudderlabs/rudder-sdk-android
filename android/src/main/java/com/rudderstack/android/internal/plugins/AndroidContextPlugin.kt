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
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.optAdd
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import java.util.*

/**
 * Sets the context specific to Android
 *
 * @property application The application is needed to generate the required values
 * @constructor
 * Initiates the values
 *
 */
internal class AndroidContextPlugin : Plugin {
    //if true collects advertising id automatically
    private val jsonAdapter
        get() = _analytics?.currentConfiguration?.jsonAdapter

    private val application
        get() = _analytics?.currentConfigurationAndroid?.application

    private var autoCollectAdvertisingId = false
        set(value) {
            if (field == value) return
            field = value
            if (value) application?.collectAdvertisingId()
            else synchronized(this) {
                _advertisingId = null
            }
        }

    private var _advertisingId: String? = null
    private var _deviceToken: String? = null

    private var _analytics: Analytics? = null

    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message()
        val newMsg = msg.copy(context = msg.context optAdd application?.defaultAndroidContext())
        return chain.proceed(newMsg)
    }

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
        autoCollectAdvertisingId =
            analytics.currentConfigurationAndroid?.autoCollectAdvertId ?: false
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

    private fun Application.collectAdvertisingId() {
        _analytics?.currentConfigurationAndroid?.advertisingIdFetchExecutor?.submit {
            val adId = getGooglePlayServicesAdvertisingID() ?: getAmazonFireAdvertisingID()
            if (adId != null) {
                synchronized(this) {
                    _advertisingId = adId
                }
            }
        }
    }


    @Throws(Exception::class)
    private fun Application.getGooglePlayServicesAdvertisingID(): String? {

        val advertisingInfo =
            Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
                .getMethod("getAdvertisingIdInfo", Context::class.java).invoke(null, this)
            ?: return null
        val isLimitAdTrackingEnabled =
            advertisingInfo.javaClass.getMethod("isLimitAdTrackingEnabled")
                .invoke(advertisingInfo) as? Boolean
        if (isLimitAdTrackingEnabled == true) {
            _analytics?.logger?.debug(log = "Not collecting advertising ID because isLimitAdTrackingEnabled (Google Play Services) is true.")
//            this.deviceInfo.setAdTrackingEnabled(false)
            return null
        }
        return advertisingInfo.javaClass.getMethod("getId").invoke(advertisingInfo) as? String
    }

    @Throws(Exception::class)
    private fun Application.getAmazonFireAdvertisingID(): String? {

        val contentResolver: ContentResolver = contentResolver
        val limitAdTracking = Settings.Secure.getInt(contentResolver, "limit_ad_tracking") != 0
        if (limitAdTracking) {
            _analytics?.logger?.debug(log = "Not collecting advertising ID because limit_ad_tracking (Amazon Fire OS) is true.")
//            this.deviceInfo.setAdTrackingEnabled(false)
            return null
        }
        return Settings.Secure.getString(
            contentResolver, "advertising_id"
        )
    }
    private var _defaultAndroidContext: MessageContext? = null
    private fun Application.defaultAndroidContext(): MessageContext {
        return _defaultAndroidContext?:mapOf<String, Any?>(
            "app" to getAppDetails(),
            "os" to getOsInfo(),
            "screen" to getScreenInfo(),
            "userAgent" to userAgent,
            "locale" to locale,
            "device" to getDeviceInfo(),
            "network" to getRudderNetwork(),
            "timezone" to timeZone
        ).also { _defaultAndroidContext = it }
    }

    private fun Application.getAppDetails(): String? {
        try {
            val packageName = packageName
            val packageManager = packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val build =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode.toString() else packageInfo.versionCode.toString()
            return mapOf(
                "name" to packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                "build" to build,
                "namespace" to packageName,
                "version" to packageInfo.versionName
            ).let {
                jsonAdapter?.writeToJson(it, RudderTypeAdapter {})
            }
        } catch (ex: PackageManager.NameNotFoundException) {
//            RudderLogger.logError(ex.cause)
            //TODO(add logging)
        }
        return null
    }

    private fun getOsInfo(): String? {
        return mapOf("name" to "Android", "version" to Build.VERSION.RELEASE).let {
            jsonAdapter?.writeToJson(it, RudderTypeAdapter {})
        }
    }

    private fun Application.getScreenInfo(): String? {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            val manager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
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
                jsonAdapter?.writeToJson(it, RudderTypeAdapter {})
            }
        } else null

    }

    private fun Application.getDeviceInfo(): String? {
        return ((mapOf(
            "id" to AndroidUtils.getDeviceId(this),
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "name" to Build.DEVICE,
            "type" to "Android",
            "adTrackingEnabled" to autoCollectAdvertisingId
        ) optAdd (if (_deviceToken != null) mapOf("token" to _deviceToken) else null)) optAdd if (_advertisingId != null) mapOf(
            "advertisingId" to _advertisingId
        ) else null).let {
            jsonAdapter?.writeToJson(it, RudderTypeAdapter {})

        }
    }

    private fun Application.getRudderNetwork(): String? {
        try {
            // carrier name
            val carrier = if (!isTv()) {
                val telephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                if (telephonyManager != null) telephonyManager.networkOperatorName else "NA"
            } else null

            // wifi enabled
            val isWifiEnabled =
                (this.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.isWifiEnabled

            // bluetooth
            val isBluetoothEnabled =
                BluetoothAdapter.getDefaultAdapter()?.state == BluetoothAdapter.STATE_ON

            // cellular status
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val isCellularEnabled =
                if (tm != null && tm.simState == TelephonyManager.SIM_STATE_READY) {

                    Settings.Global.getInt(contentResolver, "mobile_data", 1) == 1
                } else null
            val networkMap = HashMap<String, String>()
            if (carrier != null) {
                networkMap["carrier"] = carrier
            }
            networkMap["wifi"] = isWifiEnabled.toString()
            networkMap["bluetooth"] = isBluetoothEnabled.toString()
            networkMap["cellular"] = isCellularEnabled.toString()
            return jsonAdapter?.writeToJson(networkMap, RudderTypeAdapter {})
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