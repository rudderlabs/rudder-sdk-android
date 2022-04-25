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

package com.rudderstack.android.jacksonrudderadapter

import com.fasterxml.jackson.annotation.JsonProperty
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test

class ParsingTest {
    data class SomeClass(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("prop")
        val prop: String)

    val someJson = "{" +
            "\"type1\" : [" +
            "{" +
            "\"name\":\"ludo\"," +
            "\"prop\":\"iok\"" +
            "}" +
            "]" +
            "}"

    //for checking map conversion
    data class MapClass(@JsonProperty("name") val name: String,
                        @JsonProperty("age")
                        val age: Int)


    @Test
    fun checkDeserialization() {
//        val type = Map<String,String>::class.java.typeName
        val rta = object : RudderTypeAdapter<Map<String, List<SomeClass>>>() {}
        val ja = JacksonAdapter()
        val res = ja.readJson<Map<String, List<SomeClass>>>(someJson, rta)
        assert(res != null)
//        println("res: $res")
        assert(res!!["type1"] != null)
        assert(res["type1"]?.size ?: 0 == 1)
        assert(res["type1"]?.get(0)?.name == "ludo")
        assert(res["type1"]?.get(0)?.prop == "iok")

    }

    @Test
    fun checkSerialization() {
        val someClass = SomeClass("ludo", "iok")
        val ja = JacksonAdapter()
        val res =
            ja.writeToJson<Map<String, List<SomeClass>>>(mapOf(Pair("type1", listOf(someClass))),
                object : RudderTypeAdapter<Map<String, List<SomeClass>>>() {})
//        println(res)
        assert(res == someJson.replace(" ", ""))
    }

    @Test
    fun checkMapToObjConversion() {
        val mapRepresentation = mapOf("name" to "Foo", "age" to 20)
        val adapter = JacksonAdapter()

        val outCome: MapClass? = adapter.readMap(mapRepresentation, MapClass::class.java)

        MatcherAssert.assertThat(
            outCome, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.isA(MapClass::class.java),
                Matchers.hasProperty("name", Matchers.equalTo("Foo")),
                Matchers.hasProperty("age", Matchers.equalTo(20))
            )
        )
    }

}