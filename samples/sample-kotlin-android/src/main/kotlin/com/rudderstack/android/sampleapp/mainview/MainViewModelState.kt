package com.rudderstack.android.sampleapp.mainview

import java.util.Date

data class MainViewModelState(
    val logDataList: List<LogData> = emptyList(),
    val state: AnalyticsState? = null,
)

data class LogData(val time: Date, val log: String)
sealed class AnalyticsState(val eventName: String) {
    object ClearAnalytics : AnalyticsState("Clear")
    object InitializeAnalytics : AnalyticsState("Initialize")
    object ShutDownAnalytics : AnalyticsState("ShutDown")
    object TrackEvent : AnalyticsState("Track")
    object IdentifyEvent : AnalyticsState("Identify")
    object AliasEvent : AnalyticsState("Alias")
    object GroupEvent : AnalyticsState("Group")
    object ScreenEvent : AnalyticsState("Screen")
    object OptInAnalytics : AnalyticsState("Opt In/Out")
    object ForceFlush : AnalyticsState("Force Flush")
    object SendError : AnalyticsState("Send Error")
}
