package com.rudderstack.android.sampleapp.mainview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rudderstack.android.applyConfigurationAndroid
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils.primaryAnalytics
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils.secondaryAnalytics
import com.rudderstack.android.utilities.endSession
import com.rudderstack.android.utilities.startSession
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOptions
import com.rudderstack.models.GroupTraits
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.models.Message
import com.rudderstack.models.ScreenProperties
import com.rudderstack.models.TrackProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(MainViewModelState())
    val state = _state.asStateFlow()

    private var _rudderReporter = RudderAnalyticsUtils.getReporter()

    private val _loggingInterceptor by lazy {
        object : Plugin {
            private var _instanceName: String? = null
            override fun setup(analytics: Analytics) {
                _instanceName = analytics.instanceName
            }

            override fun intercept(chain: Plugin.Chain): Message {
                val msg = chain.message()
                _state.update { state ->
                    state.copy(
                        logDataList = state.logDataList + LogData(
                            Date(), "from $_instanceName, msg: $msg"
                        )
                    )
                }
                return chain.proceed(msg)
            }


        }
    }

    init {
        primaryAnalytics.addPlugin(_loggingInterceptor)
        secondaryAnalytics.addPlugin(_loggingInterceptor)
    }

    internal fun onEventClicked(analytics: AnalyticsState) {
        val log = when (analytics) {
            AnalyticsState.ShutDownAnalytics -> {
                primaryAnalytics.shutdown()
                "Rudder Analytics is shutting down. Init again if needed. This might take a second"
            }

            AnalyticsState.TrackEvent -> {
                primaryAnalytics.track(
                    eventName = "Track at ${Date()}",
                    trackProperties = TrackProperties("key1" to "prop1", "key2" to "prop2"),
                    options = RudderOptions.Builder().withIntegrations(mapOf("firebase" to false))
                        .withExternalIds(
                            listOf(mapOf("fb_id" to "1234"))
                        ).build()
                )
                "Track message sent"
            }

            AnalyticsState.IdentifyEvent -> {
                primaryAnalytics.identify(
                    userId = "some_user_id", traits = IdentifyTraits("trait1" to "some_trait")
                )
                "Identify called"
            }

            AnalyticsState.AliasEvent -> {
                primaryAnalytics.alias(newId = "user_new_id")
                "Alias called"
            }

            AnalyticsState.GroupEvent -> {
                primaryAnalytics.group(
                    groupId = "group_id",
                    groupTraits = GroupTraits("g_t1" to "t-1", "g_t2" to "t-2"),
                )
                "Group called"
            }

            AnalyticsState.ScreenEvent -> {
                primaryAnalytics.screen(
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
                primaryAnalytics.optOut(primaryAnalytics.isOptedOut)
                "OPT ${if (primaryAnalytics.isOptedOut) "out" else "in"} pressed"
            }

            AnalyticsState.ForceFlush -> {
                primaryAnalytics.flush()
                "Forcing a flush"
            }

            AnalyticsState.EnableAutoTracking -> {
                primaryAnalytics.applyConfigurationAndroid {
                    copy(trackAutoSession = true)
                }
                "Auto tracking enabled"
            }

            AnalyticsState.DisableAutoTracking -> {
                primaryAnalytics.applyConfigurationAndroid {
                    copy(trackAutoSession = false)
                }
                "Auto tracking disabled"
            }

            AnalyticsState.StartManualSession -> {
                primaryAnalytics.startSession()
                "Manual Session Started"
            }

            AnalyticsState.EndSession -> {
                primaryAnalytics.endSession()
                "Session Ended"
            }
            AnalyticsState.ShutDownAnalyticsSecondary -> {
                secondaryAnalytics.shutdown()
                "Rudder Analytics is shutting down. Init again if needed. This might take a second"
            }

            AnalyticsState.TrackEventSecondary -> {
                secondaryAnalytics.track(
                    eventName = "Track at ${Date()}",
                    trackProperties = TrackProperties("key1" to "prop1", "key2" to "prop2"),
                    options = RudderOptions.Builder().withIntegrations(mapOf("firebase" to false))
                        .withExternalIds(
                            listOf(mapOf("fb_id" to "1234"))
                        ).build()
                )
                "Track message sent"
            }

            AnalyticsState.IdentifyEventSecondary -> {
                secondaryAnalytics.identify(
                    userId = "some_user_id", traits = IdentifyTraits("trait1" to "some_trait")
                )
                "Identify called"
            }

            AnalyticsState.AliasEventSecondary -> {
                secondaryAnalytics.alias(newId = "user_new_id")
                "Alias called"
            }

            AnalyticsState.GroupEventSecondary -> {
                secondaryAnalytics.group(
                    groupId = "group_id",
                    groupTraits = GroupTraits("g_t1" to "t-1", "g_t2" to "t-2"),
                )
                "Group called"
            }

            AnalyticsState.ScreenEventSecondary -> {
                secondaryAnalytics.screen(
                    screenName = "some_screen",
                    category = "some_category",
                    screenProperties = ScreenProperties()
                )
                "Screen called"
            }

            AnalyticsState.OptInAnalyticsSecondary -> {
                secondaryAnalytics.optOut(primaryAnalytics.isOptedOut)
                "OPT ${if (primaryAnalytics.isOptedOut) "out" else "in"} pressed"
            }

            AnalyticsState.ForceFlushSecondary -> {
                secondaryAnalytics.flush()
                "Forcing a flush"
            }

            AnalyticsState.EnableAutoTrackingSecondary -> {
                secondaryAnalytics.applyConfigurationAndroid {
                    copy(trackAutoSession = true)
                }
                "Auto tracking enabled"
            }

            AnalyticsState.DisableAutoTrackingSecondary -> {
                secondaryAnalytics.applyConfigurationAndroid {
                    copy(trackAutoSession = false)
                }
                "Auto tracking disabled"
            }

            AnalyticsState.StartManualSessionSecondary -> {
                secondaryAnalytics.startSession()
                "Manual Session Started"
            }

            AnalyticsState.EndSessionSecondary -> {
                secondaryAnalytics.endSession()
                "Session Ended"
            }
            AnalyticsState.SendError -> {
                _rudderReporter?.errorClient?.leaveBreadcrumb("Error BC")
                _rudderReporter?.errorClient?.addMetadata("Error MD", "md_key", "md_value")
                _rudderReporter?.errorClient?.notify(Exception("Non Fatal Exception"))
                "Sending an error"
            }
            else -> "What's this?"
        }
        if (log.isNotEmpty()) addLogData(LogData(Date(), log))
    }

    private fun addLogData(logData: LogData) {
        _state.update { state ->
            state.copy(
                logDataList = state.logDataList + logData
            )
        }
    }

    override fun onCleared() {
        primaryAnalytics.removePlugin(_loggingInterceptor)
        super.onCleared()
    }
}
