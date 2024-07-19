package com.rudderstack.gsonrudderadapter

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

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

    @Test
    public fun whenDeserializingToSimpleObject_thenCorrect() {
        val json = "{\"intValue\":\"2\",\"stringValue\":\"one\"}"

        val targetObject = Gson().fromJson(json, Foo::class.java)

        println("int value: ${targetObject.intValue}")
        assertEquals(targetObject.intValue, 2)
        assertEquals(targetObject.stringValue, "one")
    }
    class Foo {
        var intValue = 0
        var stringValue: String? = null // + standard equals and hashCode implementations
    }
}
