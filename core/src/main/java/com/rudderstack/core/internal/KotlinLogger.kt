package com.rudderstack.core.internal

import com.rudderstack.core.RudderLogger

class KotlinLogger(
    initialLogLevel: RudderLogger.LogLevel = RudderLogger.DEFAULT_LOG_LEVEL
) : RudderLogger {

    private var logLevel = initialLogLevel
        @Synchronized set
        @Synchronized get

    override fun activate(level: RudderLogger.LogLevel) {
        logLevel = level
    }

    override fun info(tag: String, log: String) {
        if (RudderLogger.LogLevel.INFO >= logLevel)
            println("$tag-info : $log")
    }

    override fun debug(tag: String, log: String) {
        if (RudderLogger.LogLevel.DEBUG >= logLevel)
            println("$tag-debug : $log")
    }

    override fun warn(tag: String, log: String) {
        if (RudderLogger.LogLevel.WARN >= logLevel)
            println("$tag-warn : $log")
    }

    override fun error(tag: String, log: String, throwable: Throwable?) {
        if (RudderLogger.LogLevel.ERROR >= logLevel)
            println("$tag-error : $log")
    }

    override val level: RudderLogger.LogLevel
        get() = logLevel
}
