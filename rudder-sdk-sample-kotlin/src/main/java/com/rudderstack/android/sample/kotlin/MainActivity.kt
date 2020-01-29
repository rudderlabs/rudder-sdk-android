package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderMessageBuilder
import com.rudderstack.android.sdk.core.ecomm.*
import com.rudderstack.android.sdk.core.ecomm.events.CartSharedEvent
import com.rudderstack.android.sdk.core.ecomm.events.ProductSearchedEvent


class MainActivity : AppCompatActivity() {
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rudderClient = MainApplication.rudderClient

        val properties: MutableMap<String, Any> = HashMap()
        properties["test_key_1"] = "test_value_1"

        val childProperties: MutableMap<String, String> = HashMap()
        childProperties["test_child_key_1"] = "test_child_value_1"
        properties["test_key_2"] = childProperties
        properties["category"] = "test_category"

        rudderClient!!.track(
            RudderMessageBuilder()
                .setEventName("vedantuWebSite_test")
                .setUserId("test_user_id")
                .setProperty(properties)
                .build()
        )

        // ECommerce Product
        val productA = ECommerceProduct.Builder()
            .withProductId("some_product_id_a")
            .withSku("some_product_sku_a")
            .withCategory("some_category")
            .withName("Product Name A")
            .withBrand("Product Brand A")
            .withVariant("Product Variant A")
            .withPrice(2.99f)
            .withCurrency("USD")
            .withQuantity(1f)
            .withCoupon("some_coupon")
            .withPosition(1)
            .withUrl("https://product.com/productA")
            .withImageUrl("https://product.com/productA.jpg")
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

        // ECommerce WishList
        val wishList = ECommerceWishList.Builder()
            .withWishListId("some_wish_list_id")
            .withWishListName("Some Wish List Name")
            .build()

        // ECommerce Cart
        val cart = ECommerceCart.Builder()
            .withCartId("some_cart_id")
            .withProduct(productA)
            .withProduct(productB)
            .withProduct(productC)
            .build()

        // ECommerce Order
        val order = ECommerceOrder.Builder()
            .withOrderId("some_order_id")
            .withAffiliation("some_order_affiliation")
            .withCoupon("some_coupon")
            .withCurrency("USD")
            .withDiscount(1.49f)
            .withProducts(productA, productB, productC)
            .withRevenue(10.99f)
            .withShippingCost(2.49f)
            .withTax(1.49f)
            .withTotal(12.99f)
            .withValue(10.49f)
            .build()

        // ECommerce Checkout event
        val checkout = ECommerceCheckout.Builder()
            .withCheckoutId("some_checkout_id")
            .withOrderId("some_order_id")
            .withPaymentMethod("Visa")
            .withShippingMethod("FedEx")
            .withStep(1)
            .build()

        val productSearchedEvent = ProductSearchedEvent()
            .withQuery("blue hotpants")
        rudderClient.track(
            productSearchedEvent.event(),
            productSearchedEvent.properties()
        )
        rudderClient.track("some_test_event")

        val cartSharedEvent = CartSharedEvent()
            .withCart(cart)
            .withSocialChannel("facebook")
            .withShareMessage("some share message")
            .withRecipient("friend@rudderstack.com")
        rudderClient.track(cartSharedEvent.event(), cartSharedEvent.properties())
//
//        navigate_to_first.setOnClickListener {
//            startActivity(Intent(this, FirstActivity::class.java))
//        }
//
//        trackBtn.setOnClickListener {
//            if (rudderClient == null) return@setOnClickListener
//        }
//
//        screenBtn.setOnClickListener {
//            if (rudderClient == null) return@setOnClickListener
//        }
//
//        identifyBtn.setOnClickListener {
//            if (rudderClient == null) return@setOnClickListener
//        }
//
//        resetBtn.setOnClickListener {
//            if (rudderClient == null) return@setOnClickListener
//            rudderClient.reset()
//        }
    }
}
