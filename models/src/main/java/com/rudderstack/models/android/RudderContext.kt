/*
 * Creator: Debanjan Chatterjee on 15/12/21, 3:21 PM Last modified: 12/12/21, 8:02 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.models.android

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class RudderContext {
    @SerializedName("app")
    @JsonProperty("app")
    @Json(name = "app")
    private var app: RudderApp? = null

    @SerializedName("traits")
    @JsonProperty("traits")
    @Json(name = "traits")
    private var traits: MutableMap<String, Any?>? = null

    @SerializedName("library")
    @JsonProperty("library")
    @Json(name = "library")
    private var libraryInfo: RudderLibraryInfo? = null

    @SerializedName("os")
    @JsonProperty("os")
    @Json(name = "os")
    private var osInfo: RudderOSInfo? = null

    @SerializedName("screen")
    @JsonProperty("screen")
    @Json(name = "screen")
    private var screenInfo: RudderScreenInfo? = null

    @SerializedName("userAgent")
    @JsonProperty("userAgent")
    @Json(name = "userAgent")
    private var userAgent: String? = null

    @SerializedName("locale")
    @JsonProperty("locale")
    @Json(name = "locale")
    private var locale: String? = null

    @SerializedName("device")
    @JsonProperty("device")
    @Json(name = "device")
    private var deviceInfo: RudderDeviceInfo? = null

    @SerializedName("network")
    @JsonProperty("network")
    @Json(name = "network")
    private var networkInfo: RudderNetwork? = null

    @SerializedName("timezone")
    @JsonProperty("timezone")
    @Json(name = "timezone")
    private var timezone: String? = null

    @SerializedName("externalId")
    @JsonProperty("externalId")
    @Json(name = "externalId")
    private var externalIds: MutableList<MutableMap<String, Any?>>? = null
    var customContextMap: MutableMap<String, Any>? = null

    /*private constructor() {
        // stop instantiating without application instance.
        // cachedContext is used every time, once initialized
    }

    internal constructor(
        application: Application?,
        anonymousId: String?,
        advertisingId: String?,
        deviceToken: String?
    ) {
        var anonymousId = anonymousId
        val preferenceManger: RudderPreferenceManager =
            RudderPreferenceManager.getInstance(application)
        if (TextUtils.isEmpty(anonymousId)) {
            anonymousId =
                if (preferenceManger.getAnonymousId() != null) preferenceManger.getAnonymousId() else Utils.getDeviceId(
                    application
                )
        } else {
            preferenceManger.saveAnonymousId(anonymousId)
        }
        Companion.anonymousId = anonymousId
        app = RudderApp (application)

        // get saved traits from prefs. if not present create new one and save
        val traitsJson: String = preferenceManger.getTraits()
        RudderLogger.logDebug(
            String.format(
                Locale.US,
                "Traits from persistence storage%s",
                traitsJson
            )
        )
        if (traitsJson == null) {
            val traits = RudderTraits(anonymousId)
            this.traits = Utils.convertToMap(Gson().toJson(traits))
            persistTraits()
            RudderLogger.logDebug("New traits has been saved")
        } else {
            traits = Utils.convertToMap(traitsJson)
            RudderLogger.logDebug("Using old traits from persistence")
        }

        // get saved external Ids from prefs. if not present set it to null
        val externalIdsJson: String = preferenceManger.getExternalIds()
        RudderLogger.logDebug(
            String.format(
                Locale.US,
                "ExternalIds from persistence storage%s",
                externalIdsJson
            )
        )
        if (externalIdsJson != null) {
            externalIds = Utils.convertToList(externalIdsJson)
            RudderLogger.logDebug("Using old externalIds from persistence")
        }
        screenInfo = RudderScreenInfo(application)
        userAgent = System.getProperty("http.agent")
        deviceInfo = RudderDeviceInfo(advertisingId, deviceToken)
        networkInfo = RudderNetwork(application)
        osInfo = RudderOSInfo()
        libraryInfo = RudderLibraryInfo()
        locale = Locale.getDefault().language + "-" + Locale.getDefault().country
        timezone = Utils.getTimeZone()
    }

    fun resetTraits() {
        val traits = RudderTraits()
        // convert the whole traits to map and take care of the extras
        val gson =
            GsonBuilder().registerTypeAdapter(RudderTraits::class.java, RudderTraitsSerializer())
                .create()
        this.traits = Utils.convertToMap(gson.toJson(traits))
    }

    fun updateTraits(traits: RudderTraits?) {
        // if traits is null reset the traits to a new one with only anonymousId
        var traits: RudderTraits? = traits
        if (traits == null) {
            traits = RudderTraits()
        }

        // convert the whole traits to map and take care of the extras
        val gson =
            GsonBuilder().registerTypeAdapter(RudderTraits::class.java, RudderTraitsSerializer())
                .create()
        val traitsMap: MutableMap<String, Any?> = Utils.convertToMap(gson.toJson(traits))
        val existingId = this.traits!!["id"] as String?
        val newId = traitsMap["id"] as String?

        // If a user is already loggedIn and then a new user tries to login
        if (existingId != null && newId != null && existingId != newId) {
            this.traits = traitsMap
            resetExternalIds()
            return
        }

        // update traits object here
        this.traits!!.putAll(traitsMap)
    }

    fun updateAnonymousIdTraits() {
        traits!!["anonymousId"] = anonymousId
    }

    fun persistTraits() {
        // persist updated traits to sharedPreference
        try {
            if (RudderClient.getApplication() != null) {
                val preferenceManger: RudderPreferenceManager =
                    RudderPreferenceManager.getInstance(RudderClient.getApplication())
                preferenceManger.saveTraits(Gson().toJson(traits))
            }
        } catch (ex: NullPointerException) {
            RudderLogger.logError(ex)
        }
    }

    fun getTraits(): Map<String, Any?>? {
        return traits
    }

    fun updateTraitsMap(traits: MutableMap<String, Any?>?) {
        this.traits = traits
    }

    val deviceId: String
        get() = deviceInfo!!.deviceId

    // set the push token as passed by the developer
    fun putDeviceToken(token: String?) {
        if (token != null && !token.isEmpty()) {
            deviceInfo!!.setToken(token)
        }
    }

    // set the values provided by the user
    fun updateWithAdvertisingId(advertisingId: String?) {
        if (advertisingId == null || advertisingId.isEmpty()) {
            deviceInfo!!.isAdTrackingEnabled = false
        } else {
            deviceInfo!!.isAdTrackingEnabled = true
            deviceInfo!!.advertisingId = advertisingId
        }
    }

    fun updateDeviceWithAdId() {
        if (isOnClassPath("com.google.android.gms.ads.identifier.AdvertisingIdClient")) {
            // This needs to be done each time since the settings may have been updated.
            Thread {
                try {
                    var available = googlePlayServicesAdvertisingID
                    if (!available) {
                        available = amazonFireAdvertisingID
                    }
                    if (!available) {
                        RudderLogger.logDebug("Unable to collect advertising ID from Amazon Fire OS and Google Play Services.")
                    }
                } catch (e: Exception) {
                    RudderLogger.logError("Unable to collect advertising ID from Google Play Services or Amazon Fire OS.")
                }
            }.start()
        } else {
            RudderLogger.logDebug(
                "Not collecting advertising ID because "
                        + "com.google.android.gms.ads.identifier.AdvertisingIdClient "
                        + "was not found on the classpath."
            )
        }
    }

    // set the values if and only if the values are not set
    // if value exists, it must have been set by the developer. don't overwrite
    @get:Throws(Exception::class)
    private val googlePlayServicesAdvertisingID: Boolean
        private get() {
            if (RudderClient.getApplication() == null) {
                return false
            }
            val advertisingInfo =
                Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
                    .getMethod("getAdvertisingIdInfo", Context::class.java)
                    .invoke(null, RudderClient.getApplication())
                    ?: return false
            val isLimitAdTrackingEnabled = advertisingInfo.javaClass
                .getMethod("isLimitAdTrackingEnabled").invoke(advertisingInfo) as Boolean
            if (isLimitAdTrackingEnabled == null || isLimitAdTrackingEnabled) {
                RudderLogger.logDebug("Not collecting advertising ID because isLimitAdTrackingEnabled (Google Play Services) is true.")
                deviceInfo!!.isAdTrackingEnabled = false
                return false
            }
            if (TextUtils.isEmpty(deviceInfo!!.advertisingId)) {
                // set the values if and only if the values are not set
                // if value exists, it must have been set by the developer. don't overwrite
                deviceInfo!!.advertisingId =
                    advertisingInfo.javaClass.getMethod("getId").invoke(advertisingInfo) as String
                deviceInfo!!.isAdTrackingEnabled = true
            }
            return true
        }

    // set the values if and only if the values are not set
    // if value exists, it must have been set by the developer. don't overwrite
    @get:Throws(Exception::class)
    private val amazonFireAdvertisingID: Boolean
        private get() {
            if (RudderClient.getApplication() == null) {
                return false
            }
            val contentResolver: ContentResolver =
                RudderClient.getApplication().getContentResolver()
            val limitAdTracking = Settings.Secure.getInt(contentResolver, "limit_ad_tracking") !== 0
            if (limitAdTracking) {
                RudderLogger.logDebug("Not collecting advertising ID because limit_ad_tracking (Amazon Fire OS) is true.")
                deviceInfo!!.isAdTrackingEnabled = false
                return false
            }
            if (TextUtils.isEmpty(deviceInfo!!.advertisingId)) {
                // set the values if and only if the values are not set
                // if value exists, it must have been set by the developer. don't overwrite
                deviceInfo!!.advertisingId =
                    Settings.Secure.getString(contentResolver, "advertising_id")
                deviceInfo!!.isAdTrackingEnabled = true
            }
            return true
        }

    *//**
     * Getter method for Advertising ID
     *
     * @return The Advertising ID if available, returns null otherwise.
     *//*
    @get:Nullable
    val advertisingId: String?
        get() = if (deviceInfo == null) {
            null
        } else deviceInfo!!.advertisingId

    *//**
     * Getter method for Ad Tracking Status.
     *
     * @return true or false, depending on whether ad tracking is enabled or disabled.
     *//*
    val isAdTrackingEnabled: Boolean
        get() = if (deviceInfo == null) {
            false
        } else deviceInfo!!.isAdTrackingEnabled

    *//**
     * @return ExternalIds for the current session
     *//*
    @Nullable
    fun getExternalIds(): List<MutableMap<String, Any?>>? {
        return externalIds
    }

    fun updateExternalIds(@NonNull externalIds: List<MutableMap<String, Any?>>) {
        // update local variable
        if (this.externalIds == null) {
            this.externalIds = ArrayList()
            this.externalIds.addAll(externalIds)
            return
        }
        for (newExternalId in externalIds) {
            val newExternalIdType = newExternalId["type"] as String?
            var typeAlreadyExists = false
            if (newExternalIdType != null) {
                for (existingExternalId in this.externalIds!!) {
                    val existingExternalIdType = existingExternalId["type"] as String?
                    if (existingExternalIdType != null && existingExternalIdType == newExternalIdType) {
                        typeAlreadyExists = true
                        existingExternalId["id"] = newExternalId["id"]
                    }
                }
                if (!typeAlreadyExists) {
                    this.externalIds!!.add(newExternalId)
                }
            }
        }
    }

    fun persistExternalIds() {
        // persist updated externalIds to shared preferences
        try {
            if (RudderClient.getApplication() != null) {
                val preferenceManger: RudderPreferenceManager =
                    RudderPreferenceManager.getInstance(RudderClient.getApplication())
                preferenceManger.saveExternalIds(Gson().toJson(externalIds))
            }
        } catch (ex: NullPointerException) {
            RudderLogger.logError(ex)
        }
    }

    fun resetExternalIds() {
        externalIds = null
        // reset externalIds from shared preferences
        try {
            if (RudderClient.getApplication() != null) {
                val preferenceManger: RudderPreferenceManager =
                    RudderPreferenceManager.getInstance(RudderClient.getApplication())
                preferenceManger.clearExternalIds()
            }
        } catch (ex: NullPointerException) {
            RudderLogger.logError(ex)
        }
    }

    fun setCustomContexts(customContexts: Map<String, Any>?) {
        if (customContexts == null) return
        if (customContextMap == null) customContextMap = HashMap()
        customContextMap!!.putAll(customContexts)
    }

    fun copy(): RudderContext {
        val copy = RudderContext()
        copy.app = app
        if (traits != null) {
            copy.traits = HashMap(traits)
        }
        copy.libraryInfo = libraryInfo
        copy.osInfo = osInfo
        copy.screenInfo = screenInfo
        copy.userAgent = userAgent
        copy.locale = locale
        copy.deviceInfo = deviceInfo
        copy.networkInfo = networkInfo
        copy.timezone = timezone
        if (externalIds != null) {
            copy.externalIds = ArrayList(externalIds)
        }
        return copy
    }

    companion object {
        @Transient
        var anonymousId: String? = null
            private set

        fun updateAnonymousId(@NonNull anonymousId: String?) {
            Companion.anonymousId = anonymousId
        }
    }*/
}