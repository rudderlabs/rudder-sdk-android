package com.rudderstack.android

import android.app.Application
import android.util.Log
import com.rudderstack.android.internal.infrastructure.ActivityBroadcasterPlugin
import com.rudderstack.android.internal.infrastructure.AppInstallUpdateTrackerPlugin
import com.rudderstack.android.internal.infrastructure.LifecycleObserverPlugin
import com.rudderstack.android.internal.plugins.ExtractStatePlugin
import com.rudderstack.android.internal.plugins.FillDefaultsPlugin
import com.rudderstack.android.internal.plugins.PlatformInputsPlugin
import com.rudderstack.android.internal.infrastructure.ReinstatePlugin
import com.rudderstack.android.internal.plugins.SessionPlugin
import com.rudderstack.android.internal.states.ContextState
import com.rudderstack.android.internal.states.UserSessionState
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.android.utilities.onShutdown
import com.rudderstack.android.utilities.startup
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.holder.associateState
import com.rudderstack.core.holder.retrieveState
import com.rudderstack.models.MessageContext
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.models.createContext
import com.rudderstack.models.traits
import com.rudderstack.models.updateWith
import com.rudderstack.core.Storage

/**
 * Singleton class for RudderAnalytics to manage the analytics instance.
 *
 * This class ensures that only one instance of the Analytics object is created.
 */
class RudderAnalytics private constructor() {

    companion object {

        @Volatile
        private var instance: Analytics? = null

        /**
         * Returns the singleton instance of [Analytics], creating it if necessary.
         *
         * @param writeKey The write key for authentication.
         * @param configuration The configuration settings for Android.
         * @param storage The storage implementation for storing data. Defaults to [AndroidStorageImpl].
         * @param dataUploadService The service responsible for uploading data. Defaults to null.
         * @param configDownloadService The service responsible for downloading configuration. Defaults to null.
         * @param initializationListener A listener for initialization events. Defaults to null.
         * @return The singleton instance of [Analytics].
         */
        @JvmStatic
        @JvmOverloads
        fun getInstance(
            writeKey: String,
            jsonAdapter: JsonAdapter,
            application: Application,
            configurationScope: (ConfigurationAndroidInitializationScope.() -> Unit)?= null,
            dataUploadService: DataUploadService? = null,
            configDownloadService: ConfigDownloadService? = null,
            storage: AndroidStorage = AndroidStorageImpl(
                application,
                writeKey = writeKey,
                useContentProvider = ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER
            ),
            initializationListener: ((success: Boolean, message: String?) -> Unit)? = null
        ) = instance ?: synchronized(this) {
            instance ?: Analytics(writeKey,
                jsonAdapter,
                application.initialConfigurationAndroid(storage).let{
                    if(configurationScope == null) it
                    else ConfigurationAndroidInitializationScope(it).let {
                        it.configurationScope()
                        Log.e("omg tle", it.trackLifecycleEvents.toString())
                        it.build()
                    }
                },
                dataUploadService,
                configDownloadService,
                storage,
                initializationListener = initializationListener,
                shutdownHook = {
                    onShutdown()
                }).apply {
                startup()
            }.also {
                instance = it
            }
        }
    }
}
