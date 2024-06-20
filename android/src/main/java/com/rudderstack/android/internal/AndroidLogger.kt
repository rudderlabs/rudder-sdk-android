package com.rudderstack.android.internal

import android.util.Log
import com.rudderstack.core.RudderLogger

/**
 * Logger implementation specifically for android.
 *
 */
class AndroidLogger(
    initialLogLevel: RudderLogger.LogLevel = RudderLogger.DEFAULT_LOG_LEVEL
) : RudderLogger {
    private var logLevel: RudderLogger.LogLevel = initialLogLevel
        @Synchronized set
        @Synchronized get

    override fun activate(level: RudderLogger.LogLevel) {
        logLevel = level
    }

    override fun info(tag: String, log: String) {
        if (RudderLogger.LogLevel.INFO >= logLevel)
            Log.i(tag, log)
    }

    override fun debug(tag: String, log: String) {
        if (RudderLogger.LogLevel.DEBUG >= logLevel)
            Log.d(tag, log)
    }

    override fun warn(tag: String, log: String) {
        if (RudderLogger.LogLevel.WARN >= logLevel)
            Log.w(tag, log)
    }

    override fun error(tag: String, log: String, throwable: Throwable?) {
        if (RudderLogger.LogLevel.ERROR >= logLevel)
            Log.e(tag, log, throwable)
    }

    override val level: RudderLogger.LogLevel
        get() = logLevel
}
