/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:30 pm Last modified: 09/06/23, 5:18 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.Logger

class Error @JvmOverloads internal constructor(
    var errorClass: String,
    var errorMessage: String?,
    stacktrace: Stacktrace,
    var type: ErrorType = ErrorType.ANDROID
){

    internal val stacktrace: List<Stackframe> = stacktrace.trace
    internal companion object {
        fun createError(exc: Throwable, projectPackages: Collection<String>, logger: Logger): MutableList<Error> {
            return exc.safeUnrollCauses()
                .mapTo(mutableListOf()) { currentEx ->
                    // Somehow it's possible for stackTrace to be null in rare cases
                    val stacktrace = currentEx.stackTrace ?: arrayOf<StackTraceElement>()
                    val trace = Stacktrace(stacktrace, projectPackages, logger)
                     return@mapTo Error(currentEx.javaClass.name, currentEx.localizedMessage,
                         trace)
                }
        }
    }

    override fun toString(): String {
        return "Error(errorClass='$errorClass', errorMessage=$errorMessage, stacktrace=$stacktrace, type=$type)"
    }
internal fun toMap(): Map<String, Any?> {
        return mapOf(
            "errorClass" to errorClass,
            "errorMessage" to errorMessage,
            "stacktrace" to stacktrace.map { it.toMap() },
            "type" to type.toString()
        )
    }
}