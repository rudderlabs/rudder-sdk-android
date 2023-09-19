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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BreadcrumbTest {
    private val TEST_BREADCRUMB_JSON = """
        {
          "name": "test",
          "type": "ERROR",
          "metadata": {
            "test_m": "test_m_v"
          },
          "timestamp": "2023-08-31T17:32:32.000Z"
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
            Calendar.getInstance(TimeZone.getTimeZone("US")).apply {
                set(Calendar.YEAR, 2023)
                set(Calendar.MONTH, 7)
                set(Calendar.DAY_OF_MONTH, 31)
                set(Calendar.HOUR_OF_DAY, 17)
                set(Calendar.MINUTE, 32)
                set(Calendar.SECOND, 32)
                set(Calendar.MILLISECOND, 0)
            }.time,
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
        assertThat(breadcrumbJson, equalTo( breadcrumbJsonJackson))
        assertThat(gsonActual, equalTo(gsonExpected))
        assertThat(jacksonActual, equalTo(jacksonExpected))
        assertThat(gsonActual, equalTo(jacksonActual))
    }
}