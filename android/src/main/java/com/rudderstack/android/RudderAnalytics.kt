/*
 * Creator: Debanjan Chatterjee on 26/04/22, 3:08 PM Last modified: 26/04/22, 3:08 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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
@file:Suppress("FunctionName")
package com.rudderstack.android

import com.rudderstack.core.Analytics
import com.rudderstack.core.Logger
import com.rudderstack.core.RetryStrategy
import com.rudderstack.core.Settings
import com.rudderstack.core.internal.KotlinLogger
import com.rudderstack.models.IdentifyTraits
import com.rudderstack.rudderjsonadapter.JsonAdapter
//device info and stuff
//multi process
//bt stuff
//tv,
//work manager
fun RudderAnalytics(
    writeKey: String,
    settings: Settings,
    jsonAdapter: JsonAdapter,
    shouldVerifySdk: Boolean = true,
    sdkVerifyRetryStrategy: RetryStrategy = RetryStrategy.exponential(),
    dataPlaneUrl: String? = null,
    controlPlaneUrl: String? = null,
    logger: Logger = KotlinLogger,
    defaultTraits: IdentifyTraits? = null,
    defaultExternalIds: List<Map<String, String>>? = null,
    defaultContextMap: Map<String, Any>? = null,
    initializationListener: ((success: Boolean, message: String?) -> Unit)? = null
) : Analytics{
//    return Analytics()
    TODO()
}