/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
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

package com.rudderstack.android.web

import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.web.internal.WebServiceImpl
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A Singleton factory for producing default implementation of WebService.
 * For multiple base urls or executors, multiple WebService objects can be created.
 */
object WebServiceFactory {
    /**
     * Creates a default implementation of WebService
     *
     * @param baseUrl a valid base url for this web service
     * @param jsonAdapter A JsonAdapter implementation for serialization and deserialization
     * @param defaultTimeout Timeout in ms
     * @param executor The Executor for the web service calls
     * @return A WebService implementation
     */
    fun getWebService(
        baseUrl: String,
        jsonAdapter: JsonAdapter,
        defaultTimeout: Int = 10000,
        executor: ExecutorService = Executors.newCachedThreadPool(),
    ): WebService = WebServiceImpl(baseUrl, jsonAdapter, defaultTimeout, executor)
}
