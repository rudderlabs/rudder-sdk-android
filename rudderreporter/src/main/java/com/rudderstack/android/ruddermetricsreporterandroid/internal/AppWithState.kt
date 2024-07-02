package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.ImmutableConfig

/**
 * Stateful information set by the notifier about your app can be found on this class. These values
 * can be accessed and amended if necessary.
 */
class AppWithState(
    binaryArch: String?,
    id: String?,
    releaseStage: String?,
    version: String?,
    codeBundleId: String?,
    versionCode: String?,

) : App(binaryArch, id, releaseStage, version, codeBundleId, versionCode) {

    internal constructor(
        config: ImmutableConfig,
        binaryArch: String?,
        id: String?,
        releaseStage: String?,
        version: String?,
        codeBundleId: String?,
    ) : this(
        binaryArch,
        id,
        releaseStage,
        version,
        codeBundleId,
        config.libraryMetadata.versionCode,
    )
}
