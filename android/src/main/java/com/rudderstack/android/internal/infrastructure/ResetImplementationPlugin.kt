package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.utilities.contextState
import com.rudderstack.android.utilities.processNewContext
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.models.createContext
import com.rudderstack.core.models.updateWith

class ResetImplementationPlugin : InfrastructurePlugin {

    override lateinit var analytics: Analytics

    private val contextState
        get() = analytics.contextState

    override fun reset() {
        analytics.processNewContext(
            contextState?.value?.updateWith(
                traits = mapOf(),
                externalIds = listOf()
            ) ?: createContext()
        )
    }
}
