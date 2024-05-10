package com.rudderstack.core

import java.util.concurrent.ExecutorService

open class ConfigurationScope(configuration: Configuration) {
    var options: RudderOptions = configuration.options
    var flushQueueSize: Int = configuration.flushQueueSize
    var maxFlushInterval: Long = configuration.maxFlushInterval
    var gzipEnabled: Boolean = configuration.gzipEnabled
    var dataPlaneUrl: String = configuration.dataPlaneUrl
    var controlPlaneUrl: String = configuration.controlPlaneUrl
    var logger: Logger = configuration.logger
    var analyticsExecutor: ExecutorService = configuration.analyticsExecutor
    var networkExecutor: ExecutorService = configuration.networkExecutor
    var base64Generator: Base64Generator = configuration.base64Generator

    open fun build() = Configuration(
        options = this.options,
        flushQueueSize = this.flushQueueSize,
        maxFlushInterval = this.maxFlushInterval,
        gzipEnabled = this.gzipEnabled,
        dataPlaneUrl = this.dataPlaneUrl,
        controlPlaneUrl = this.controlPlaneUrl,
        logger = this.logger,
        analyticsExecutor = this.analyticsExecutor,
        networkExecutor = this.networkExecutor,
        base64Generator = this.base64Generator,
    )
}