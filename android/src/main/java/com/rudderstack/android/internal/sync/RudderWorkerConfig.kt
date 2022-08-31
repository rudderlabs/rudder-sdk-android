/*
 * Creator: Debanjan Chatterjee on 29/08/22, 12:32 PM Last modified: 29/08/22, 12:32 PM
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

package com.rudderstack.android.internal.sync

import com.rudderstack.core.Logger
import com.rudderstack.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService

internal data class RudderWorkerConfig(
    val writeKey : String,
    val useContentProvider: Boolean,
    val dataPlaneUrl : String?,
    val controlPlaneUrl : String?,
    val jsonAdapter: JsonAdapter,
    val logger : Logger,
    val processName: String?= null,
    val networkExecutorService: ExecutorService? = null
)
