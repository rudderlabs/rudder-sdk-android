package com.rudderstack.android.ruddermetricsreporterandroid.error

import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultReservoir
import com.rudderstack.android.ruddermetricsreporterandroid.internal.NoopLogger
import com.rudderstack.rudderjsonadapter.JsonAdapter

internal object TestUtils {
    val runtimeVersions = HashMap<String, Any>()

    init {
        runtimeVersions["osBuild"] = "bulldog"
        runtimeVersions["androidApiLevel"] = "24"
    }

    fun generateClient(
        configuration: Configuration,
        jsonAdapter: JsonAdapter,
    ): DefaultErrorClient {
        return DefaultErrorClient(ApplicationProvider.getApplicationContext(), configuration,
            DefaultReservoir(ApplicationProvider.getApplicationContext(), false,
                "test_db"),
            jsonAdapter)
    }
    fun generateClient(
        configuration: Configuration,
        reservoir: Reservoir,
        jsonAdapter: JsonAdapter,
    ): DefaultErrorClient {
        return DefaultErrorClient(
            ApplicationProvider.getApplicationContext(),
            configuration,
            reservoir,
            jsonAdapter,
        )
    }

    fun generateConfiguration(): Configuration {
        val configuration = Configuration(generateLibraryMetadata())
        configuration.logger = NoopLogger
        return configuration
    }
    fun generateLibraryMetadata(): LibraryMetadata {
        return LibraryMetadata("com.bugsnag.android", "1.2.0", "1", "1234QWER")
    }

//    fun generateImmutableConfig(): ImmutableConfig {
//        return convert(generateConfiguration())
//    }
//
//    fun convert(config: Configuration): ImmutableConfig {
//
//        return convertToImmutableConfig(config, null, null, null)
//    }
//
//    fun generateDevice(): Device {
//        val buildInfo = defaultInfo()
//        return Device(
//            buildInfo, arrayOf<String>(), null, null, null, 109230923452L, runtimeVersions
//        )
//    }
//
//    fun generateDeviceWithState(): DeviceWithState {
//        val buildInfo = defaultInfo()
//        return DeviceWithState(
//            buildInfo,
//            null,
//            null,
//            null,
//            109230923452L,
//            runtimeVersions,
//            22234423124L,
//            92340255592L,
//            "portrait",
//            Date(0)
//        )
//    }
//
//    fun generateAppWithState(): AppWithState {
//        return AppWithState(
//            generateImmutableConfig(), null, null, null, null, null, null, null, null, null
//        )
//    }

//    fun generateApp(): App {
//        return App(generateImmutableConfig(), null, null, null, null, null)
//    }
}
