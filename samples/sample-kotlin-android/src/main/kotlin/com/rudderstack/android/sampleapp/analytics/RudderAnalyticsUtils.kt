package com.rudderstack.android.sampleapp.analytics

import android.app.Application
import com.rudderstack.android.BuildConfig
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.RudderAnalytics
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.DefaultRudderReporter
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.RudderReporter
import com.rudderstack.core.Analytics
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter

object RudderAnalyticsUtils {

    private var _rudderAnalytics: Analytics? = null
    private var _rudderReporter: RudderReporter? = null
    private var _listener: InitializationListener? = null

    fun initialize(application: Application) {
        _rudderAnalytics = RudderAnalytics(
            writeKey = WRITE_KEY,
            initializationListener = { success, message ->
                _listener?.onAnalyticsInitialized(success, message)
            },
            configuration = ConfigurationAndroid(
                application = application,
                GsonAdapter(),
                dataPlaneUrl = DATA_PLANE_URL,
                controlPlaneUrl = CONTROL_PLANE_URL,
                trackLifecycleEvents = true,
                recordScreenViews = true,
            )
        )
        _rudderReporter = DefaultRudderReporter(
            context = application,
            baseUrl = BASE_URL,
            configuration = Configuration(
                LibraryMetadata(
                    name = "android",
                    sdkVersion = BuildConfig.LIBRARY_PACKAGE_NAME,
                    versionCode = BuildConfig.LIBRARY_VERSION_NAME,
                    writeKey = WRITE_KEY
                )
            ),
            JacksonAdapter()
        )
    }

    fun getInstance(): Analytics? {
        return _rudderAnalytics
    }

    fun getReporter(): RudderReporter? {
        return _rudderReporter
    }

    fun setInitializationListener(listener: InitializationListener) {
        _listener = listener
    }

    fun interface InitializationListener {

        fun onAnalyticsInitialized(success: Boolean, message: String?)
    }

}
