package com.rudderstack.android.ruddermetricsreporterandroid.internal


/**
 * Represents a single native stackframe
 */
class NativeStackframe internal constructor(

    /**
     * The name of the method that was being executed
     */
    var method: String?,

    /**
     * The location of the source file
     */
    var file: String?,

    /**
     * The line number within the source file this stackframe refers to
     */
    var lineNumber: Number?,

    /**
     * The address of the instruction where the event occurred.
     */
    var frameAddress: Long?,

    /**
     * The address of the function where the event occurred.
     */
    var symbolAddress: Long?,

    /**
     * The address of the library where the event occurred.
     */
    var loadAddress: Long?,

    /**
     * Whether this frame identifies the program counter
     */
    var isPC: Boolean?,

    /**
     * The type of the error
     */
    var type: ErrorType? = null,

    /**
     * Identifies the exact build this frame originates from.
     */
    var codeIdentifier: String? = null,
)