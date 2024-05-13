package com.rudderstack.android.sampleapp.analytics

import android.app.Application
import com.rudderstack.android.BuildConfig
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.createInstance
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.DefaultRudderReporter
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.RudderReporter
import com.rudderstack.android.sampleapp.analytics.workmanger.SampleWorkManagerPlugin
import com.rudderstack.core.Analytics
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter

object RudderAnalyticsUtils {


    private var _rudderAnalytics: Analytics? = null
    private var _rudderAnalyticsSecondary: Analytics? = null
    private var _rudderReporter: RudderReporter? = null

    fun initialize(application: Application, listener: InitializationListener? = null) {
        //wen add work manager support to this instance
        _rudderAnalytics = createPrimaryAnalyticsInstanceWithWorkerSupport(application, listener)
        _rudderAnalyticsSecondary = createSecondaryInstance(listener, application)
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
        _rudderAnalytics?.initializeWorkManager()
    }

    private fun createSecondaryInstance(
        listener: InitializationListener?,
        application: Application
    ) = createInstance(writeKey = WRITE_KEY_SECONDARY,
        application = application,
        jsonAdapter = GsonAdapter(),

        initializationListener = { success, message ->
            listener?.onAnalyticsInitialized(WRITE_KEY_SECONDARY, success, message)
        },
        configurationInitializer = {
//                dataPlaneUrl = DATA_PLANE_URL,
//                controlPlaneUrl = CONTROL_PLANE_URL,
//                recordScreenViews = true,
            trackAutoSession = true
            dataPlaneUrl = DATA_PLANE_URL_SECONDARY
            controlPlaneUrl = CONTROL_PLANE_URL_SECONDARY
            recordScreenViews = true
            trackLifecycleEvents = true
        })

    fun createPrimaryAnalyticsInstanceWithWorkerSupport(
        application: Application,
        listener: InitializationListener? = null
    ): Analytics {
        return createInstance(writeKey = WRITE_KEY,
            application = application,
            jsonAdapter = GsonAdapter(),

            initializationListener = { success, message ->
                listener?.onAnalyticsInitialized(WRITE_KEY, success, message)
            },
            configurationInitializer = {
//                dataPlaneUrl = DATA_PLANE_URL,
//                controlPlaneUrl = CONTROL_PLANE_URL,
//                recordScreenViews = true,
                trackAutoSession = true
                dataPlaneUrl = DATA_PLANE_URL
                controlPlaneUrl = CONTROL_PLANE_URL
                recordScreenViews = true

            })
    }

    private fun Analytics.initializeWorkManager() {
        addInfrastructurePlugin(SampleWorkManagerPlugin())
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

        fun onAnalyticsInitialized(writeKey: String, success: Boolean, message: String?)
    }

}
