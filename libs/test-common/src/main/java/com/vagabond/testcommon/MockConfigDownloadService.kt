/*
 * Creator: Debanjan Chatterjee on 05/12/23, 4:03 pm Last modified: 05/12/23, 4:03 pm
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

package com.vagabond.testcommon

import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.RetryStrategy
import com.rudderstack.models.RudderServerConfig

class MockConfigDownloadService(val mockConfigDownloadSuccess: Boolean = true,
                                val mockLastErrorMsg: String? = null,
                                val mockConfig: RudderServerConfig = RudderServerConfig(
                                    source = RudderServerConfig.RudderServerConfigSource(),
                                )) : ConfigDownloadService {

    override fun download(
        platform: String,
        libraryVersion: String,
        osVersion: String,
        retryStrategy: RetryStrategy,
        callback: (success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit
    ) {
        callback(mockConfigDownloadSuccess, mockConfig, mockLastErrorMsg)
    }

    override fun shutDown() {
    }

}