/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:30 pm Last modified: 06/06/23, 11:53 am
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

/**
 * Serialize an exception stacktrace and mark frames as "in-project"
 * where appropriate.
 */
internal class Stacktrace {

    companion object {
        private const val STACKTRACE_TRIM_LENGTH = 200

        /**
         * Calculates whether a stackframe is 'in project' or not by checking its class against
         * [Configuration.getProjectPackages].
         *
         * For example if the projectPackages included 'com.example', then
         * the `com.example.Foo` class would be considered in project, but `org.example.Bar` would
         * not.
         */
        fun inProject(className: String, projectPackages: Collection<String>): Boolean? {
            return when {
                projectPackages.any { className.startsWith(it) } -> true
                else -> null
            }
        }
    }

    val trace: List<Stackframe>

    constructor(frames: List<Stackframe>) {
        trace = limitTraceLength(frames)
    }

    constructor(
        stacktrace: Array<StackTraceElement>,
        projectPackages: Collection<String>,
        logger: Logger
    ) {
        val frames = limitTraceLength(stacktrace)
        trace = frames.mapNotNull { serializeStackframe(it, projectPackages, logger) }
    }

    private fun limitTraceLength(frames: Array<StackTraceElement>): Array<StackTraceElement> {
        return when {
            frames.size >= STACKTRACE_TRIM_LENGTH -> frames.sliceArray(0 until STACKTRACE_TRIM_LENGTH)
            else -> frames
        }
    }

    private fun limitTraceLength(frames: List<Stackframe>): List<Stackframe> {
        return when {
            frames.size >= STACKTRACE_TRIM_LENGTH -> frames.subList(0, STACKTRACE_TRIM_LENGTH)
            else -> frames
        }
    }

    private fun serializeStackframe(
        el: StackTraceElement,
        projectPackages: Collection<String>,
        logger: Logger
    ): Stackframe? {
        try {
            val className = el.className
            val methodName = when {
                className.isNotEmpty() -> className + "." + el.methodName
                else -> el.methodName
            }

            return Stackframe(
                methodName,
                el.fileName ?: "Unknown",
                el.lineNumber,
                inProject(className, projectPackages)
            )
        } catch (lineEx: Exception) {
            logger.w("Failed to serialize stacktrace", lineEx)
            return null
        }
    }

    override fun toString(): String {
        return "Stacktrace{trace=$trace}"
    }

}
