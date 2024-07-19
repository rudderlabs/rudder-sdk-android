package com.rudderstack.core.internal

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.models.Message
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.web.HttpInterceptor
import com.rudderstack.web.HttpResponse
import com.rudderstack.web.WebService
import com.rudderstack.web.WebServiceFactory
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class DataUploadServiceImpl @JvmOverloads constructor(
    private val writeKey: String,
    webService: WebService? = null,
) : DataUploadService {

    override lateinit var analytics: Analytics

    private val encodedWriteKey: AtomicReference<String?> = AtomicReference()
    private val webService: AtomicReference<WebService?> = AtomicReference(webService)
    private val currentConfigurationAtomic = AtomicReference<Configuration?>()
    private var headers = mutableMapOf<String, String>()

    private val _isPaused = AtomicBoolean(false)
    private val currentConfiguration
        get() = currentConfigurationAtomic.get()

    private val interceptor = HttpInterceptor {
        if (headers.isNotEmpty()) {
            synchronized(this) {
                headers.forEach { (key, value) ->
                    it.setRequestProperty(key, value)
                }
            }
        }
        it
    }

    private fun Configuration.initializeWebService() {
        if (webService.get() == null) webService.set(
            WebServiceFactory.getWebService(
                dataPlaneUrl, jsonAdapter, executor = networkExecutor
            ).also {
                it.setInterceptor(interceptor)
            }
        )
    }

    override fun addHeaders(headers: Map<String, String>) {
        synchronized(this) {
            this.headers += headers
        }
    }

    //    private val configurationState: State<Configuration> =
    override fun upload(
        data: List<Message>,
        extraInfo: Map<String, String>?,
        callback: (response: HttpResponse<out Any>) -> Unit
    ) {
        if (_isPaused.get()) {
            callback(HttpResponse(-1, null, "Upload Service is Paused", null))
            return
        }

        currentConfiguration?.apply {
            if (networkExecutor.isShutdown || networkExecutor.isTerminated) return
            val batchBody = createBatchBody(data, extraInfo)
            webService.get()?.post(
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to String.format(Locale.US, "Basic %s", encodedWriteKey),
                ), null, batchBody, "v1/batch", String::class.java, gzipEnabled
            ) {
                callback.invoke(it)
            }
        }
    }


    override fun uploadSync(data: List<Message>, extraInfo: Map<String, String>?): HttpResponse<out Any>? {
        if (_isPaused.get()) return null
        return currentConfiguration?.let { config ->
            val batchBody = config.createBatchBody(data, extraInfo)
            webService.get()?.post(
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to String.format(Locale.US, "Basic %s", encodedWriteKey)
                ), null, batchBody, "v1/batch",
                String::class.java, isGzipEnabled = config.gzipEnabled
            )?.get()
        }
    }

    private fun Configuration.createBatchBody(
        data: List<Message>,
        extraInfo: Map<String, String>?
    ): String? {
        val sentAt = RudderUtils.timeStamp
        return mapOf(
            "sentAt" to sentAt, "batch" to data.map {
                it.sentAt = sentAt
                it
            }
        ).let {
            if (extraInfo.isNullOrEmpty()) it else it + extraInfo //adding extra info data
        }.let {
            jsonAdapter.writeToJson(it, object : RudderTypeAdapter<Map<String, Any>>() {})
        }
    }

    override fun updateConfiguration(configuration: Configuration) {
        encodedWriteKey.set(configuration.base64Generator.generateBase64(writeKey))
        configuration.initializeWebService()
        currentConfigurationAtomic.set(configuration)
    }

    override fun pause() {
        _isPaused.set(true)
    }

    override fun resume() {
        _isPaused.set(false)
    }

    override fun shutdown() {
        webService.get()?.shutdown()
        webService.set(null)
        currentConfigurationAtomic.set(null)
        encodedWriteKey.set(null)
    }
}
