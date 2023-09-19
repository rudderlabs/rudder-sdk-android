/*
 * Creator: Debanjan Chatterjee on 15/09/23, 6:43 pm Last modified: 15/09/23, 6:43 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.TEST_ERROR_EVENTS_JSON
import com.rudderstack.android.ruddermetricsreporterandroid.internal.NoopLogger
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.util.Date

class BreadcrumbTest {
    private val TEST_BREADCRUMB_JSON = """
        {
          "name": "test",
          "type": "ERROR",
          "metadata": {
            "test_m": "test_m_v"
          },
          "timestamp": "2023-08-31T12:02:32.000Z"
        }

    """.trimIndent()
    private val gsonAdapter = GsonAdapter()
    private val jacksonAdapter = JacksonAdapter()

    @Test
    fun `Breadcrumb serialization test`(){
        val breadcrumb = Breadcrumb(
            "test",
            BreadcrumbType.ERROR,
            mapOf("test_m" to "test_m_v"),
            Date(123, 7, 31, 17, 32, 32),
            NoopLogger
        )
        val breadcrumbJson = gsonAdapter.writeToJson(breadcrumb)
        val breadcrumbJsonJackson = jacksonAdapter.writeToJson(breadcrumb)
        val gsonExpected = gsonAdapter.readJson(
            TEST_BREADCRUMB_JSON,
            object : RudderTypeAdapter<Map<String, Any>>() {
            })
        val jacksonExpected = jacksonAdapter.readJson(
            TEST_BREADCRUMB_JSON,
            object : RudderTypeAdapter<Map<String, Any>>() {
            })
        val gsonActual = gsonAdapter.readJson(
            breadcrumbJson!!,
            object : RudderTypeAdapter<Map<String, Any>>() {
            })
        val jacksonActual = jacksonAdapter.readJson(
            breadcrumbJson!!,
            object : RudderTypeAdapter<Map<String, Any>>() {
            })
        println("***bc-gson*****")
        println(breadcrumbJson)
        println("***bc-jackson*****")
        println(breadcrumbJsonJackson)
        println("******gson actual*********")
        println(gsonActual)
        println("******jackson actual*********")
        println(jacksonActual)
        println("******gson expected*********")
        println(gsonExpected)
        println("******jackson expected*********")
        println(jacksonExpected)
        assertThat(breadcrumbJson, equalTo( breadcrumbJsonJackson))
        assertThat(gsonActual, equalTo(gsonExpected))
        assertThat(jacksonActual, equalTo(jacksonExpected))
        assertThat(gsonActual, equalTo(jacksonActual))
    }
}