package com.rudderstack.android.moshijsonadapter

import com.rudderstack.android.rudderjsonadapter.JsonAdapterFactory
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test
import java.lang.reflect.ParameterizedType

class ParsingTest {
    data class SomeClass(val name: String, val prop: String)

    val someJson = "{" +
            "\"type1\" : [" +
            "{" +
            "\"name\":\"ludo\"," +
            "\"prop\":\"iok\"" +
            "}" +
            "]" +
            "}"

    @Test
    fun checkDeserialization() {
//        val type = Map<String,String>::class.java.typeName
        val rta = object : RudderTypeAdapter<Map<String, List<SomeClass>>>() {}
        val ja = MoshiAdapter()
        val res = ja.readJsonParameterized<Map<String, List<SomeClass>>>(rta, someJson)
        assert(res != null)
        println("res: $res")
        assert(res!!["type1"] != null)
        assert(res["type1"]?.size?:0 ==1)
        assert(res["type1"]?.get(0)?.name == "ludo")
        assert(res["type1"]?.get(0)?.prop == "iok")

    }
    @Test
    fun checkSerialization(){
        val someClass = SomeClass("ludo", "iok")
        val ja = MoshiAdapter()
        val res = ja.writeToJson<Map<String, List<SomeClass>>>(mapOf(Pair("type1", listOf(someClass)) ),
        object : RudderTypeAdapter<Map<String, List<SomeClass>>>(){})
        println(res)
        assert(res == someJson.replace(" ",""))
    }
}