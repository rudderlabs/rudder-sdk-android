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

    private const val PRIMARY_INSTANCE_NAME = "primary"
    private const val SECONDARY_INSTANCE_NAME = "secondary"

    private var _rudderAnalytics: Analytics? = null
    private var _rudderAnalyticsSecondary: Analytics? = null
    private var _rudderReporter: RudderReporter? = null

    fun initialize(application: Application, listener: InitializationListener? = null) {
        _rudderAnalytics = RudderAnalytics(
            writeKey = WRITE_KEY,
            instanceName = PRIMARY_INSTANCE_NAME,
            initializationListener = { success, message ->
                listener?.onAnalyticsInitialized(PRIMARY_INSTANCE_NAME, success, message)
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
        _rudderAnalyticsSecondary = RudderAnalytics(
            writeKey = WRITE_KEY_SECONDARY,
            instanceName = SECONDARY_INSTANCE_NAME,
            initializationListener = { success, message ->
                listener?.onAnalyticsInitialized(SECONDARY_INSTANCE_NAME, success, message)
            },
            configuration = ConfigurationAndroid(
                application = application,
                GsonAdapter(),
                dataPlaneUrl = DATA_PLANE_URL_SECONDARY,
                controlPlaneUrl = CONTROL_PLANE_URL_SECONDARY,
                trackLifecycleEvents = true,
                recordScreenViews = true,
            )
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
    }

    val primaryAnalytics: Analytics
        get() = _rudderAnalytics ?: throw IllegalStateException(
            "Rudder Analytics Primary not " + "initialized"
        )

    val secondaryAnalytics: Analytics
        get() = _rudderAnalyticsSecondary ?: throw IllegalStateException(
            "Rudder Analytics " + "Secondary" + " not initialized"
        )


    fun getReporter(): RudderReporter? {
        return _rudderReporter
    }

    fun interface InitializationListener {

        fun onAnalyticsInitialized(instanceName: String, success: Boolean, message: String?)
    }

}
