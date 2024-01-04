/*
 * Creator: Debanjan Chatterjee on 28/07/22, 6:04 PM Last modified: 28/07/22, 6:04 PM
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.rudderstack.android.sampleapp.MainViewModel.Event.ALIAS
import com.rudderstack.android.sampleapp.MainViewModel.Event.CLEAR
import com.rudderstack.android.sampleapp.MainViewModel.Event.FORCE_FLUSH
import com.rudderstack.android.sampleapp.MainViewModel.Event.GROUP
import com.rudderstack.android.sampleapp.MainViewModel.Event.IDENTIFY
import com.rudderstack.android.sampleapp.MainViewModel.Event.INITIALIZE
import com.rudderstack.android.sampleapp.MainViewModel.Event.OPT_IN
import com.rudderstack.android.sampleapp.MainViewModel.Event.SCREEN
import com.rudderstack.android.sampleapp.MainViewModel.Event.SEND_ERROR
import com.rudderstack.android.sampleapp.MainViewModel.Event.SHUTDOWN
import com.rudderstack.android.sampleapp.MainViewModel.Event.TRACK
import com.rudderstack.android.sampleapp.MyApplication.Companion.reporter
import com.rudderstack.android.sampleapp.MyApplication.Companion.rudderAnalytics
import com.rudderstack.android.sampleapp.models.LogData
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOptions
import com.rudderstack.models.GroupTraits
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.models.ScreenProperties
import com.rudderstack.models.TrackProperties
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    object Event {
        internal const val CLEAR = "clear"
        internal const val INITIALIZE = "init"
        internal const val SHUTDOWN = "shutdown"
        internal const val TRACK = "track"
        internal const val IDENTIFY = "identify"
        internal const val ALIAS = "alias"
        internal const val GROUP = "group"
        internal const val SCREEN = "screen"
        internal const val OPT_IN = "opt in/out"
        internal const val FORCE_FLUSH = "force flush"
        internal const val SEND_ERROR = "send error"
    }

    private var _logState = mutableStateOf<List<LogData>>(listOf())
    val logState: State<List<LogData>>
        get() = _logState
    private var _logDataList = listOf<LogData>()

    private val _loggingInterceptor by lazy {
        Plugin {
            val msg = it.message()
            _logDataList = _logDataList + LogData(Date(), msg.toString())
            _logState.value = _logDataList
            it.proceed(msg)
        }
    }

    init {
        rudderAnalytics.addPlugin(_loggingInterceptor)
        getApplication<MyApplication>().initializationCallback = ::initResponseHandler
    }

    internal fun onEventClicked(eventName: String) {
        val log = when (eventName) {
            INITIALIZE -> {
                if(!rudderAnalytics.isShutdown){
                    "Already initialized"
                }else{
                    getApplication<MyApplication>().initializeRudderAnalytics(getApplication())
                    rudderAnalytics.addPlugin(_loggingInterceptor)
                    "Initializing Rudder Analytics"
                }
            }
            SHUTDOWN -> {
                rudderAnalytics.shutdown()
                "Rudder Analytics is shutting down. Init again if needed. This might take a second"
            }
            TRACK -> {
                rudderAnalytics.track("Track at ${Date()}", trackProperties =  TrackProperties("key1" to "prop1", "key2" to "prop2"),
                options = RudderOptions.Builder().withIntegrations(mapOf("firebase" to false)).withExternalIds(
                    listOf(mapOf("fb_id" to "1234"))).build() )
                "Track message sent"
            }
            IDENTIFY -> {
                rudderAnalytics.identify("some_user_id", IdentifyTraits("trait1" to "some_trait"))
                "Identify called"

            }
            ALIAS -> {
                rudderAnalytics.alias("user_new_id")
                "Alias called"

            }
            GROUP -> {
                rudderAnalytics.group("group_id", groupTraits = GroupTraits("g_t1" to "t-1", "g_t2" to
                    "t-2"), )
                "Group called"
            }
            SCREEN -> {
                rudderAnalytics.screen("some_screen", "some_category", ScreenProperties(),)
                "Screen called"

            }
            CLEAR ->{
                _logDataList = listOf()
                _logState.value = _logDataList
                ""
            }
            OPT_IN ->{
                rudderAnalytics.optOut(!rudderAnalytics.isOptedOut)
                "OPT ${if(rudderAnalytics.isOptedOut) "out" else "in"} pressed"
            }
            FORCE_FLUSH ->{
                rudderAnalytics.forceFlush()
                "Forcing a flush"
            }
            SEND_ERROR ->{
                reporter.errorClient.leaveBreadcrumb("Error BC")
                reporter.errorClient.addMetadata("Error MD", "md_key", "md_value")
                reporter.errorClient.notify(Exception("Non Fatal Exception"))
                "Sending an error"
            }
            else -> "What's this?"
        }
        if(log.isNotEmpty())
            addLogData(LogData(Date(), log))

    }

    private fun initResponseHandler(response : MyApplication.InitializationResponse){
        addLogData(LogData(Date(), "Init Response -> \nsuccess: ${response.success}, \nmessage: ${response.message}" ))
    }
    private fun addLogData(logData: LogData){
        _logDataList = _logDataList + logData
        _logState.value = _logDataList
    }
    override fun onCleared() {
        rudderAnalytics.removePlugin(_loggingInterceptor)
        super.onCleared()
    }
}