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

/**
 * Represents a single stackframe from a [Throwable]
 */
class Stackframe{

    /**
     * The name of the method that was being executed
     */
    var method: String?

    /**
     * The location of the source file
     */
    var file: String?

    /**
     * The line number within the source file this stackframe refers to
     */
    var lineNumber: Number?

    /**
     * Whether the package is considered to be in your project for the purposes of grouping and
     * readability on the Bugsnag dashboard. Project package names can be set in
     * [Configuration.projectPackages]
     */
    var inProject: Boolean?

    /**
     * Lines of the code surrounding the frame, where the lineNumber is the key (React Native only)
     */
    var code: Map<String, String?>?

    /**
     * The column number of the frame (React Native only)
     */
    var columnNumber: Number?

    /**
     * The address of the instruction where the event occurred.
     */
    var frameAddress: Long? = null

    /**
     * The address of the function where the event occurred.
     */
    var symbolAddress: Long? = null

    /**
     * The address of the library where the event occurred.
     */
    var loadAddress: Long? = null

    /**
     * Identifies the exact build this frame originates from.
     */
    var codeIdentifier: String? = null

    /**
     * Whether this frame identifies the program counter
     */
    var isPC: Boolean? = null

    /**
     * The type of the error
     */
    var type: ErrorType? = null

    @JvmOverloads
    internal constructor(
        method: String?,
        file: String?,
        lineNumber: Number?,
        inProject: Boolean?,
        code: Map<String, String?>? = null,
        columnNumber: Number? = null
    ) {
        this.method = method
        this.file = file
        this.lineNumber = lineNumber
        this.inProject = inProject
        this.code = code
        this.columnNumber = columnNumber
    }
    internal fun toMap(): Map<String, Any?> {
        return mapOf(
            "type" to type,
            "method" to method,
            "file" to file,
            "lineNumber" to lineNumber,
            "inProject" to inProject,
            "code" to code,
            "columnNumber" to columnNumber,
            "frameAddress" to frameAddress,
            "symbolAddress" to symbolAddress,
            "loadAddress" to loadAddress,
            "codeIdentifier" to codeIdentifier,
            "isPC" to isPC,
        ).filterValues { it != null }
    }

    override fun toString(): String {
        return "Stackframe{method='$method', file='$file', lineNumber=$lineNumber, " +
            "inProject=$inProject, code=$code, columnNumber=$columnNumber}"
    }
}
