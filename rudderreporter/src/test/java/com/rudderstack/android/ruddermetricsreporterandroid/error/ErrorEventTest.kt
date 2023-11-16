/*
 * Creator: Debanjan Chatterjee on 29/08/23, 7:29 pm Last modified: 29/08/23, 7:29 pm
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

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadataTestGson
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadataTestJackson
import com.rudderstack.android.ruddermetricsreporterandroid.LibraryMetadataTestMoshi
import com.rudderstack.android.ruddermetricsreporterandroid.Logger
import com.rudderstack.android.ruddermetricsreporterandroid.TEST_ERROR_EVENTS_JSON
import com.rudderstack.android.ruddermetricsreporterandroid.internal.AppWithState
import com.rudderstack.android.ruddermetricsreporterandroid.internal.CustomDateAdapterMoshi
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DeviceBuildInfo
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DeviceWithState
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.ImmutableConfig
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class ErrorEventTest {
    abstract val jsonAdapter: JsonAdapter


    @Test
    fun serialize() {
        val immutableConfig = ImmutableConfig(
            LibraryMetadata(
                "test_lib",
                "1.3.0", "14", "my_write_key"
            ),
            listOf("com.rudderstack.android"),
            setOf(BreadcrumbType.ERROR),
            listOf("com.rudderstack.android.MyClass"),
            CrashFilter.generateWithKeyWords(listOf()),
            object : Logger {},
            15,
            16,
            listOf("Develpoment"),
            "Dev",
            PackageInfo().also {
                it.packageName = "com.example.myPackage"
            },
            ApplicationInfo()
        )
        val errorEvent = ErrorEvent(
            originalError = Exception(), //if this line moves from 69, change the line number in
            // testErrorEventJson line 46 file=ErrorEventTest.kt lineNumber=309.0,
            config = immutableConfig,
            severityReason = SeverityReason.newInstance(SeverityReason.REASON_ANR),
            data = Metadata(store = mutableMapOf("m1" to mutableMapOf("dumb" to "dumber")))
        )
        errorEvent.app = AppWithState(immutableConfig, "arm64", "write_key", "release", "2.1.0",
            "reporter.test")
        errorEvent.device = DeviceWithState(
            DeviceBuildInfo(
                "LG", "Nexus",
                "8.0.1", 29, null, "null", null, "LG", arrayOf("x86_64"),
            ), false, "locale", 1234556L, mutableMapOf(
                "androidApiLevel" to "29",
                "osBuild" to "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys"
            ), 54354354L,
            45345345L, null, Calendar.getInstance(TimeZone.getTimeZone("US")).apply {
                set(Calendar.YEAR, 2023)
                set(Calendar.MONTH, 7)
                set(Calendar.DAY_OF_MONTH, 31)
                set(Calendar.HOUR_OF_DAY, 17)
                set(Calendar.MINUTE, 32)
                set(Calendar.SECOND, 32)
                set(Calendar.MILLISECOND, 0)
            }.time
        )
        val actual = jsonAdapter.readJson(errorEvent.serialize(jsonAdapter)!!.also { println(it) },
            object : RudderTypeAdapter<Map<String, Any>>() {

            })
        val expected = jsonAdapter.readJson(TEST_ERROR_EVENTS_JSON,
            object : RudderTypeAdapter<Map<String, Any>>() {
            })

        MatcherAssert.assertThat(actual?.entries?.filter {
            it.key != "exceptions" }?.also { println("parser: ${jsonAdapter::class.simpleName}")
                                           println(it)
                                           },
            contains
            (*(expected!!.entries!!.filter { it.key != "exceptions" }.toTypedArray().also {
                println("expected for parser: ${jsonAdapter::class.simpleName}")
                println(it)
            }))
        )
    }
}

class GsonErrorEventTest : ErrorEventTest() {
    override val jsonAdapter: JsonAdapter
        get() = GsonAdapter()

}
class JacksonErrorEventTest : ErrorEventTest() {
    override val jsonAdapter: JsonAdapter
        get() = JacksonAdapter()

}
//class MoshiErrorEventTest : ErrorEventTest() {
//    override val jsonAdapter: JsonAdapter
//        get() = MoshiAdapter( Moshi.Builder()
//            .add(CustomDateAdapterMoshi())
//            .add(CustomDateAdapterMoshi())
//            .addLast(KotlinJsonAdapterFactory())
//            .build())
//
//}

@RunWith(Suite::class)
@Suite.SuiteClasses(
    GsonErrorEventTest::class,
    JacksonErrorEventTest::class,
//    MoshiErrorEventTest::class
)
class DefaultErrorEventsTestSuite {
}