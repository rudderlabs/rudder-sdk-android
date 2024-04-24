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

import android.os.SystemClock
import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.androidStorage
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.InfrastructurePlugin
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

const val EVENT_NAME_APPLICATION_OPENED = "Application Opened"
const val EVENT_NAME_APPLICATION_STOPPED = "Application Backgrounded"
private const val MAX_CONFIG_DOWNLOAD_INTERVAL = 90 * 60 * 1000 // 90 MINUTES

internal class LifecycleObserverPlugin(val currentMillisGenerator: (() -> Long) = { SystemClock.uptimeMillis() }) :
    InfrastructurePlugin, LifecycleListenerPlugin {

    private var _isFirstLaunch = AtomicBoolean(true)

    private var _lastSuccessfulDownloadTimeInMillis = AtomicLong(-1L)
    private var analytics: Analytics? = null
    private var currentActivityName: String? = null
    private val listener = ConfigDownloadService.Listener {
        if (it) {
            _lastSuccessfulDownloadTimeInMillis.set(currentMillisGenerator().also {
                println("setting time $it")
            })
        }
    }

    private fun sendLifecycleStart() {
        withTrackLifeCycle {
            analytics?.also { analytics ->
                analytics.track {
                    event(EVENT_NAME_APPLICATION_OPENED)
                    trackProperties {
                        _isFirstLaunch.getAndSet(false).also { isFirstLaunch ->
                            add("from_background" to !isFirstLaunch)
                        }.takeIf { it }?.let {
                            add("version" to (analytics.androidStorage.versionName ?: ""))
                        }
                    }
                }
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
        analytics.applyInfrastructureClosure {
            if (this is ConfigDownloadService) {
                addListener(listener, 1)
            }
        }
    }

    override fun shutdown() {
        _lastSuccessfulDownloadTimeInMillis.set(0)
        analytics?.applyInfrastructureClosure {
            if (this is ConfigDownloadService) {
                removeListener(listener)
            }
        }
        analytics = null
    }

    override fun updateConfiguration(configuration: Configuration) {
        // no -op
    }

    override fun onAppForegrounded() {
        sendLifecycleStart()
        checkAndDownloadSourceConfig()
    }

    private fun checkAndDownloadSourceConfig() {
        analytics?.takeIf { it.currentConfiguration?.shouldVerifySdk == true }?.apply {
            if (currentMillisGenerator().also {
                println("current: $it")
                } - (_lastSuccessfulDownloadTimeInMillis.get().also {
                    println("last: $it")
                }) >= MAX_CONFIG_DOWNLOAD_INTERVAL) {
                analytics?.updateSourceConfig()
            }
        }
    }

    override fun onAppBackgrounded() {
        sendLifecycleStop()
        analytics?.flush()
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
        val activityName = currentActivityName ?: ""
        withRecordScreenViews {
            analytics?.screen {
                screenName(activityName)
                this.category(name)
                this.screenProperties {
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
            && analytics?.currentConfigurationAndroid?.recordScreenViews == true
        ) {
            body()
        }
    }

}
