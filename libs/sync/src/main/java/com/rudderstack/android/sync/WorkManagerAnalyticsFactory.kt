/*
 * Creator: Debanjan Chatterjee on 27/11/23, 4:58 pm Last modified: 27/11/23, 4:58 pm
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

package com.rudderstack.android.sync

import android.app.Application
import com.rudderstack.core.Analytics

/**
 * Factory to create Analytics instance for WorkManager
 * In case process is killed prior to initialization of [RudderSyncWorker] then
 * [RudderSyncWorker] will create a new instance of Analytics using this factory
 * This factory should have a no-arg constructor so that the instance can be created
 * wth reflection
 *
 */
interface WorkManagerAnalyticsFactory {
    fun createAnalytics(application: Application): Analytics
}