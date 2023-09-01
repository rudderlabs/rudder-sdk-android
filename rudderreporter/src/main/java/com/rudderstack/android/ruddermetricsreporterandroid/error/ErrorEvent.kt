/*
 * Creator: Debanjan Chatterjee on 09/06/23, 5:31 pm Last modified: 06/06/23, 11:55 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.error

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.rudderstack.android.ruddermetricsreporterandroid.JSerialize
import com.rudderstack.android.ruddermetricsreporterandroid.internal.AppWithState
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DeviceWithState
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.Error
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.Error.Companion.createError
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.ImmutableConfig
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MetadataAware
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.Severity
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.squareup.moshi.Json

class ErrorEvent : MetadataAware, JSerialize<ErrorEvent> {
    @JvmOverloads
    internal constructor(
        originalError: Throwable? = null,
        config: ImmutableConfig,
        severityReason: SeverityReason,
        data: Metadata = Metadata()
    ) : this(
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
        breadcrumbs: MutableList<Breadcrumb> = mutableListOf(),
        discardClasses: Set<String> = setOf(),
        errors: MutableList<Error> = mutableListOf(),
        metadata: Metadata = Metadata(),
        originalError: Throwable? = null,
        projectPackages: Collection<String> = setOf(),
        severityReason: SeverityReason = SeverityReason.newInstance(
            SeverityReason.REASON_HANDLED_EXCEPTION
        ),
    ) {
        this.breadcrumbs = breadcrumbs
        this.discardClasses = discardClasses
        this.errors = errors
        this.metadata = metadata
        this.originalError = originalError
        this.projectPackages = projectPackages
        this.severityReason = severityReason
    }

    @JsonIgnore
    @Transient
    @Json(ignore = true)
    val originalError: Throwable?

    @JsonIgnore
    @Transient
    @Json(ignore = true)
    internal var severityReason: SeverityReason
    @JsonIgnore
    @Transient
    @Json(ignore = true)
    val metadata: Metadata
    @get:SerializedName("metadata")
    @get:JsonProperty("metadata")
    @get:Json(name = "metadata")
    val metadataMap
        get() = metadata.toMap()

    @JsonIgnore
    @Transient
    @Json(ignore = true)
    private val discardClasses: Set<String>
    internal var projectPackages: Collection<String>

    var severity: Severity
        get() = severityReason.currentSeverity
        set(value) {
            severityReason.currentSeverity = value
        }

    lateinit var app: AppWithState
    lateinit var device: DeviceWithState

    val unhandled: Boolean
        get() = severityReason.unhandled
//        set(value) {
//            severityReason.unhandled = value
//        }

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


    override fun addMetadata(section: String, value: Map<String, Any?>) =
        metadata.addMetadata(section, value)

    override fun addMetadata(section: String, key: String, value: Any?) =
        metadata.addMetadata(section, key, value)

    override fun clearMetadata(section: String) = metadata.clearMetadata(section)

    override fun clearMetadata(section: String, key: String) = metadata.clearMetadata(section, key)

    override fun getMetadata(section: String) = metadata.getMetadata(section)

    override fun getMetadata(section: String, key: String) = metadata.getMetadata(section, key)
    override fun serialize(jsonAdapter: JsonAdapter): String? {
        return jsonAdapter.writeToJson(mapOf(
            "exceptions" to errors,
            "severity" to severity,
            "breadcrumbs" to breadcrumbs,
            "context" to context,
            "unhandled" to unhandled,
            "projectPackages" to projectPackages,
            "app" to app,
            "device" to device,
            "metadata" to metadataMap
        ))
    }

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