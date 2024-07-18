package com.rudderstack.core

import com.rudderstack.core.models.RudderServerConfig

/**
 * While [Plugin] is mostly used for message processing, [InfrastructurePlugin] is used for
 * implementing infrastructure related tasks.
 * Infrastructure Plugins are generally independent of event processing.
 *
 */
interface InfrastructurePlugin {

    var analytics: Analytics

    fun setup(analytics: Analytics) {
        this.analytics = analytics
    }

    fun shutdown() {}
    fun updateConfiguration(configuration: Configuration) {
        //optional method
    }

    fun updateRudderServerConfig(serverConfig: RudderServerConfig) {
        //optional method
    }

    /**
     * Pause the proceedings if applicable, for example data upload service can halt the upload
     */
    fun pause() {
        //optional-method
    }

    /**
     * Resume the proceedings had the plugin been paused
     */
    fun resume() {
        //optional-method
    }

    fun reset() {
        //optional method
    }
}
