/*
 * Creator: Debanjan Chatterjee on 20/11/23, 3:48 pm Last modified: 20/11/23, 3:48 pm
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

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin
import java.util.concurrent.atomic.AtomicInteger

/** Tracks the Activities in the application and broadcasts the same */
internal class ActivityBroadcasterPlugin(
) : InfrastructurePlugin {

    private val application: Application?
        get() = analytics?.currentConfigurationAndroid?.application
    private var analytics: Analytics? = null
    private val activityCount = AtomicInteger()
    private val lifecycleCallback by lazy {
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
                incrementActivityCount()
                if (activityCount.get() == 1) {
                    broadCastApplicationStart()
                }
                broadcastActivityStart(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                //nothing to implement
            }

            override fun onActivityPaused(activity: Activity) {
                //nothing to implement
            }

            override fun onActivityStopped(activity: Activity) {
                if (analytics?.currentConfigurationAndroid?.trackLifecycleEvents == true) {
                    decrementActivityCount()
                    if (activityCount.get() == 0) {
                        broadCastApplicationStop()
                    }
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                //nothing to implement
            }

            override fun onActivityDestroyed(activity: Activity) {
                //nothing to implement
            }
        }
    }

    private fun broadcastActivityStart(activity: Activity) {
        analytics?.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                setCurrentActivity(activity)
                onActivityStarted(activity.localClassName)
            }
        }
        analytics?.applyMessageClosure {
            if (this is LifecycleListenerPlugin) {
                setCurrentActivity(activity)
                onActivityStarted(activity.localClassName)
            }
        }
    }

    private fun decrementActivityCount() {
        activityCount.decrementAndGet()
    }

    private fun incrementActivityCount() {
        activityCount.incrementAndGet()
    }


    private fun broadCastApplicationStart() {
        analytics?.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppForegrounded()
            }
        }
        analytics?.applyMessageClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppForegrounded()
            }
        }
    }

    private fun broadCastApplicationStop() {
        analytics?.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                setCurrentActivity(null)
                this.onAppBackgrounded()
            }
        }
        analytics?.applyMessageClosure {

            if (this is LifecycleListenerPlugin) {
                setCurrentActivity(null)
                this.onAppBackgrounded()
            }
        }
    }

    override fun setup(analytics: Analytics) {
        this.analytics = analytics
        application?.registerActivityLifecycleCallbacks(lifecycleCallback)
    }

    override fun shutdown() {
        application?.unregisterActivityLifecycleCallbacks(lifecycleCallback)
    }
}
