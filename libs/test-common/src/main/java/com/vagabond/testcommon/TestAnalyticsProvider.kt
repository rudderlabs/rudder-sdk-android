@file:JvmName("TestAnalyticsProvider")

package com.vagabond.testcommon

import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.Plugin
import com.rudderstack.core.Storage
import com.rudderstack.core.models.Message
import com.rudderstack.rudderjsonadapter.JsonAdapter

private const val DUMMY_WRITE_KEY = "DUMMY_WRITE_KEY"
private var currentTestPlugin: Plugin? = null
private var inputs = listOf<Message>()
val inputVerifyPlugin = Plugin { chain ->
    chain.proceed(chain.message().also {
        inputs += it.copy()
    })
}

fun generateTestAnalytics(jsonAdapter: JsonAdapter): Analytics {
    return generateTestAnalytics(
        Configuration(
            jsonAdapter = jsonAdapter,
            shouldVerifySdk = false
        )
    )
}

fun generateTestAnalytics(
    mockConfiguration: Configuration,
    configDownloadService: ConfigDownloadService =
        MockConfigDownloadService(),
    storage: Storage = VerificationStorage(),
    dataUploadService: DataUploadService = TestDataUploadService(),
): Analytics {
    val testingConfig = mockConfiguration
    return Analytics(
        DUMMY_WRITE_KEY, testingConfig, dataUploadService = dataUploadService,
        configDownloadService = configDownloadService, storage = storage
    ).also {
        it.addPlugin(inputVerifyPlugin)
    }
}

fun Analytics.testPlugin(pluginUnderTest: Plugin) {
    currentTestPlugin = pluginUnderTest
    addPlugin(pluginUnderTest)
}

fun Analytics.assertArguments(verification: Verification<List<Message>, List<Message>>) {
    busyWait(100)
    verification.assert(
        inputs.toList(), storage.getDataSync() ?: emptyList()
    )
}

fun Analytics.assertArgument(verification: Verification<Message?, Message?>) {
    busyWait(100)
    verification.assert(inputs.lastOrNull(), storage.getDataSync().lastOrNull())
}

private fun busyWait(millis: Long) {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < millis) {
        // busy wait
    }
}

fun interface Verification<IN, OUT> {
    fun assert(input: IN, output: OUT)
}
