package com.rudderstack.android.ruddermetricsreporterandroid.internal

import java.io.IOException

/**
 * The severity of an Event, one of "error", "warning" or "info".
 *
 * By default, unhandled exceptions will be Severity.ERROR and handled
 * exceptions sent with bugsnag.notify will be Severity.WARNING.
 */
enum class Severity(private val str: String) {
    ERROR("error"),
    WARNING("warning"),
    INFO("info");

    internal companion object {
        internal fun fromDescriptor(desc: String) = values().find { it.str == desc }
    }
}
