package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RedisStoreResponse
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun createFile(file: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(dir, file)
    }

    private fun clearFile(file: File) {
        FileWriter(file, false).close()
    }

    private fun writeFile(file: File, data: String) {
        try {
            println(data)
            FileWriter(file, true).use { fileWriter -> fileWriter.append(data) }
        } catch (e: IOException) {
            //Handle exception
        }
    }

    private fun makeTransformAPICall(data: Map<String, Any>, a: Int, j: Int, tag: String, file: File) {
        MainApplication.rudderClient!!.transform(data, a) { it ->
            val redisStoreResponse: RedisStoreResponse = it
            redisStoreResponse.let {
                val errorMsg = it.errorMsg
                val payload = it.payload
                val success = it.success
                val s = "RedisStoreResponse: $tag: $j Success is: $success, error msg: $errorMsg " +
                        "and payload is $payload" + "\n"
                writeFile(file, s)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val file = createFile("AbhishekLogfile2.txt")
        clearFile(file)
        val noOfTimes = 100
        var map = emptyMap<String, Any>()
        Thread {
            map = mapOf(
                "nextStep" to "",
                "event" to "Passed Event - 1",
            )
            Thread {
                for (i in 1..noOfTimes)
                    makeTransformAPICall(map, 0, i, "a0", file)
            }.start()

            Thread {
                for (j in 1..noOfTimes)
                    makeTransformAPICall(map, 1, j, "a1", file)
            }.start()
//
            Thread {
                map = mapOf(
                    "key-1" to "Should be transformed",
                    "key-2" to true,
                )
                for (k in 1..noOfTimes)
                    makeTransformAPICall(map, 1, k, "a2", file)
            }.start()
        }.start()



/*
//        val option = RudderOption()
//            .putExternalId("brazeExternalId", "some_external_id_1")
//            .putExternalId("braze_id", "some_braze_id_2")
//            .putIntegration("GA", true).putIntegration("Amplitude", true)
//            .putCustomContext(
//                "customContext", mapOf(
//                    "version" to "1.0.0",
//                    "language" to "kotlin"
//                )
//            )
//        MainApplication.rudderClient!!.identify(
//            "userId",
//            RudderTraits().putFirstName("Test First Name").putBirthday(Date()),
//            option
//        )
//
//        val properties = RudderProperty()
//        properties.put("data", true)
//        val option1 = RudderOption().putIntegration("All", false).putIntegration("Amplitude", true)
//        val option2 = RudderOption().putIntegration("All", false).putIntegration("Braze", true)
//        for (i in 1..2) {
//            MainApplication.rudderClient!!.track("Test Event Amplitude $i", properties, option1)
//            MainApplication.rudderClient!!.track("Test Event Braze $i", properties, option2)
//        }
//
//        val productA = ECommerceProduct.Builder()
//            .withProductId("some_product_id_a")
//            .withSku("some_product_sku_a")
//            .withCurrency("USD")
//            .withPrice(2.99f)
//            .withName("Some Product Name A")
//            .withQuantity(1f)
//            .build()
//
//        val productB = ECommerceProduct.Builder()
//            .withProductId("some_product_id_b")
//            .withSku("some_product_sku_b")
//            .withCurrency("USD")
//            .withPrice(3.99f)
//            .withName("Some Product Name B")
//            .withQuantity(1f)
//            .build()
//
//        val productC = ECommerceProduct.Builder()
//            .withProductId("some_product_id_c")
//            .withSku("some_product_sku_c")
//            .withCurrency("USD")
//            .withPrice(4.99f)
//            .withName("Some Product Name C")
//            .withQuantity(1f)
//            .build()
//
//
//        // ECommerce Cart
//        val cart = ECommerceCart.Builder()
//            .withCartId("some_cart_id")
//            .withProduct(productA)
//            .withProduct(productB)
//            .withProduct(productC)
//            .build()
//
//
//        val cartViewedEvent = CartViewedEvent().withCart(cart)
//        MainApplication.rudderClient!!.track(cartViewedEvent.event(), cartViewedEvent.properties())


 */

    }
}

