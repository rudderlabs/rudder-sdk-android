package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.ImmutableConfig


/**
 * Stateless information set by the notifier about your app can be found on this class. These values
 * can be accessed and amended if necessary.
 */
open class App internal constructor(
    /**
     * The architecture of the running application binary
     */
    var binaryArch: String?,

    /**
     * The package name of the application
     */
    var id: String?,

    /**
     * The release stage set in [Configuration.releaseStage]
     */
    var releaseStage: String?,

    /**
     * The version of the application set in [Configuration.version]
     */
    var version: String?,

    /**
     The revision ID from the manifest (React Native apps only)
     */
    var codeBundleId: String?,


    /**
     * The version code of the application set in [Configuration.versionCode]
     */
    var versionCode: String?
) {

    internal constructor(
        config: ImmutableConfig,
        binaryArch: String?,
        id: String?,
        releaseStage: String?,
        version: String?,
        codeBundleId: String?
    ) : this(
        binaryArch,
        id,
        releaseStage,
        version,
        codeBundleId,
        config.libraryMetadata.versionCode
    )
}
