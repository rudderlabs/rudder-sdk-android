package com.rudderstack.android.sampleapp.mainview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rudderstack.android.utilities.applyConfigurationAndroid
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils
import com.rudderstack.android.sampleapp.analytics.RudderAnalyticsUtils.analytics
import com.rudderstack.android.utilities.endSession
import com.rudderstack.android.utilities.startSession
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOption
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

    private var _rudderReporter = RudderAnalyticsUtils.reporter

    private val _loggingInterceptor by lazy {
        object : Plugin {
            private var _writeKey: String? = null
            override fun setup(analytics: Analytics) {
                _writeKey = analytics.writeKey
            }
            private var logsName: String = "Sample app-$_writeKey"

            override fun intercept(chain: Plugin.Chain): Message {
                val msg = chain.message()
                _state.update { state ->
                    state.copy(
                        logDataList = state.logDataList + LogData(
                            Date(), "from $logsName, msg: $msg"
                        )
                    )
                }
                return chain.proceed(msg)
            }


        }
    }

    init {
        analytics.addPlugin(_loggingInterceptor)
    }
    private var extCount = 1

    internal fun onEventClicked(analytics: AnalyticsState) {
        val log = when (analytics) {
            AnalyticsState.ShutDownAnalytics -> {
                RudderAnalyticsUtils.analytics.shutdown()
                "Rudder Analytics is shutting down. Init again if needed. This might take a second"
            }

            AnalyticsState.TrackEvent -> {
                RudderAnalyticsUtils.analytics.track(
                    eventName = "Track at ${Date()}",
                    trackProperties = TrackProperties("key1" to "prop1", "key2" to "prop2"),
                    options = RudderOption().putIntegration("firebase", false)
                        .putExternalId(
                            "fb_id","1234"
                        )
                )
                "Track message sent"
            }

            AnalyticsState.IdentifyEvent -> {
                RudderAnalyticsUtils.analytics.identify(
                    userId = "some_user_id", traits = IdentifyTraits("trait1" to "some_trait"),
                    options = RudderOption().putExternalId("test_ext_id_key_$extCount", "test_val_$extCount")

                )
                ++ extCount
                "Identify called"
            }

            AnalyticsState.AliasEvent -> {
                RudderAnalyticsUtils.analytics.alias(newId = "user_new_id")
                "Alias called"
            }

            AnalyticsState.GroupEvent -> {
                RudderAnalyticsUtils.analytics.group(
                    groupId = "group_id",
                    groupTraits = GroupTraits("g_t1" to "t-1", "g_t2" to "t-2"),
                )
                "Group called"
            }

            AnalyticsState.ScreenEvent -> {
                RudderAnalyticsUtils.analytics.screen(
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
                RudderAnalyticsUtils.analytics.optOut(RudderAnalyticsUtils.analytics.isOptedOut)
                "OPT ${if (RudderAnalyticsUtils.analytics.isOptedOut) "out" else "in"} pressed"
            }

            AnalyticsState.ForceFlush -> {
                RudderAnalyticsUtils.analytics.flush()
                "Forcing a flush"
            }

            AnalyticsState.EnableAutoTracking -> {
                RudderAnalyticsUtils.analytics.applyConfigurationAndroid {
                    copy(trackAutoSession = true)
                }
                "Auto tracking enabled"
            }

            AnalyticsState.DisableAutoTracking -> {
                RudderAnalyticsUtils.analytics.applyConfigurationAndroid {
                    copy(trackAutoSession = false)
                }
                "Auto tracking disabled"
            }

            AnalyticsState.StartManualSession -> {
                RudderAnalyticsUtils.analytics.startSession()
                "Manual Session Started"
            }

            AnalyticsState.EndSession -> {
                RudderAnalyticsUtils.analytics.endSession()
                "Session Ended"
            }
            AnalyticsState.SendError -> {
                _rudderReporter?.errorClient?.leaveBreadcrumb("Error BC")
                _rudderReporter?.errorClient?.addMetadata("Error MD", "md_key", "md_value")
                _rudderReporter?.errorClient?.notify(Exception("Non Fatal Exception"))
                "Sending an error"
            }
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
        analytics.removePlugin(_loggingInterceptor)
        super.onCleared()
    }
}
