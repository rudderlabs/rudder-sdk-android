/*
 * Creator: Debanjan Chatterjee on 21/11/23, 1:15 pm Last modified: 16/11/23, 11:15 am
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

import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.models.TrackMessage

class LifecycleObserverPlugin : InfrastructurePlugin, LifecycleListenerPlugin {

    companion object {
        const val EVENT_NAME_APPLICATION_OPENED = "Application Opened"
        const val EVENT_NAME_APPLICATION_STOPPED = "Application Backgrounded"
    }

    private var analytics: Analytics? = null
    private var currentActivityName: String? = null

    private fun sendLifecycleStart() {
        withTrackLifeCycle {
            analytics?.track {
                event(EVENT_NAME_APPLICATION_OPENED)
            }
        }
    }

    private fun sendLifecycleStop() {
        withTrackLifeCycle {
            analytics?.track {
                event(EVENT_NAME_APPLICATION_STOPPED)
            }
        }
    }

    override fun setup(analytics: Analytics) {
        this.analytics = analytics
    }

    override fun shutdown() {
        analytics = null
    }

    override fun updateConfiguration(configuration: Configuration) {
        // no -op
    }

    override fun onAppForegrounded() {
        sendLifecycleStart()
    }

    override fun onAppBackgrounded() {
        sendLifecycleStop()
    }

    override fun onActivityStarted(activityName: String) {
        currentActivityName = activityName
        withTrackLifeCycleAndRecordScreenViews {
            analytics?.screen {
                screenName(activityName)
            }
        }
    }

    override fun onActivityStopped(activityName: String) {
        //No-Ops
    }

    override fun onScreenChange(name: String, arguments: Map<String, Any>?) {
        withRecordScreenViews {
            analytics?.screen {
                screenName(currentActivityName?:return@screen)
                this.category(name)
                this.screenProperties{
                    add(arguments ?: mapOf())
                }
            }
        }
    }

    private fun withTrackLifeCycle(body: () -> Unit) {
        if (analytics?.currentConfigurationAndroid?.trackLifecycleEvents == true) {
            body()
        }
    }
    private fun withRecordScreenViews(body: () -> Unit) {
        if (analytics?.currentConfigurationAndroid?.recordScreenViews == true) {
            body()
        }
    }
    private fun withTrackLifeCycleAndRecordScreenViews(body: () -> Unit) {
        if (analytics?.currentConfigurationAndroid?.trackLifecycleEvents == true
            && analytics?.currentConfigurationAndroid?.recordScreenViews == true) {
            body()
        }
    }

}