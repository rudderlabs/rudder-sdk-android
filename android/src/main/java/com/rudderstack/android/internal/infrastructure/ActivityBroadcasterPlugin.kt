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
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.InfrastructurePlugin
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tracks the Activities in the application and broadcasts the same
 *
 */
internal class ActivityBroadcasterPlugin(
) : InfrastructurePlugin {
    private var application: Application? = null
    private var analytics: Analytics? = null
    private val activityCount = AtomicInteger()
    private val lifecycleCallback by lazy {
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                //nothing to implement
            }

            override fun onActivityStarted(activity: Activity) {
                incrementActivityCount()
                if (analytics?.currentConfigurationAndroid?.recordScreenViews == true) {
                    broadcastActivityStart(activity)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                //nothing to implement
            }

            override fun onActivityPaused(activity: Activity) {
                //nothing to implement
            }

            override fun onActivityStopped(activity: Activity) {
                if (analytics?.currentConfigurationAndroid?.recordScreenViews == true) {
                    decrementActivityCount()
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
                onActivityStarted(activity.localClassName)
            }
        }
        analytics?.applyClosure {
            if (this is LifecycleListenerPlugin) {
                onActivityStarted(activity.localClassName)
            }
        }
    }

    private fun decrementActivityCount() {
        sideEffect(activityCount.decrementAndGet())
    }

    private fun incrementActivityCount() {
        sideEffect(activityCount.incrementAndGet())
    }

    private fun sideEffect(activityCount: Int) {
        if (activityCount == 1) {
            //send message activity started
            broadCastApplicationStart()
        } else if (activityCount == 0) {
            broadCastApplicationStop()
        }
    }

    private fun broadCastApplicationStart() {
        analytics?.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppForegrounded()
            }
        }
        analytics?.applyClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppForegrounded()
            }
        }
    }

    private fun broadCastApplicationStop() {
        analytics?.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppBackgrounded()
            }
        }
        analytics?.applyClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppBackgrounded()
            }
        }
    }

    override fun setup(analytics: Analytics) {
        if (analytics.currentConfigurationAndroid?.trackLifecycleEvents == true) {
            application?.registerActivityLifecycleCallbacks(lifecycleCallback)
        }
    }

    override fun shutdown() {
        application?.unregisterActivityLifecycleCallbacks(lifecycleCallback)
    }

    override fun updateConfiguration(configuration: Configuration) {
        application = (configuration as? ConfigurationAndroid)?.application
    }

}