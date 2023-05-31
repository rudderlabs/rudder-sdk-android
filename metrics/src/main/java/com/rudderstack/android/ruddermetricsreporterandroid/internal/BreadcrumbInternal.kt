package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.BreadcrumbType
import java.util.Date

/**
 * In order to understand what happened in your application before each crash, it can be helpful
 * to leave short log statements that we call breadcrumbs. Breadcrumbs are
 * attached to a crash to help diagnose what events lead to the error.
 */
internal class BreadcrumbInternal internal constructor(
    @JvmField var message: String,
    @JvmField var type: BreadcrumbType,
    @JvmField var metadata: MutableMap<String, Any?>?,
    @JvmField val timestamp: Date = Date()
) { // JvmField allows direct field access optimizations

    internal constructor(message: String) : this(
        message,
        BreadcrumbType.MANUAL,
        mutableMapOf(),
        Date()
    )



}
