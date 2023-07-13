package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.error.BreadcrumbType

sealed class StateEvent { // JvmField allows direct field access optimizations

    object DeliverPending : StateEvent()

    class AddMetadata(
        @JvmField val section: String,
        @JvmField val key: String?,
        @JvmField val value: Any?
    ) : StateEvent()

    class ClearMetadataSection(@JvmField val section: String) : StateEvent()

    class ClearMetadataValue(
        @JvmField val section: String,
        @JvmField val key: String?
    ) : StateEvent()

    class AddBreadcrumb(
        @JvmField val message: String,
        @JvmField val type: BreadcrumbType,
        @JvmField val timestamp: String,
        @JvmField val metadata: MutableMap<String, Any?>
    ) : StateEvent()

    class UpdateMemoryTrimEvent(
        @JvmField val isLowMemory: Boolean,
        @JvmField val memoryTrimLevel: Int? = null,
        @JvmField val memoryTrimLevelDescription: String = "None"
    ) : StateEvent()

}
