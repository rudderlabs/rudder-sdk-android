/*
 * Creator: Debanjan Chatterjee on 02/08/23, 1:05 pm Last modified: 02/08/23, 1:05 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid

import android.os.Build
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultUploaderTestGson
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultUploaderTestJackson
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultUploaderTestMoshi
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite

abstract class LibraryMetadataTest {
    protected abstract val jsonAdapter: JsonAdapter
    @Test
    fun serialize() {
        val libraryMetadata = LibraryMetadata("test","1.0","4","abcde")
        val json = libraryMetadata.serialize(jsonAdapter)
        assertEquals("{\"name\":\"test\",\"sdk_version\":\"1.0\",\"version_code\":\"4\"," +
                     "\"write_key\":\"abcde\",\"os_version\":\"${Build.VERSION.SDK_INT}\"}",json)
    }
    @Test
    fun `serialize with version`() {
        val libraryMetadata = LibraryMetadata("test","1.0","4","abcde", "[14]")
        val json = libraryMetadata.serialize(jsonAdapter)
        assertEquals("{\"name\":\"test\",\"sdk_version\":\"1.0\",\"version_code\":\"4\"," +
                     "\"write_key\":\"abcde\",\"os_version\":\"[14]\"}",json)
    }
}
class LibraryMetadataTestGson : LibraryMetadataTest() {
    override val jsonAdapter: JsonAdapter = GsonAdapter()
}
class LibraryMetadataTestJackson : LibraryMetadataTest() {
    override val jsonAdapter: JsonAdapter = JacksonAdapter()
}
class LibraryMetadataTestMoshi : LibraryMetadataTest() {
    override val jsonAdapter: JsonAdapter = MoshiAdapter()
}
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LibraryMetadataTestGson::class,
    LibraryMetadataTestJackson::class,
    LibraryMetadataTestMoshi::class
)
class DefaultMetadataTestSuite {
}