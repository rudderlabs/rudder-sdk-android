package com.rudderstack.android.sampleapp.mainview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rudderstack.android.applyConfigurationAndroid
import com.rudderstack.android.sampleapp.MyApplication
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils
import com.rudderstack.android.utilities.endSession
import com.rudderstack.android.utilities.startSession
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOptions
import com.rudderstack.models.GroupTraits
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.models.ScreenProperties
import com.rudderstack.models.TrackProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(MainViewModelState())
    val state = _state.asStateFlow()

    private var _rudderAnalytics = RudderAnalyticsUtils.getInstance()
    private var _rudderReporter = RudderAnalyticsUtils.getReporter()

    private val _loggingInterceptor by lazy {
        Plugin {
            val msg = it.message()
            _state.update { state ->
                state.copy(
                    logDataList = state.logDataList + LogData(Date(), msg.toString())
                )
            }
            it.proceed(msg)
        }
    }

    init {
        _rudderAnalytics?.addPlugin(_loggingInterceptor)
    }

    internal fun onEventClicked(analytics: AnalyticsState) {
        val log = when (analytics) {
            AnalyticsState.InitializeAnalytics -> {
                if (_rudderAnalytics?.isShutdown == false) {
                    "Already initialized"
                } else {
                    _rudderAnalytics = RudderAnalyticsUtils.apply {
                        setInitializationListener { success, message ->
                            addLogData(LogData(Date(), "Init Response -> \nsuccess: ${success}, \nmessage: ${message}"))
                        }
                        initialize(getApplication<MyApplication>())
                    }.getInstance()
                    "Initializing Rudder Analytics"
                }
            }

            AnalyticsState.ShutDownAnalytics -> {
                _rudderAnalytics?.shutdown()
                "Rudder Analytics is shutting down. Init again if needed. This might take a second"
            }

            AnalyticsState.TrackEvent -> {
                _rudderAnalytics?.track(
                    eventName = "Track at ${Date()}",
                    trackProperties = TrackProperties("key1" to "prop1", "key2" to "prop2"),
                    options = RudderOptions.Builder().withIntegrations(mapOf("firebase" to false)).withExternalIds(
                        listOf(mapOf("fb_id" to "1234"))
                    ).build()
                )
                "Track message sent"
            }

            AnalyticsState.IdentifyEvent -> {
                _rudderAnalytics?.identify(
                    userId = "some_user_id",
                    traits = IdentifyTraits("trait1" to "some_trait")
                )
                "Identify called"
            }

            AnalyticsState.AliasEvent -> {
                _rudderAnalytics?.alias(newId = "user_new_id")
                "Alias called"
            }

            AnalyticsState.GroupEvent -> {
                _rudderAnalytics?.group(
                    groupId = "group_id",
                    groupTraits = GroupTraits("g_t1" to "t-1", "g_t2" to "t-2"),
                )
                "Group called"
            }

            AnalyticsState.ScreenEvent -> {
                _rudderAnalytics?.screen(
                    screenName = "some_screen",
                    category = "some_category",
                    screenProperties = ScreenProperties()
                )
                "Screen called"
            }

            AnalyticsState.ClearAnalytics -> {
                _state.update { state ->
                    state.copy(
                        logDataList = emptyList()
                    )
                }
                ""
            }

            AnalyticsState.OptInAnalytics -> {
                _rudderAnalytics?.optOut(_rudderAnalytics?.isOptedOut == false)
                "OPT ${if (_rudderAnalytics?.isOptedOut == true) "out" else "in"} pressed"
            }

            AnalyticsState.ForceFlush -> {
                _rudderAnalytics?.flush()
                "Forcing a flush"
            }

            AnalyticsState.SendError -> {
                _rudderReporter?.errorClient?.leaveBreadcrumb("Error BC")
                _rudderReporter?.errorClient?.addMetadata("Error MD", "md_key", "md_value")
                _rudderReporter?.errorClient?.notify(Exception("Non Fatal Exception"))
                "Sending an error"
            }
            AnalyticsState.EnableAutoTracking -> {
                _rudderAnalytics?.applyConfigurationAndroid{
                    copy(trackAutoSession = true)
                }
                "Auto tracking enabled"
            }
            AnalyticsState.DisableAutoTracking -> {
                _rudderAnalytics?.applyConfigurationAndroid{
                    copy(trackAutoSession = false)
                }
                "Auto tracking disabled"
            }
            AnalyticsState.StartManualSession -> {
                _rudderAnalytics?.startSession()
                "Manual Session Started"
            }
            AnalyticsState.EndSession -> {
                _rudderAnalytics?.endSession()
                "Session Ended"
            }

            else -> "What's this?"
        }
        if (log.isNotEmpty())
            addLogData(LogData(Date(), log))
    }

    private fun addLogData(logData: LogData) {
        _state.update { state ->
            state.copy(
                logDataList = state.logDataList + logData
            )
        }
    }

    override fun onCleared() {
        _rudderAnalytics?.removePlugin(_loggingInterceptor)
        super.onCleared()
    }
}
