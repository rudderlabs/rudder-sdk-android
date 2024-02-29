/*
 * Creator: Debanjan Chatterjee on 29/11/23, 4:58 pm Last modified: 29/11/23, 4:58 pm
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

package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.applyConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.InfrastructurePlugin

internal class AnonymousIdHeaderPlugin : InfrastructurePlugin{
    private var dataUploadService: DataUploadService? = null
    private var _analytics: Analytics? = null
    override fun setup(analytics: Analytics) {
        _analytics = analytics
        dataUploadService = analytics.dataUploadService
    }

    override fun shutdown() {
        dataUploadService = null
        _analytics = null
    }

    override fun updateConfiguration(configuration: Configuration) {
        if(configuration !is ConfigurationAndroid) return
        val anonId = configuration.anonymousId?: AndroidUtils.getDeviceId(configuration
            .application).also {
                _analytics?.applyConfigurationAndroid {
                    copy(anonymousId = it)
                }
        }
            dataUploadService?.addHeaders(mapOf("Anonymous-Id" to anonId))
    }
}