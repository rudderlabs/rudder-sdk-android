package com.rudderstack.android.sample.kotlin

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderOption
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits
import com.rudderstack.android.sdk.core.ecomm.ECommerceCart
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct
import com.rudderstack.android.sdk.core.ecomm.events.CartViewedEvent
import java.util.*
import javax.net.ssl.SSLContext


class MainActivity : AppCompatActivity() {
    private var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        val option = RudderOption()
            .putExternalId("brazeExternalId", "some_external_id_1")
            .putExternalId("braze_id", "some_braze_id_2")
            .putIntegration("GA", true).putIntegration("Amplitude", true)
            .putCustomContext(
                "customContext", mapOf(
                    "version" to "1.0.0",
                    "language" to "kotlin"
                )
            )
        MainApplication.rudderClient!!.identify(
            "userId",
            RudderTraits().putFirstName("Test First Name").putBirthday(Date()),
            option
        )

        val option1 = RudderOption().putIntegration("All", false).putIntegration("Adjust", true)
        val option2 = RudderOption().putIntegration("All", false).putIntegration("Amplitude", true)
        for (i in 1..5) {
            MainApplication.rudderClient!!.track("Test Event Amplitude $i", null, option1)
            MainApplication.rudderClient!!.track("Test Event Adjust $i", null, option2)
        }

        val productA = ECommerceProduct.Builder()
            .withProductId("some_product_id_a")
            .withSku("some_product_sku_a")
            .withCurrency("USD")
            .withPrice(2.99f)
            .withName("Some Product Name A")
            .withQuantity(1f)
            .build()

        val productB = ECommerceProduct.Builder()
            .withProductId("some_product_id_b")
            .withSku("some_product_sku_b")
            .withCurrency("USD")
            .withPrice(3.99f)
            .withName("Some Product Name B")
            .withQuantity(1f)
            .build()

        val productC = ECommerceProduct.Builder()
            .withProductId("some_product_id_c")
            .withSku("some_product_sku_c")
            .withCurrency("USD")
            .withPrice(4.99f)
            .withName("Some Product Name C")
            .withQuantity(1f)
            .build()


        // ECommerce Cart
        val cart = ECommerceCart.Builder()
            .withCartId("some_cart_id")
            .withProduct(productA)
            .withProduct(productB)
            .withProduct(productC)
            .build()


        val cartViewedEvent = CartViewedEvent().withCart(cart)
        MainApplication.rudderClient!!.track(cartViewedEvent.event(), cartViewedEvent.properties())


    }
}

