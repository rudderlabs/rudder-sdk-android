package com.rudderstack.core.internal

import com.rudderstack.core.Logger

class KotlinLogger(
    initialLogLevel: Logger.LogLevel = Logger.DEFAULT_LOG_LEVEL
) : Logger {

    private var logLevel = initialLogLevel
        @Synchronized set
        @Synchronized get

    override fun activate(level: Logger.LogLevel) {
        logLevel = level
    }

    override fun info(tag: String, log: String) {
        if (Logger.LogLevel.INFO >= logLevel)
            println("$tag-info : $log")
    }

    override fun debug(tag: String, log: String) {
        if (Logger.LogLevel.DEBUG >= logLevel)
            println("$tag-debug : $log")
    }

    override fun warn(tag: String, log: String) {
        if (Logger.LogLevel.WARN >= logLevel)
            println("$tag-warn : $log")
    }

    override fun error(tag: String, log: String, throwable: Throwable?) {
        if (Logger.LogLevel.ERROR >= logLevel)
            println("$tag-error : $log")
    }

    override val level: Logger.LogLevel
        get() = logLevel
}
