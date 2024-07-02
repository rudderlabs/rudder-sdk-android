package com.rudderstack.core

typealias RudderLogLevel = Logger.LogLevel

/**
 * Logger interface.
 * Contains methods for different scenarios
 *
 */
interface Logger {
    companion object {
        const val DEFAULT_TAG = "Rudder-Analytics"
        val DEFAULT_LOG_LEVEL = LogLevel.NONE
    }

    /**
     * Activate or deactivate logger based on choice.
     *
     * @param level should log anything greater or equal to that level. See [LogLevel]
     */
    fun activate(level: LogLevel)

    fun info(tag: String = DEFAULT_TAG, log: String)

    fun debug(tag: String = DEFAULT_TAG, log: String)

    fun warn(tag: String = DEFAULT_TAG, log: String)

    fun error(tag: String = DEFAULT_TAG, log: String, throwable: Throwable? = null)

    /**
     * Level based on priority. Higher the number, greater the priority
     *
     * @property level priority for each type
     */
    enum class LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        NONE,
    }

    val level: LogLevel

    object Noob : Logger {
        override fun activate(level: LogLevel) {
            // do nothing
        }

        override fun info(tag: String, log: String) {
            // do nothing
        }

        override fun debug(tag: String, log: String) {
            // do nothing
        }

        override fun warn(tag: String, log: String) {
            // do nothing
        }

        override fun error(tag: String, log: String, throwable: Throwable?) {
            // do nothing
        }

        override val level: LogLevel
            get() = DEFAULT_LOG_LEVEL
    }
}
