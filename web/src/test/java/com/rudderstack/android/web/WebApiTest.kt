/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.android.web

import com.rudderstack.android.gsonrudderadapter.GsonAdapter
import com.rudderstack.android.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.android.moshirudderadapter.MoshiAdapter
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.android.web.models.ArtDataListResponse
import com.rudderstack.android.web.models.ArtDataResponse
import com.rudderstack.android.web.models.Data
import junit.framework.TestCase
import junit.framework.TestSuite
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

open class WebApiTest {
    protected var jsonAdapter: JsonAdapter = MoshiAdapter()

    private lateinit var webService: WebService
    private lateinit var agifyWebService: WebService
    private lateinit var jsonPlaceHolderWebService: WebService

    //public api used
    //https://api.artic.edu/docs/#quick-start
    //also test using
    //https://gorest.co.in
    //for post testing
    //https://jsonplaceholder.typicode.com/guide/



    @Before
    fun init() {
        webService = WebServiceFactory.getWebService(
            "https://api.artic.edu/api/v1/",
            jsonAdapter
        )
        agifyWebService = WebServiceFactory.getWebService(
            "https://api.agify.io/",
            jsonAdapter
        )
        jsonPlaceHolderWebService = WebServiceFactory.getWebService(
            "https://jsonplaceholder.typicode.com/",
            jsonAdapter
        )
    }


    @Test
    fun testSimpleGetSync() {
        val response = webService.get(
            null,
            mapOf("fields" to "id,title"),
            "artworks/200154",
            ArtDataResponse::class.java
        ).get().body

        MatcherAssert.assertThat(
            response, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.isA(ArtDataResponse::class.java),
                Matchers.hasProperty(
                    "data", Matchers.allOf(
                        Matchers.notNullValue(),
                        Matchers.isA(Data::class.java),
                        Matchers.hasProperty("id", Matchers.equalTo(200154))
                    )
                )
            )
        )
        assertThat(response?.info?.licenseText, allOf(
            notNullValue(),
            `is`("The data in this response is licensed under a Creative Commons Zero (CC0) " +
                    "1.0 designation and the Terms and Conditions of artic.edu.")
        ))
    }

    @Test
    fun testParameterizedGetSync() {
        val response = webService.get(
            null,
            mapOf("fields" to "id,title"),
            "artworks",
            ArtDataListResponse::class.java
        ).get().body

        MatcherAssert.assertThat(
            response, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.isA(ArtDataListResponse::class.java),
                Matchers.hasProperty(
                    "data", Matchers.allOf(
                        Matchers.notNullValue(),
                        Matchers.isA(List::class.java),
//                Matchers.not("id",Matchers.equalTo(200154) )
                    )
                )
            )
        )
        MatcherAssert.assertThat(
            response?.data, Matchers.allOf(
                Matchers.notNullValue()
            )
        )
        MatcherAssert.assertThat(
            response?.data?.get(0), Matchers.allOf(
                Matchers.notNullValue()
            )
        )

    }

    @Test
    fun testTypeAdaptedGetSync() {
        val response = agifyWebService.get(
            null,
            mapOf("name" to "bella"),
            "",
            object : RudderTypeAdapter<Map<String, String>>() {}
        ).get().body

        MatcherAssert.assertThat(
            response, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.isA(Map::class.java),
            )
        )
        val age = response?.get("age")
        MatcherAssert.assertThat(age, Matchers.equalTo("34"))
        val name = response?.get("name")
        MatcherAssert.assertThat(name, Matchers.equalTo("bella"))
    }

    @Test
    fun testSimpleGetASync() {
        val isComplete = AtomicBoolean(false)
        var response: ArtDataResponse? = null
        webService.get(
            null,
            mapOf("fields" to "id,title"),
            "artworks/200154",
            ArtDataResponse::class.java
        ) {
            response = it.body
            MatcherAssert.assertThat(
                response, Matchers.allOf(
                    Matchers.notNullValue(),
                    Matchers.isA(ArtDataResponse::class.java),
                    Matchers.hasProperty(
                        "data", Matchers.allOf(
                            Matchers.notNullValue(),
                            Matchers.isA(Data::class.java),
                            Matchers.hasProperty("id", Matchers.equalTo(200154))
                        )
                    )
                )
            )
            isComplete.set(true)
        }
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isComplete)
    }

    @Test
    fun testPostSync() {
        //this will also be success return body
        /*"{\n" +
                "    \"title\": \"foo\",\n" +
                "    \"body\": \"bar\",\n" +
                "    \"userId\": \"1\"\n" +
                "  }"*/
        val postBody = mapOf("title" to "foo", "body" to "bar", "userId" to "1")
        val postStringedBody =
            jsonAdapter.writeToJson(postBody, object : RudderTypeAdapter<Map<String, String>>() {})
        val response =
            jsonPlaceHolderWebService.post(mapOf("Content-Type" to "application/json; charset=UTF-8"),
                null,
                postStringedBody,
                "posts",
                object : RudderTypeAdapter<Map<String, String>>() {}).get().body

        MatcherAssert.assertThat(
            response, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.aMapWithSize(4) //a field "id" is sent alongside
            )
        )
        val title = response?.get("title")
        MatcherAssert.assertThat(title, equalTo("foo"))
        val body = response?.get("body")
        MatcherAssert.assertThat(body, equalTo("bar"))
        val userId = response?.get("userId")
        MatcherAssert.assertThat(userId, equalTo("1"))
    }

    @Test
    fun testPostASync() {
        //this will also be success return body
        /*"{\n" +
                "    \"title\": \"foo\",\n" +
                "    \"body\": \"bar\",\n" +
                "    \"userId\": \"1\"\n" +
                "  }"*/
        val isComplete = AtomicBoolean(false)
        val postBody = mapOf("title" to "foo", "body" to "bar", "userId" to "1")
        val postStringedBody =
            jsonAdapter.writeToJson(postBody, object : RudderTypeAdapter<Map<String, String>>() {})
        jsonPlaceHolderWebService.post(mapOf("Content-Type" to "application/json; charset=UTF-8"),
            null, postStringedBody, "posts", object : RudderTypeAdapter<Map<String, String>>() {}) {
            val response = it.body
            MatcherAssert.assertThat(
                response, Matchers.allOf(
                    Matchers.notNullValue(),
                    Matchers.aMapWithSize(4) //a field "id" is sent alongside
                )
            )
            val title = response?.get("title")
            MatcherAssert.assertThat(title, equalTo("foo"))
            val body = response?.get("body")
            MatcherAssert.assertThat(body, equalTo("bar"))
            val userId = response?.get("userId")
            MatcherAssert.assertThat(userId, equalTo("1"))
            isComplete.set(true)
        }
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isComplete)
    }
}
class WebApiTestJackson : WebApiTest() {
    init {
        jsonAdapter = GsonAdapter()
    }
}
class WebApiTestGson : WebApiTest() {
    init {
        jsonAdapter = GsonAdapter()
    }
}
class WebApiTestMoshi : WebApiTest() {
    init {
        jsonAdapter = MoshiAdapter()
    }
}