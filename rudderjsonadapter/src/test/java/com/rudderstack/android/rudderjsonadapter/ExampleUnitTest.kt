package com.rudderstack.android.rudderjsonadapter

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

import org.junit.Assert.*
import java.lang.reflect.ParameterizedType

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    data class SomeClass(val name: String, val prop: String)

    val someJson = "{" +
            "\" type1\" : [" +
            "{" +
            "\"name\":\"ludo\"," +
            "\"prop\":\"iok\"" +
            "}" +
            "]" +
            "}"

    @Test
    fun checkType() {
//        val type = Map<String,String>::class.java.typeName
        val rta = object : RudderTypeAdapter<Map<String, List<SomeClass>>>() {}
//        val type = ((rta::class.java.genericSuperclass) as ParameterizedType).rawType
        val types = rta::class.java.fields.forEach {
            println("level1 $it")
        }
//        println("type : $type")
        rta::class.java.genericSuperclass?.also {
            println("type: $it")
            (it as ParameterizedType).actualTypeArguments.forEach {
                println("Inside: $it")
            }
        }
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter =
            moshi.adapter<MutableMap<String, List<SomeClass>>>((rta::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[0])
        val res :Map<String, List<SomeClass>>? = jsonAdapter.fromJson(someJson)
        assert(res != null)
        println("res: $res")

    }
}