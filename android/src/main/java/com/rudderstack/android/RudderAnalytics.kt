package com.rudderstack.android

import com.rudderstack.android.storage.AndroidStorageImpl
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
            instance ?: Analytics(
                writeKey = writeKey,
                configuration = configuration,
                dataUploadService = dataUploadService,
                configDownloadService = configDownloadService,
                storage = storage,
                initializationListener = initializationListener,
                shutdownHook = { onShutdown() }
            ).apply {
                startup()
            }.also {
                instance = it
            }
        }
    }
}
