package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.SeverityReason
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode
import com.rudderstack.android.ruddermetricsreporterandroid.Client
import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import com.rudderstack.android.ruddermetricsreporterandroid.Metadata

/**
 * Provides automatic notification hooks for unhandled exceptions.
 */
internal class ExceptionHandler internal constructor(
    private val client: Client,
    private val logger: Logger
) : Thread.UncaughtExceptionHandler {
    private val originalHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
    private val strictModeHandler = StrictModeHandler()

    fun install() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun uninstall() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            if (client.config.shouldDiscardError(throwable)) {
                return
            }
            val strictModeThrowable = strictModeHandler.isStrictModeThrowable(throwable)

            // Notify any subscribed clients of the uncaught exception
            var metadata = Metadata()
            var violationDesc: String? = null
            if (strictModeThrowable) { // add strictmode policy violation to metadata
                violationDesc = strictModeHandler.getViolationDescription(throwable.message)
                metadata = Metadata()
                metadata.addMetadata(STRICT_MODE_TAB, STRICT_MODE_KEY, violationDesc)
            }
            val severityReason =
                if (strictModeThrowable) SeverityReason.REASON_STRICT_MODE else SeverityReason.REASON_UNHANDLED_EXCEPTION
            if (strictModeThrowable) { // writes to disk on main thread
                val originalThreadPolicy = StrictMode.getThreadPolicy()
                StrictMode.setThreadPolicy(ThreadPolicy.LAX)
                client.notifyUnhandledException(
                    throwable,
                    metadata, severityReason, violationDesc
                )
                StrictMode.setThreadPolicy(originalThreadPolicy)
            } else {
                client.notifyUnhandledException(
                    throwable,
                    metadata, severityReason, null
                )
            }
        } catch (ignored: Throwable) {
            // the runtime would ignore any exceptions here, we make that absolutely clear
            // to avoid any possible unhandled-exception loops
        } finally {
            forwardToOriginalHandler(thread, throwable)
        }
    }

    private fun forwardToOriginalHandler(thread: Thread, throwable: Throwable) {
        // Pass exception on to original exception handler
        if (originalHandler != null) {
            originalHandler.uncaughtException(thread, throwable)
        } else {
            System.err.printf("Exception in thread \"%s\" ", thread.name)
            logger.w("Exception", throwable)
        }
    }

    companion object {
        private const val STRICT_MODE_TAB = "StrictMode"
        private const val STRICT_MODE_KEY = "Violation"
    }
}