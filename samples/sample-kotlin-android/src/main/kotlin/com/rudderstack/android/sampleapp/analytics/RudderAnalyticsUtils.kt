package com.rudderstack.android.sampleapp.analytics

import android.app.Application
import com.rudderstack.android.BuildConfig
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.RudderAnalytics.Companion.getInstance
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.DefaultRudderReporter
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.RudderReporter
import com.rudderstack.android.sampleapp.analytics.workmanger.SampleWorkManagerPlugin
import com.rudderstack.core.Analytics
import com.rudderstack.core.RudderLogger
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter

object RudderAnalyticsUtils {

    private var _rudderAnalytics: Analytics? = null
    val analytics: Analytics
        get() = _rudderAnalytics ?: throw IllegalStateException(
            "Rudder Analytics Primary not " + "initialized"
        )

    private var _rudderReporter: RudderReporter? = null
    val reporter: RudderReporter? get() = _rudderReporter

    fun initialize(application: Application, listener: InitializationListener? = null) {
        //wen add work manager support to this instance
        _rudderAnalytics = getInstance(
            writeKey = WRITE_KEY,
            application = application,
            jsonAdapter = GsonAdapter(),
            initializationListener = { success, message ->
                listener?.onAnalyticsInitialized(WRITE_KEY, success, message)
            },
            configurationScope = {
                dataPlaneUrl = DATA_PLANE_URL
                controlPlaneUrl = CONTROL_PLANE_URL
                trackLifecycleEvents = true
                recordScreenViews = true
                isPeriodicFlushEnabled = true
                autoCollectAdvertisingId = true
                trackAutoSession = true
                logLevel = RudderLogger.LogLevel.DEBUG
            }
        )
        _rudderReporter = DefaultRudderReporter(
            context = application, baseUrl = METRICS_BASE_URL, configuration = Configuration(
                LibraryMetadata(
                    name = "android",
                    sdkVersion = BuildConfig.LIBRARY_PACKAGE_NAME,
                    versionCode = BuildConfig.LIBRARY_VERSION_NAME,
                    writeKey = WRITE_KEY
                )
            ), JacksonAdapter()
        )
        _rudderAnalytics?.addInfrastructurePlugin(SampleWorkManagerPlugin())
    }

    fun interface InitializationListener {

        fun onAnalyticsInitialized(writeKey: String, success: Boolean, message: String?)
    }

}
