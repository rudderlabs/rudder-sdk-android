package com.rudderstack.android

import com.rudderstack.android.storage.AndroidStorageImpl
import com.rudderstack.android.utilities.onShutdown
import com.rudderstack.android.utilities.setAnonymousId
import com.rudderstack.android.utilities.startup
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.Storage

/**
 * Singleton class for RudderAnalytics to manage the analytics instance.
 *
 * This class ensures that only one instance of the Analytics object is created.
 */
class RudderAnalytics private constructor() {

    companion object {

        private var anonymousId: String? = null

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
            configuration: ConfigurationAndroid,
            storage: Storage = AndroidStorageImpl(
                configuration.application,
                writeKey = writeKey,
                useContentProvider = ConfigurationAndroid.Defaults.USE_CONTENT_PROVIDER
            ),
            dataUploadService: DataUploadService? = null,
            configDownloadService: ConfigDownloadService? = null,
            initializationListener: ((success: Boolean, message: String?) -> Unit)? = null,
        ) = instance ?: synchronized(this) {
            if (instance != null) {
                instance
            } else {
                // Similarly we can configure all other values that we like e.g., deviceToken, advertisingId etc
                var updatedConfiguration = configuration.copy()
                this.anonymousId?.let {
                    updatedConfiguration = configuration.copy(anonymousId = it)
                }
                Analytics(
                    writeKey = writeKey,
                    configuration = updatedConfiguration,
                    dataUploadService = dataUploadService,
                    configDownloadService = configDownloadService,
                    storage = storage,
                    initializationListener = initializationListener,
                    shutdownHook = { onShutdown() }
                ).apply {
                    startup()
                }.also {
                    instance = it
                    this.anonymousId?.let { manualAnonymousId ->
                        instance?.setAnonymousId(manualAnonymousId)
                    }
                }
            }
        }

        // User can call this method before SDK init
        fun setAnonymousId(anonymousId: String) {
            if (instance == null) {
                this.anonymousId = anonymousId
            } else {
                instance?.setAnonymousId(anonymousId)
            }
        }
    }
}
