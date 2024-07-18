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
internal class ActivityBroadcasterPlugin : InfrastructurePlugin {

    override lateinit var analytics: Analytics
    private val application: Application?
        get() = analytics.currentConfigurationAndroid?.application

    private val activityCount = AtomicInteger()

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        application?.registerActivityLifecycleCallbacks(lifecycleCallback)
    }

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
                if (analytics.currentConfigurationAndroid?.trackLifecycleEvents == true) {
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
        analytics.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                setCurrentActivity(activity)
                onActivityStarted(activity.localClassName)
            }
        }
        analytics.applyMessageClosure {
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
        analytics.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppForegrounded()
            }
        }
        analytics.applyMessageClosure {
            if (this is LifecycleListenerPlugin) {
                this.onAppForegrounded()
            }
        }
    }

    private fun broadCastApplicationStop() {
        analytics.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                setCurrentActivity(null)
                this.onAppBackgrounded()
            }
        }
        analytics.applyMessageClosure {

            if (this is LifecycleListenerPlugin) {
                setCurrentActivity(null)
                this.onAppBackgrounded()
            }
        }
    }

    override fun shutdown() {
        application?.unregisterActivityLifecycleCallbacks(lifecycleCallback)
    }
}
