/*
 * Creator: Debanjan Chatterjee on 05/12/23, 12:04 pm Last modified: 05/12/23, 12:04 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

@file:JvmName("TestAnalyticsProvider")
package com.vagabond.testcommon

import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.Plugin
import com.rudderstack.core.Storage
import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.models.Message
import com.rudderstack.models.RudderServerConfig
import com.rudderstack.rudderjsonadapter.JsonAdapter

private const val DUMMY_WRITE_KEY = "DUMMY_WRITE_KEY"
private var currentTestPlugin : Plugin? = null
private var inputs = listOf<Message>()
private val inputVerifyPlugin = Plugin { chain ->
    chain.proceed(chain.message().also {
        inputs += it.copy()
    })
}

fun generateTestAnalytics(jsonAdapter: JsonAdapter): Analytics {
    return generateTestAnalytics(jsonAdapter,Configuration(
        shouldVerifySdk = false))
}
fun generateTestAnalytics(jsonAdapter: JsonAdapter, mockConfiguration: Configuration,
                          configDownloadService: ConfigDownloadService =
                              MockConfigDownloadService(),
                          storage: Storage = VerificationStorage(),
                          dataUploadService: DataUploadService = TestDataUploadService(),
                          ): Analytics {
    val testingConfig = mockConfiguration.copy(
        logger = KotlinLogger,
        analyticsExecutor = TestExecutor()
    )
    return Analytics(
        DUMMY_WRITE_KEY,jsonAdapter, testingConfig, dataUploadService = dataUploadService,
        configDownloadService = configDownloadService, storage = storage
    ).also {
        it.addPlugin(inputVerifyPlugin)
    }
}
fun Analytics.testPlugin(pluginUnderTest : Plugin) {
    currentTestPlugin = pluginUnderTest
    addPlugin(pluginUnderTest)
}
fun Analytics.assertArguments(verification : Verification<List<Message>,List<Message>>) {
    busyWait(100)
    verification.assert(inputs.toList(), storage.getDataSync() ?:
    emptyList())
}
fun Analytics.assertArgument(verification: Verification<Message?, Message?>){
    busyWait(100)
    verification.assert(inputs.lastOrNull(), storage.getDataSync().lastOrNull())
}
private fun busyWait(millis: Long) {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < millis) {
        // busy wait
    }
}
fun interface Verification<IN,OUT> {
    fun assert(input : IN, output : OUT)
}