package com.rudderstack.core

import java.util.concurrent.ExecutorService

open class ConfigurationScope(private val configuration: Configuration) {
    var options: RudderOption = configuration.options
    var flushQueueSize: Int = configuration.flushQueueSize
    var maxFlushInterval: Long = configuration.maxFlushInterval
    var gzipEnabled: Boolean = configuration.gzipEnabled
    var dataPlaneUrl: String = configuration.dataPlaneUrl
    var controlPlaneUrl: String = configuration.controlPlaneUrl
    var rudderLogger: RudderLogger = configuration.rudderLogger
    var analyticsExecutor: ExecutorService = configuration.analyticsExecutor
    var networkExecutor: ExecutorService = configuration.networkExecutor
    var base64Generator: Base64Generator = configuration.base64Generator

    open fun build() = configuration.copy(
        options = this.options,
        flushQueueSize = this.flushQueueSize,
        maxFlushInterval = this.maxFlushInterval,
        gzipEnabled = this.gzipEnabled,
        dataPlaneUrl = this.dataPlaneUrl,
        controlPlaneUrl = this.controlPlaneUrl,
        rudderLogger = this.rudderLogger,
        analyticsExecutor = this.analyticsExecutor,
        networkExecutor = this.networkExecutor,
        base64Generator = this.base64Generator,
    )
}