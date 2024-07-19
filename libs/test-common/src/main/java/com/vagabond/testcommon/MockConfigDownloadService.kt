package com.vagabond.testcommon

import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigDownloadService
import com.rudderstack.core.models.RudderServerConfig

class MockConfigDownloadService(
    val mockConfigDownloadSuccess: Boolean = true,
    val mockLastErrorMsg: String? = null,
    val mockConfig: RudderServerConfig = RudderServerConfig(
        source = RudderServerConfig.RudderServerConfigSource(),
    )
) : ConfigDownloadService {

    override lateinit var analytics: Analytics

    override fun download(callback: (success: Boolean, RudderServerConfig?, lastErrorMsg: String?) -> Unit) {
        callback(mockConfigDownloadSuccess, mockConfig, mockLastErrorMsg)
    }

    override fun addListener(listener: ConfigDownloadService.Listener, replay: Int) {
        // Not-required
    }

    override fun removeListener(listener: ConfigDownloadService.Listener) {
        // Not-required
    }

    override fun shutdown() {
        // Not-required
    }

}
