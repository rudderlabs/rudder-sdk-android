/*
 * Creator: Debanjan Chatterjee on 23/11/23, 6:20 pm Last modified: 23/11/23, 6:20 pm
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

package com.rudderstack.android.internal.infrastructure.sync

import android.app.Application
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.InfrastructurePlugin

abstract class WorkerManagerPlugin : InfrastructurePlugin {
    private var application: Application?= null
    override fun setup(analytics: Analytics) {
        val currentConfig = analytics.currentConfigurationAndroid
        if (currentConfig?.isPeriodicFlushEnabled != true) {
            return
        }
        currentConfig.apply {
            this@WorkerManagerPlugin.application = this.application
            application.registerWorkManager(
                analytics, workManagerAnalyticsFactoryClassName
            )
        }
    }
    override fun shutdown() {
        application?.unregisterWorkManager()
    }

    abstract val workManagerAnalyticsFactoryClassName: Class<out WorkManagerAnalyticsFactory>

    override fun updateConfiguration(configuration: Configuration) {
        // no -op
    }

}