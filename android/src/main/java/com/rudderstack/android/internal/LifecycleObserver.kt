/*
 * Creator: Debanjan Chatterjee on 08/07/22, 11:05 AM Last modified: 07/07/22, 5:21 PM
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

package com.rudderstack.android.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.rudderstack.core.RudderUtils
import com.rudderstack.models.Message
import com.rudderstack.models.ScreenMessage
import com.rudderstack.models.TrackMessage
import com.rudderstack.models.android.ScreenProperty

class LifecycleObserver(
    private val application: Application,
    private val isTrackLifecycle: Boolean,
    private val isRecordScreen: Boolean,
    private val callback: (Message) -> Unit
) {

    companion object {
        const val EVENT_NAME_APPLICATION_OPENED = "Application Opened"
        const val EVENT_NAME_APPLICATION_STOPPED = "Application Backgrounded"
    }

    private var _activityCount = 0
        set(value) {
            field = value
            if (value == 1) {
                //send message activity started
                sendLifecycleStart()
            } else if (value == 0) {
                sendLifecycleStop()
            }
        }

    private val lifecycleCallback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            //nothing to implement
        }

        override fun onActivityStarted(activity: Activity) {
            if (isTrackLifecycle) _activityCount++

            if (isRecordScreen) {
                val screenProperty = ScreenProperty(activity.localClassName, true)
                ScreenMessage.create(
                    timestamp = RudderUtils.timeStamp,
                    name = activity.localClassName,
                    properties = screenProperty.getMap()
                ).apply(callback)
            }
        }

        override fun onActivityResumed(activity: Activity) {
            //nothing to implement
        }

        override fun onActivityPaused(activity: Activity) {
            //nothing to implement
        }

        override fun onActivityStopped(activity: Activity) {
            if (isTrackLifecycle) _activityCount--
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            //nothing to implement
        }

        override fun onActivityDestroyed(activity: Activity) {
            //nothing to implement
        }

    }

    init {
        application.registerActivityLifecycleCallbacks(lifecycleCallback)
    }

    private fun sendLifecycleStart() {
        TrackMessage.create(
            eventName = EVENT_NAME_APPLICATION_OPENED, timestamp = RudderUtils.timeStamp
        ).apply(callback)
    }

    private fun sendLifecycleStop() {
        TrackMessage.create(
            eventName = EVENT_NAME_APPLICATION_STOPPED, timestamp = RudderUtils.timeStamp
        ).apply(callback)
    }

    internal fun shutdown() {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallback)
    }
}