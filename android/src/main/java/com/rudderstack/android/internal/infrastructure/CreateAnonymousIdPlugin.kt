package com.rudderstack.android.internal.infrastructure

import com.rudderstack.android.AndroidUtils
import com.rudderstack.android.androidStorage
import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin

// TODO("Add description for the class")
class CreateAnonymousIdPlugin : InfrastructurePlugin {

    private var _analytics: Analytics? = null

    override fun setup(analytics: Analytics) {
        _analytics = analytics
        createAnonymousIdIfNeeded()
    }

    private fun createAnonymousIdIfNeeded() {
        if (!isAnonymousIdPresentInStorage()) {
            createNewAnonymousId()
        }
    }

    private fun isAnonymousIdPresentInStorage(): Boolean {
        return _analytics?.androidStorage?.anonymousId != null
    }

    private fun createNewAnonymousId() {
        _analytics?.androidStorage?.setAnonymousId(AndroidUtils.getDeviceId())
    }

    override fun shutdown() {
        _analytics = null
    }
}
