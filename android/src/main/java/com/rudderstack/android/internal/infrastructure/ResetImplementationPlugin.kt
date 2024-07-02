package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.utilities.contextState
import com.rudderstack.android.utilities.processNewContext
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.models.createContext
import com.rudderstack.core.models.updateWith

class ResetImplementationPlugin : InfrastructurePlugin {
    private var _analytics: Analytics? = null
    override fun setup(analytics: Analytics) {
        _analytics = analytics
    }
    private val contextState
    get() =_analytics?.contextState
    override fun reset() {

        _analytics?.processNewContext(contextState?.value?.updateWith(traits = mapOf(),
            externalIds = listOf()
        ) ?: createContext())
    }

    override fun shutdown() {
        //nothing to implement
    }

}
