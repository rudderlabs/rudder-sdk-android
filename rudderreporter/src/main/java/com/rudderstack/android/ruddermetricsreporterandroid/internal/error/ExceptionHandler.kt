/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:30 pm Last modified: 05/06/23, 5:52 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.ruddermetricsreporterandroid.internal.error

import com.rudderstack.android.ruddermetricsreporterandroid.error.SeverityReason
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode
import com.rudderstack.android.ruddermetricsreporterandroid.error.DefaultErrorClient
import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import com.rudderstack.android.ruddermetricsreporterandroid.error.Metadata

/**
 * Provides automatic notification hooks for unhandled exceptions.
 */
internal class ExceptionHandler internal constructor(
    private val errorClient: DefaultErrorClient,
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
            if (errorClient.config.shouldDiscardError(throwable)) {
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
                errorClient.notifyUnhandledException(
                    throwable,
                    metadata, severityReason, violationDesc
                )
                StrictMode.setThreadPolicy(originalThreadPolicy)
            } else {
                errorClient.notifyUnhandledException(
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