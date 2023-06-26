package com.rudderstack.android.ruddermetricsreporterandroid

import org.junit.Test

import org.junit.Assert.*
import kotlin.math.ln

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val log8base2 = (ln(8.0) / ln(2.0)).toInt()
        assert(log8base2 == 3)
    }
}