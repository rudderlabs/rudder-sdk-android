package com.rudderstack.core.internal

import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.models.RudderServerConfig
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import com.rudderstack.web.WebServiceFactory
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference

internal class ConfigDownloadServiceImpl @JvmOverloads constructor(
    private val writeKey: String,
    webService: WebService? = null,
) : ConfigDownloadService {

    override lateinit var analytics: Analytics

    private val downloadSequence = CopyOnWriteArrayList<Boolean>()
    private var listeners = CopyOnWriteArrayList<ConfigDownloadService.Listener>()
    private val controlPlaneWebService: AtomicReference<WebService?> =
        AtomicReference<WebService?>(webService)
    private val encodedWriteKey: AtomicReference<String?> = AtomicReference()
    private val currentConfigurationAtomic = AtomicReference<Configuration?>()
    private val currentConfiguration
        get() = currentConfigurationAtomic.get()

    private fun Configuration.initializeWebServiceIfRequired() {
        if (controlPlaneWebService.get() == null) controlPlaneWebService.set(
            WebServiceFactory.getWebService(
                controlPlaneUrl, jsonAdapter = jsonAdapter, executor = networkExecutor
            )
        )
    }

    private var ongoingConfigFuture: Future<HttpResponse<RudderServerConfig>>? = null
    private var lastRudderServerConfig: RudderServerConfig? = null
    private var lastErrorMsg: String? = null

    override fun download(
        callback: (success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit
    ) {
        currentConfiguration?.apply {
            networkExecutor.submit {
                with(sdkVerifyRetryStrategy) {
                    analytics.perform({
                        ongoingConfigFuture = controlPlaneWebService.get()?.get(
                            mapOf(
                                "Content-Type" to "application/json",
                                "Authorization" to String.format(
                                    Locale.US,
                                    "Basic %s",
                                    encodedWriteKey
                                )
                            ), mapOf(
                                "p" to this.storage.libraryPlatform,
                                "v" to this.storage.libraryVersion,
                                "bv" to this.storage.libraryOsVersion
                            ), "sourceConfig", RudderServerConfig::class.java
                        )
                        val response = ongoingConfigFuture?.get()
                        lastRudderServerConfig = response?.body
                        lastErrorMsg = response?.errorBody ?: response?.error?.message
                        return@perform (ongoingConfigFuture?.get()?.status ?: -1) == 200 //TODO -
                        // if the status is excluded from retry 429 or 500-599
                    }) {
                        listeners.forEach { listener ->
                            listener.onDownloaded(it)
                        }
                        downloadSequence.add(it)
                        callback.invoke(it, lastRudderServerConfig, lastErrorMsg)
                    }
                }
            }
        }

    }

    override fun addListener(listener: ConfigDownloadService.Listener, replay: Int) {
        val replayCount = replay.coerceAtLeast(0)
        replayConfigDownloadHistory(replayCount, listener)
        listeners += listener
    }

    private fun replayConfigDownloadHistory(
        replayCount: Int,
        listener: ConfigDownloadService.Listener
    ) {
        for (i in (downloadSequence.size - replayCount).coerceAtLeast(0) until downloadSequence.size) {
            listener.onDownloaded(downloadSequence[i])
        }
    }

    override fun removeListener(listener: ConfigDownloadService.Listener) {
        listeners.remove(listener)
    }

    override fun updateConfiguration(configuration: Configuration) {
        encodedWriteKey.set(configuration.base64Generator.generateBase64(writeKey))
        configuration.initializeWebServiceIfRequired()
        currentConfigurationAtomic.set(configuration)
    }

    override fun shutdown() {
        listeners.clear()
        downloadSequence.clear()
        controlPlaneWebService.get()?.shutdown()
        controlPlaneWebService.set(null)
        currentConfigurationAtomic.set(null)
        encodedWriteKey.set(null)
        try {
            ongoingConfigFuture?.cancel(true)
        } catch (ex: Exception) {
            // Ignore the exception
        }
    }
}
