package com.rudderstack.android.sampleapp.mainview

import java.util.Date

data class MainViewModelState(
    val logDataList: List<LogData> = emptyList(),
    val state: AnalyticsState? = null,
)

data class LogData(val time: Date, val log: String)
sealed class AnalyticsState(val eventName: String) {
    object ClearAnalytics : AnalyticsState("Clear")
//    object InitializeAnalytics : AnalyticsState("Initialize")
    object ShutDownAnalytics : AnalyticsState("ShutDown")
    object TrackEvent : AnalyticsState("Track")
    object IdentifyEvent : AnalyticsState("Identify")
    object AliasEvent : AnalyticsState("Alias")
    object GroupEvent : AnalyticsState("Group")
    object ScreenEvent : AnalyticsState("Screen")
    object OptInAnalytics : AnalyticsState("Opt In/Out")
    object StartManualSession : AnalyticsState("start manual session")
    object EndSession : AnalyticsState("End Session")
    object EnableAutoTracking : AnalyticsState("Enable Auto Tracking")
    object DisableAutoTracking : AnalyticsState("Disable Auto Tracking")
    object ForceFlush : AnalyticsState("Force Flush")

    object ShutDownAnalyticsSecondary : AnalyticsState("ShutDown Secondary")
    object TrackEventSecondary : AnalyticsState("Track Secondary")
    object IdentifyEventSecondary : AnalyticsState("Identify Secondary")
    object AliasEventSecondary : AnalyticsState("Alias Secondary")
    object GroupEventSecondary : AnalyticsState("Group Secondary")
    object ScreenEventSecondary : AnalyticsState("Screen Secondary")
    object OptInAnalyticsSecondary : AnalyticsState("Opt In/Out Secondary")
    object StartManualSessionSecondary : AnalyticsState("start manual session Secondary")
    object EndSessionSecondary : AnalyticsState("End Session Secondary")
    object EnableAutoTrackingSecondary : AnalyticsState("Enable Auto Tracking Secondary")
    object DisableAutoTrackingSecondary : AnalyticsState("Disable Auto Tracking Secondary")
    object ForceFlushSecondary : AnalyticsState("Force Flush Secondary")
    object SendError : AnalyticsState("Send Error")
}
