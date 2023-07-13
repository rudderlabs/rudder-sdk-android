package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.JSerialize
import com.rudderstack.rudderjsonadapter.JsonAdapter

/**
 * Stateless information set by the notifier about the device on which the event occurred can be
 * found on this class. These values can be accessed and amended if necessary.
 */
open class Device internal constructor(
    buildInfo: DeviceBuildInfo,

    /**
     * The Application Binary Interface used
     */
    var cpuAbi: Array<String>?,

    /**
     * Whether the device has been jailbroken
     */
    var jailbroken: Boolean?,

    /**
     * The IETF language tag of the locale used
     */
    var locale: String?,

    /**
     * The total number of bytes of memory on the device
     */
    var totalMemory: Long?,

    /**
     * A collection of names and their versions of the primary languages, frameworks or
     * runtimes that the application is running on
     */
    runtimeVersions: MutableMap<String, Any>?
) : JSerialize<Device>{

    /**
     * The manufacturer of the device used
     */
    var manufacturer: String? = buildInfo.manufacturer

    /**
     * The model name of the device used
     */
    var model: String? = buildInfo.model

    /**
     * The name of the operating system running on the device used
     */
    var osName: String? = "android"

    /**
     * The version of the operating system running on the device used
     */
    var osVersion: String? = buildInfo.osVersion

    var runtimeVersions: MutableMap<String, Any>? = sanitizeRuntimeVersions(runtimeVersions)
        set(value) {
            field = sanitizeRuntimeVersions(value)
        }

    private fun sanitizeRuntimeVersions(value: MutableMap<String, Any>?): MutableMap<String, Any>? =
        value?.mapValuesTo(mutableMapOf()) { (_, value) -> value.toString() }

    override fun serialize(jsonAdapter: JsonAdapter): String? {
        return jsonAdapter.writeToJson(this)
    }
}
