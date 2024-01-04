/*
 * Creator: Debanjan Chatterjee on 29/07/22, 12:08 PM Last modified: 29/07/22, 12:08 PM
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

package com.rudderstack.android.sampleapp

import android.app.Application
import android.content.res.AssetManager
import android.util.Log
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.RudderAnalytics
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.DefaultRudderReporter
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.RudderReporter
import com.rudderstack.core.Analytics
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
//import com.rudderstack.rudd.JacksonAdapter
import java.io.IOException
import java.io.InputStream
import java.util.*

class MyApplication : Application() {
    companion object {
        private lateinit var _rudderAnalytics: Analytics
        val rudderAnalytics
            get() = _rudderAnalytics
        private lateinit var _rudderReporter: RudderReporter
        val reporter
            get() = _rudderReporter
    }

    var initializationCallback: ((InitializationResponse) -> Unit)? = null
    override fun onCreate() {
        super.onCreate()
        initializeRudderAnalytics(this)
    }

    internal fun initializeRudderAnalytics(application: Application) {
        _rudderAnalytics = RudderAnalytics(
            properties.getProperty("writeKey"),
            ConfigurationAndroid(application, jsonAdapter = JacksonAdapter(), flushQueueSize = 15,
                maxFlushInterval = 50_000L,dataPlaneUrl = properties.getProperty("dataPlaneUrl"),
                controlPlaneUrl = properties.getProperty("controlPlaneUrl"),
                recordScreenViews = true)
            )
            { success, message ->
                initializationCallback?.invoke(InitializationResponse(success, message))
            }
        _rudderReporter =
            DefaultRudderReporter(
                application,"https://hosted.rudderlabs.com",
                Configuration(
                    LibraryMetadata("android", BuildConfig.VERSION_NAME, BuildConfig
                    .VERSION_CODE.toString(), "write-key")
                ),
                JacksonAdapter()
                )

    }

    private val properties by lazy {
        Log.e("sample", "looking for properties")
        Properties().apply {
            try {
                //access to the folder ‘assets’
                val am: AssetManager = getAssets()
                //opening the file
                val inputStream: InputStream = am.open("local.properties")
                //loading of the properties
                load(inputStream)
            } catch (e: IOException) {
                Log.e("PropertiesReader", e.toString())
            }
        }
    }

    data class InitializationResponse(val success: Boolean, val message: String?)
}