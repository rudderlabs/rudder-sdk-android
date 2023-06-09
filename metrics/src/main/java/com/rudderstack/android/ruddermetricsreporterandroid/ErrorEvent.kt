package com.rudderstack.android.ruddermetricsreporterandroid

import com.bugsnag.android.Stackframe
import com.rudderstack.android.ruddermetricsreporterandroid.internal.*
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Error.Companion.createError

class ErrorEvent : MetadataAware{
    @JvmOverloads
    internal constructor(
        originalError: Throwable? = null,
        config: ImmutableConfig,
        severityReason: SeverityReason,
        data: Metadata = Metadata()
    ) : this(
        config.logger,
        mutableListOf(),
        config.discardClasses.toSet(),
        when (originalError) {
            null -> mutableListOf()
            else -> createError(originalError, config.projectPackages, config.logger)
        },
        data.copy(),
        originalError,
        config.projectPackages,
        severityReason
    )

    internal constructor(
        logger: Logger,
        breadcrumbs: MutableList<Breadcrumb> = mutableListOf(),
        discardClasses: Set<String> = setOf(),
        errors: MutableList<Error> = mutableListOf(),
        metadata: Metadata = Metadata(),
        originalError: Throwable? = null,
        projectPackages: Collection<String> = setOf(),
        severityReason: SeverityReason = SeverityReason.newInstance(
            SeverityReason.REASON_HANDLED_EXCEPTION),
    ) {
        this.logger = logger
        this.breadcrumbs = breadcrumbs
        this.discardClasses = discardClasses
        this.errors = errors
        this.metadata = metadata
        this.originalError = originalError
        this.projectPackages = projectPackages
        this.severityReason = severityReason
    }

    val originalError: Throwable?
    internal var severityReason: SeverityReason

    val logger: Logger
    val metadata: Metadata
    private val discardClasses: Set<String>
    internal var projectPackages: Collection<String>

    var severity: Severity
        get() = severityReason.currentSeverity
        set(value) {
            severityReason.currentSeverity = value
        }

    lateinit var app: AppWithState
    lateinit var device: DeviceWithState
    var unhandled: Boolean
        get() = severityReason.unhandled
        set(value) {
            severityReason.unhandled = value
        }

    var breadcrumbs: MutableList<Breadcrumb>
    var errors: MutableList<Error>
    var groupingHash: String? = null
    var context: String? = null


    protected fun shouldDiscardClass(): Boolean {
        return when {
            errors.isEmpty() -> true
            else -> errors.any { discardClasses.contains(it.errorClass) }
        }
    }

//    protected fun isAnr(event: ErrorEvent): Boolean {
//        val errors = event.errors
//        var errorClass: String? = null
//        if (errors.isNotEmpty()) {
//            val error = errors[0]
//            errorClass = error.errorClass
//        }
//        return "ANR" == errorClass
//    }
//
//
//    internal fun getErrorTypesFromStackframes(): Set<ErrorType> {
//        val errorTypes = errors.mapNotNull(Error::type).toSet()
//        val frameOverrideTypes = errors
//            .map { it.stacktrace }
//            .flatMap { it.mapNotNull(Stackframe::type) }
//        return errorTypes.plus(frameOverrideTypes)
//    }

//    internal fun normalizeStackframeErrorTypes() {
//        if (getErrorTypesFromStackframes().size == 1) {
//            errors.flatMap { it.stacktrace }.forEach {
//                it.type = null
//            }
//        }
//    }

    internal fun updateSeverityReasonInternal(severityReason: SeverityReason) {
        this.severityReason = severityReason
    }

    protected fun updateSeverityInternal(severity: Severity) {
        severityReason =
            SeverityReason(
                severityReason.severityReasonType,
                severity,
                severityReason.unhandled,
                severityReason.unhandledOverridden,
                severityReason.attributeValue,
                severityReason.attributeKey
            )
    }

    protected fun updateSeverityReason(@SeverityReason.SeverityReasonType reason: String) {
        severityReason =
            SeverityReason(
                reason,
                severityReason.currentSeverity,
                severityReason.unhandled,
                severityReason.unhandledOverridden,
                severityReason.attributeValue,
                severityReason.attributeKey
            )
    }

    fun getSeverityReasonType(): String = severityReason.severityReasonType

//    fun trimMetadataStringsTo(maxLength: Int): TrimMetrics {
//        var stringCount = 0
//        var charCount = 0
//
//        var stringAndCharCounts = metadata.trimMetadataStringsTo(maxLength)
//        stringCount += stringAndCharCounts.itemsTrimmed
//        charCount += stringAndCharCounts.dataTrimmed
//        for (breadcrumb in breadcrumbs) {
//            stringAndCharCounts = breadcrumb.impl.trimMetadataStringsTo(maxLength)
//            stringCount += stringAndCharCounts.itemsTrimmed
//            charCount += stringAndCharCounts.dataTrimmed
//        }
//        return TrimMetrics(stringCount, charCount)
//    }

//    fun trimBreadcrumbsBy(byteCount: Int): TrimMetrics {
//        var removedBreadcrumbCount = 0
//        var removedByteCount = 0
//        while (removedByteCount < byteCount && breadcrumbs.isNotEmpty()) {
//            val breadcrumb = breadcrumbs.removeAt(0)
//            removedByteCount += JsonHelper.serialize(breadcrumb).size
//            removedBreadcrumbCount++
//        }
//        when (removedBreadcrumbCount) {
//            1 -> breadcrumbs.add(Breadcrumb("Removed to reduce payload size", logger))
//            else -> breadcrumbs.add(
//                Breadcrumb(
//                    "Removed, along with ${removedBreadcrumbCount - 1} older breadcrumbs, to reduce payload size",
//                    logger
//                )
//            )
//        }
//        return TrimMetrics(removedBreadcrumbCount, removedByteCount)
//    }

    override fun addMetadata(section: String, value: Map<String, Any?>) =
        metadata.addMetadata(section, value)

    override fun addMetadata(section: String, key: String, value: Any?) =
        metadata.addMetadata(section, key, value)

    override fun clearMetadata(section: String) = metadata.clearMetadata(section)

    override fun clearMetadata(section: String, key: String) = metadata.clearMetadata(section, key)

    override fun getMetadata(section: String) = metadata.getMetadata(section)

    override fun getMetadata(section: String, key: String) = metadata.getMetadata(section, key)

    override fun toString(): String {
        return "ErrorEvent{" +
                "originalError=" + originalError +
                ", severityReason=" + severityReason +
                ", metadata=" + metadata +
                ", discardClasses=" + discardClasses +
                ", projectPackages=" + projectPackages +
                ", breadcrumbs=" + breadcrumbs +
                ", errors=" + errors +
                ", groupingHash='" + groupingHash + '\'' +
                ", context='" + context + '\'' +
                "}"
    }
}