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

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.Plugin
import com.rudderstack.core.copy
import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.models.Message
import com.rudderstack.rudderjsonadapter.JsonAdapter

private const val DUMMY_WRITE_KEY = "DUMMY_WRITE_KEY"
private var currentTestPlugin : Plugin? = null
private var inputs = listOf<Message>()
private var outputs = listOf<Message>()
private val inputVerifyPlugin = Plugin { chain ->
    chain.message().also {
        inputs += it.copy()
    }
}

fun generateTestAnalytics(jsonAdapter: JsonAdapter): Analytics {
    return generateTestAnalytics(Configuration(jsonAdapter, storage = VerificationStorage(),
        shouldVerifySdk = false))
}
fun generateTestAnalytics(mockConfiguration: Configuration): Analytics {
    return Analytics(
        DUMMY_WRITE_KEY, mockConfiguration.copy(
            logger = KotlinLogger
        ), TestDataUploadService(), MockConfigDownloadService()
    ).also {
        it.addPlugin(inputVerifyPlugin)
    }
}
fun Analytics.testPlugin(pluginUnderTest : Plugin) {
    currentTestPlugin = pluginUnderTest
    addPlugin(pluginUnderTest)
}
fun Analytics.assert(verification : Verification<List<Message>,List<Message>>) {
    verification.assert(inputs.toList(), currentConfiguration?.storage?.startupQueue?.toList() ?:
    emptyList())
}
fun Analytics.assert(verification: Verification<Message?, Message?>){
    verification.assert(inputs.lastOrNull(), currentConfiguration?.storage?.startupQueue?.lastOrNull())
}
fun interface Verification<IN,OUT> {
    fun assert(input : IN, output : OUT)
}