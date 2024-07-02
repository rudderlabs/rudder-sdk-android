package com.rudderstack.android.internal.infrastructure

import android.os.SystemClock
import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.utilities.androidStorage
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.Configuration
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

const val EVENT_NAME_APPLICATION_OPENED = "Application Opened"
const val EVENT_NAME_APPLICATION_STOPPED = "Application Backgrounded"
private const val MAX_CONFIG_DOWNLOAD_INTERVAL = 90 * 60 * 1000 // 90 MINUTES

private const val AUTOMATIC = "automatic"

/**
 * We use a [Plugin] instead of [InfrastructurePlugin] as sending events from Infrastructure plugin might
 * not ensure all plugins to be ready
 *
 * @property currentMillisGenerator
 */
internal class LifecycleObserverPlugin(val currentMillisGenerator: (() -> Long) = { SystemClock.uptimeMillis() }) :
    Plugin, LifecycleListenerPlugin {

    private var _isFirstLaunch = AtomicBoolean(true)

    private var _lastSuccessfulDownloadTimeInMillis = AtomicLong(-1L)
    private var analytics: Analytics? = null
    private var currentActivityName: String? = null
    private val listener = ConfigDownloadService.Listener {
        if (it) {
            _lastSuccessfulDownloadTimeInMillis.set(currentMillisGenerator())
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

    override fun intercept(chain: Plugin.Chain): Message {
        return chain.proceed(chain.message())
    }

    override fun setup(analytics: Analytics) {
        this.analytics = analytics
        analytics.applyInfrastructureClosure {
            if (this is ConfigDownloadService) {
                addListener(listener, 1)
            }
        }
    }

    override fun onShutDown() {
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
            if (currentMillisGenerator() - (_lastSuccessfulDownloadTimeInMillis.get()) >= MAX_CONFIG_DOWNLOAD_INTERVAL) {
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
        withRecordScreenViews {
            analytics?.screen {
                screenName(activityName)
                screenProperties {
                    add(AUTOMATIC to true)
                }
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
}
