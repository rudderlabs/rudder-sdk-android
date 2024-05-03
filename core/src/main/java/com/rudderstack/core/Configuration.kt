/*
 * Creator: Debanjan Chatterjee on 30/12/21, 1:26 PM Last modified: 29/12/21, 5:30 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.core

import java.util.concurrent.ExecutorService

interface Configuration {
    val options: RudderOptions
    val flushQueueSize: Int
    val maxFlushInterval: Long
    val shouldVerifySdk: Boolean
    val gzipEnabled: Boolean
    val sdkVerifyRetryStrategy: RetryStrategy
    val dataPlaneUrl: String
    val controlPlaneUrl: String
    val logger: Logger
    val analyticsExecutor: ExecutorService
    val networkExecutor: ExecutorService
    val base64Generator: Base64Generator
}