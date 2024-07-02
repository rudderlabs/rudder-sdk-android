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

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.VisibleForTesting
import com.rudderstack.android.AndroidUtils.getDeviceId
import com.rudderstack.android.AndroidUtils.isOnClassPath
import com.rudderstack.android.AndroidUtils.isTv
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.utilities.applyConfigurationAndroid
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.optAdd
import com.rudderstack.models.Message
import com.rudderstack.models.MessageContext
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * Sets the context specific to Android
 *
 * @constructor Initiates the values
 *
 */
private const val CHANNEL = "mobile"

internal class PlatformInputsPlugin : Plugin, LifecycleListenerPlugin {
    //if true collects advertising id automatically
    private val application
        get() = _analytics?.currentConfigurationAndroid?.application

    private var autoCollectAdvertisingId = false
        set(value) {
            field = value
            if (value && _advertisingId.isNullOrEmpty()) application?.collectAdvertisingId()
            else if(!value) synchronized(this) {
                _advertisingId = null
            }
        }
    private var _collectDeviceId = false
        set(value) {
            if (field == value) return
            field = value
            if (value) application?.collectDeviceId()
            else synchronized(this) {
                _deviceId = null
            }
        }

    private var _deviceId: String? = null
    private var _advertisingId: String? = null
    private var _deviceToken: String? = null

    private var _analytics: Analytics? = null
    private val _currentActivity: AtomicReference<Activity?> = AtomicReference()
    private val currentActivity: Activity?
        get() = _currentActivity.get()

    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message()
        val newMsg =
            msg.copy(context = msg.context optAdd application?.defaultAndroidContext()).also {
                it.channel = CHANNEL
            }
        return chain.proceed(newMsg)
    }

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        _analytics = analytics
        analytics.currentConfigurationAndroid?.updateAdvertisingValues()
    }

    override fun updateConfiguration(configuration: Configuration) {
        if (configuration !is ConfigurationAndroid) return
        configuration.updateAdvertisingValues()
        _defaultAndroidContext = null
    }

    private fun ConfigurationAndroid.updateAdvertisingValues() {
        if(!advertisingId.isNullOrEmpty()) {
            synchronized(this) {
                if (_advertisingId != advertisingId)
                    _advertisingId = advertisingId
            }
        }
        autoCollectAdvertisingId = autoCollectAdvertId
        _collectDeviceId = collectDeviceId
    }

    /**
     * Overriding advertising id, will disable auto collection if it's on
     *
     * @param advertisingId
     */

    @VisibleForTesting
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
        if(!isOnClassPath("com.google.android.gms.ads.identifier.AdvertisingIdClient")){
            _analytics?.currentConfiguration?.logger?.debug(log = "Not collecting advertising ID because "
                    + "com.google.android.gms.ads.identifier.AdvertisingIdClient "
                    + "was not found on the classpath."
            )
            return
        }
        _analytics?.currentConfigurationAndroid?.advertisingIdFetchExecutor?.submit {
            val adId = try {
                getGooglePlayServicesAdvertisingID()
            } catch (ex: Exception) {
                _analytics?.currentConfiguration?.logger?.error(log = "Error collecting play services ad id", throwable = ex)
                null
            } ?: try { getAmazonFireAdvertisingID() } catch (ex: Exception){
                _analytics?.currentConfiguration?.logger?.error(log = "Error collecting amazon fire ad id", throwable = ex)
                null
            }
            _analytics?.currentConfiguration?.logger?.info(log = "Ad id collected is $adId")
            if (adId != null) {
                _analytics?.applyConfigurationAndroid {
                    copy(advertisingId = adId)
                }
            }
        }
    }

    private fun Application.collectDeviceId() {
        synchronized(this@PlatformInputsPlugin) {
            _deviceId = getDeviceId(this)
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
            return null
        }
        return Settings.Secure.getString(
            contentResolver, "advertising_id"
        )
    }

    private var _defaultAndroidContext: MessageContext? = null
    private fun Application.defaultAndroidContext(): MessageContext {
        return synchronized(this) { _defaultAndroidContext } ?: generateDefaultAndroidContext()
    }

    private fun Application.generateDefaultAndroidContext() = mapOf<String, Any?>(
        "app" to getAppDetails(),
        "os" to getOsInfo(),
        "screen" to getScreenInfo(),
        "userAgent" to userAgent,
        "locale" to locale,
        "device" to getDeviceInfo(),
        "network" to getRudderNetwork(),
        "timezone" to timeZone
    ).also {
        synchronized(this) {
            _defaultAndroidContext = it
        }
    }

    private fun Application.getAppDetails(): Any? {
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
            )
        } catch (ex: PackageManager.NameNotFoundException) {
            _analytics?.currentConfiguration?.logger?.error(
                log = "Package Name Not Found",
                throwable = ex
            )
        }
        return null
    }

    private fun getOsInfo(): Any? {
        return mapOf("name" to "Android", "version" to Build.VERSION.RELEASE)
    }

    private fun Application.getScreenInfo(): Any? {
        return currentActivity?.resources?.displayMetrics?.let {
            mapOf(
                "density" to it.densityDpi,
                "height" to it.heightPixels,
                "width" to it.widthPixels
            )
        }

    }

    private fun Application.getDeviceInfo(): Any? {
        return ((mapOf(
            "id" to _deviceId,
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "name" to Build.DEVICE,
            "type" to "Android",
            "adTrackingEnabled" to !_advertisingId.isNullOrEmpty()
        ) optAdd (if (_deviceToken != null) mapOf("token" to _deviceToken) else null)) optAdd if (_advertisingId != null) mapOf(
            "advertisingId" to _advertisingId
        ) else null)
    }

    private fun Application.getRudderNetwork(): Map<String, Any> {
        // carrier name
        val carrier = if (!isTv()) {
            val telephonyManager =
                getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (telephonyManager != null) telephonyManager.networkOperatorName else "NA"
        } else null

        // wifi enabled
        val isWifiEnabled =
            try {
                (this.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.isWifiEnabled?:false
            } catch (ex: Exception) {
                _analytics?.currentConfiguration?.logger?.error(log = "Cannot detect wifi. Wifi Permission not available")
                false
            }


        // bluetooth
        val isBluetoothEnabled =
            BluetoothAdapter.getDefaultAdapter()?.state == BluetoothAdapter.STATE_ON

        // cellular status
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val isCellularEnabled =
            if (tm != null && tm.simState == TelephonyManager.SIM_STATE_READY) {
                Settings.Global.getInt(contentResolver, "mobile_data", 1) == 1
            } else false
        val networkMap = HashMap<String, Any>()
        if (carrier != null) {
            networkMap["carrier"] = carrier
        }
        networkMap["wifi"] = isWifiEnabled
        networkMap["bluetooth"] = isBluetoothEnabled
        networkMap["cellular"] = isCellularEnabled
        return networkMap

    }

    private val userAgent
        get() = System.getProperty("http.agent")
    private val locale
        get() = Locale.getDefault().language + "-" + Locale.getDefault().country
    private val timeZone
        get() = TimeZone.getDefault().id

    override fun onAppBackgrounded() {
        _currentActivity.set(null)
    }

    override fun setCurrentActivity(activity: Activity?) {
        _currentActivity.set(activity)
        _defaultAndroidContext = null
        //we generate the default context here because we need the activity to get the screen info
        application?.generateDefaultAndroidContext()
    }

    override fun onShutDown() {
        super.onShutDown()
        _analytics?.currentConfigurationAndroid?.advertisingIdFetchExecutor?.shutdownNow()
        _analytics = null
    }

}
