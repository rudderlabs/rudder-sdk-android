package com.rudderstack.android.internal

import android.util.Log
import com.rudderstack.core.Logger

/**
 * Logger implementation specifically for android.
 *
 */
class AndroidLogger(
    initialLogLevel: Logger.LogLevel = Logger.DEFAULT_LOG_LEVEL
) : Logger {
    private var logLevel: Logger.LogLevel = initialLogLevel
        @Synchronized set
        @Synchronized get

    override fun activate(level: Logger.LogLevel) {
        logLevel = level
    }

    override fun info(tag: String, log: String) {
        if (Logger.LogLevel.INFO >= logLevel)
            Log.i(tag, log)
    }

    override fun debug(tag: String, log: String) {
        if (Logger.LogLevel.DEBUG >= logLevel)
            Log.d(tag, log)
    }

    override fun warn(tag: String, log: String) {
        if (Logger.LogLevel.WARN >= logLevel)
            Log.w(tag, log)
    }

    override fun error(tag: String, log: String, throwable: Throwable?) {
        if (Logger.LogLevel.ERROR >= logLevel)
            Log.e(tag, log, throwable)
    }

    override val level: Logger.LogLevel
        get() = logLevel
}
