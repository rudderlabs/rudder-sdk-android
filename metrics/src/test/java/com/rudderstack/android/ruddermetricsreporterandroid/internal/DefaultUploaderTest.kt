/*
 * Creator: Debanjan Chatterjee on 22/06/23, 6:16 pm Last modified: 22/06/23, 6:16 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.internal

import com.rudderstack.android.ruddermetricsreporterandroid.Source
import com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorModel
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.mockito.Mockito
import java.util.Locale

open class DefaultUploaderTest {

    protected var jsonAdapter: JsonAdapter = MoshiAdapter()
    private val mockedDataCollectionModule = Mockito.mock(DataCollectionModule::class.java).also {
        Mockito.`when`(
            it.deviceDataCollector
        ).thenReturn (
            Mockito.mock(DeviceDataCollector::class.java).also {
                Mockito.`when`(it.generateDeviceWithState(Mockito.anyLong())).thenReturn(
                    DeviceWithState(DeviceBuildInfo(MANUFACTURER, MODEL, OS_VERSION, API_LEVEL,
                    OS_BUILD, null, null, null, null), false, ID, "Locale.ENGLISH",
                    null, mutableMapOf<String, Any>(),
                        null, null, null, null)
                )

        })
    }
    private val defaultUploader = DefaultUploader(mockedDataCollectionModule, "",
        Source("Android", "1.14.0", "write_key"), networkExecutor =
        TestExecutor(), jsonAdapter = jsonAdapter)
    @Test
    fun upload() {
        defaultUploader.upload(listOf(), ErrorModel()){

        }
    }
    companion object{
        private const val MANUFACTURER = "Google"
        private const val MODEL = "pixel 7"
        private const val OS_VERSION = "10"
        private const val API_LEVEL = 28
        private const val OS_BUILD = "Android"
        private const val ID = "id"

    }
}

class DefaultUploaderTestGson: DefaultUploaderTest() {
    init {
        jsonAdapter = GsonAdapter()
    }
}
class DefaultUploaderTestJackson: DefaultUploaderTest() {
    init {
        jsonAdapter = JacksonAdapter()
    }
}
class DefaultUploaderTestMoshi: DefaultUploaderTest() {
    init {
        jsonAdapter = MoshiAdapter()
    }
}
@RunWith(Suite::class)
@Suite.SuiteClasses(DefaultUploaderTestGson::class, DefaultUploaderTestJackson::class, DefaultUploaderTestMoshi::class)
class DefaultUploaderTestSuite {
}