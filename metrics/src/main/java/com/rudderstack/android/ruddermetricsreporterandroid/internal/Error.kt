package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.bugsnag.android.Stackframe
import com.bugsnag.android.Stacktrace
import com.rudderstack.android.ruddermetricsreporterandroid.Logger

class Error @JvmOverloads internal constructor(
    var errorClass: String,
    var errorMessage: String?,
    stacktrace: Stacktrace,
    val logger: Logger,
    var type: ErrorType = ErrorType.ANDROID
){

    val stacktrace: List<Stackframe> = stacktrace.trace
    internal companion object {
        fun createError(exc: Throwable, projectPackages: Collection<String>, logger: Logger): MutableList<Error> {
            return exc.safeUnrollCauses()
                .mapTo(mutableListOf()) { currentEx ->
                    // Somehow it's possible for stackTrace to be null in rare cases
                    val stacktrace = currentEx.stackTrace ?: arrayOf<StackTraceElement>()
                    val trace = Stacktrace(stacktrace, projectPackages, logger)
                     return@mapTo Error(currentEx.javaClass.name, currentEx.localizedMessage, trace, logger)
                }
        }
    }

    override fun toString(): String {
        return "Error(errorClass='$errorClass', errorMessage=$errorMessage, stacktrace=$stacktrace, type=$type)"
    }

}