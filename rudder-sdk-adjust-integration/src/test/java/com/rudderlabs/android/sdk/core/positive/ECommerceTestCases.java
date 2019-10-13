//package com.rudderlabs.android.sdk.core.positive;
//
//
//import com.rudderlabs.android.sdk.core.BaseTestCase;
//import com.rudderlabs.android.sdk.core.RudderElement;
//import com.rudderlabs.android.sdk.core.RudderMessageBuilder;
//import org.junit.Test;
//
//public class ECommerceTestCases extends BaseTestCase {
//    private ECommerceProduct dummyProduct;
//    private ECommercePromotion dummyPromotion;
//
//    @Override
//    public void setup() throws InterruptedException {
//        super.setup();
//        dummyProduct = new ECommerceProduct(
//                "507f1f77bcf86cd799439011",
//                "45790-32",
//                "Games",
//                "Monopoly: 3rd Edition",
//                "Monopoly",
//                "Single User",
//                19f,
//                1f,
//                "MAY_DEALS_3",
//                1,
//                "https://www.example.com/product/path",
//                "https://www.example.com/product/path.jpg"
//        );
//
//        dummyPromotion = new ECommercePromotion(
//                "promo_1",
//                "top_banner_2",
//                "75% store-wide shoe sale",
//                "home_banner_top"
//        );
//    }
//
//    private void createCart(ECommerceCartBuilder builder) {
//        builder.createCart("skdjsidjsdkdj29j")
//                .addProductToCart(dummyProduct)
//                .addProductToCart(dummyProduct)
//                .addProductToCart(dummyProduct);
//    }
//
//    private void createOrder(ECommerceCartBuilder builder) {
//        builder.createOrder(
//                "50314b8e9bcf000000000000",
//                "Google Store",
//                30f,
//                25.00f,
//                3f,
//                2f,
//                2.5f,
//                "hasbros",
//                "USD"
//        );
//    }
//
//    private void updateOrder(ECommerceCartBuilder builder) {
//        builder.updateOrder(
//                "Google Store",
//                27.50f,
//                30f,
//                25.00f,
//                3f,
//                2f,
//                2.5f,
//                "hasbros",
//                "USD"
//        );
//    }
//
//    @Test /*PRODUCTS_SEARCHED*/
//    public void testProductSearch() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCTS_SEARCHED.getValue())
//                .setPropertyBuilder(new ECommercePropertyBuilder().addQuery("blue hotpants"))
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_LIST_VIEWED*/
//    public void productListViewedTest() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_LIST_VIEWED.getValue())
//                .setPropertyBuilder(
//                        new ECommercePropertyBuilder()
//                                .addListId("hot_deals_1")
//                                .addCategory("Deals")
//                                .addProduct(dummyProduct)
//                                .addProduct(dummyProduct)
//                                .addProduct(dummyProduct)
//                )
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(5000);
//    }
//
//    @Test /*PRODUCT_LIST_FILTERED*/
//    public void testProductListFiltered() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_LIST_FILTERED.getValue())
//                .setPropertyBuilder(
//                        new ECommercePropertyBuilder()
//                                .addListId("todays_deals_may_11_2016")
//                                .addCategory("Deals")
//                                .addProduct(dummyProduct)
//                                .addProduct(dummyProduct)
//                                .addProduct(dummyProduct)
//                                .addFilter(new TypeValuePair("department", "beauty"))
//                                .addFilter(new TypeValuePair("price", "under-$25"))
//                                .addSortItem(new TypeValuePair("price", "desc"))
//                )
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PROMOTION_VIEWED*/
//    public void testPromotionViewed() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PROMOTION_VIEWED.getValue())
//                .setPropertyBuilder(new ECommercePropertyBuilder().addPromotion(dummyPromotion))
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PROMOTION_CLICKED*/
//    public void promotionClickedTest() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PROMOTION_CLICKED.getValue())
//                .setPropertyBuilder(new ECommercePropertyBuilder().addPromotion(dummyPromotion))
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_CLICKED*/
//    public void productClickedTest() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_CLICKED.getValue())
//                .setPropertyBuilder(
//                        new ECommercePropertyBuilder().addProductViewed(dummyProduct)
//                )
//                .build();
//        rudderClient.track(event);
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_VIEWED*/
//    public void productViewedTest() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_VIEWED.getValue())
//                .setPropertyBuilder(
//                        new ECommercePropertyBuilder().addProductViewed(dummyProduct)
//                )
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_ADDED*/
//    public void productAddedTest() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_ADDED.getValue())
//                .putValue(
//                        ECommerceCartBuilder.instance()
//                                .createCart("skdjsidjsdkdj29j")
//                                .addProductToCart(dummyProduct)
//                                .buildProductProperty()
//                )
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_REMOVED*/
//    public void productRemovedTest() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_REMOVED.getValue())
//                .putValue(
//                        ECommerceCartBuilder.instance()
//                                .createCart("skdjsidjsdkdj29j")
//                                .addProductToCart(dummyProduct)
//                                .addProductToCart(dummyProduct)
//                                .addProductToCart(dummyProduct)
//                                .addProductToCart(dummyProduct)
//                                .removeProductFromCart(dummyProduct)
//                                .buildProductProperty()
//                )
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*CART_VIEWED*/
//    public void cartViewedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.CART_VIEWED.getValue())
//                .putValue(builder.buildCartProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*CHECKOUT_STARTED*/
//    public void testCheckoutStarted() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.startCheckout("50314b8e9bcf000000000000");
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.CHECKOUT_STARTED.getValue())
//                .putValue(builder.buildOrderProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*CHECKOUT_STEP_VIEWED*/
//    public void checkoutStepViewedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.startCheckout("50314b8e9bcf000000000000");
//        builder.updateCheckout(2, "Fedex", "Visa");
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.CHECKOUT_STEP_VIEWED.getValue())
//                .putValue(builder.buildCheckoutProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*CHECKOUT_STEP_COMPLETED*/
//    public void checkoutStepCompletedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.startCheckout("50314b8e9bcf000000000000");
//        builder.updateCheckout(2, "Fedex", "Visa");
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.CHECKOUT_STEP_COMPLETED.getValue())
//                .putValue(builder.buildCheckoutProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PAYMENT_INFO_ENTERED*/
//    public void paymentInfoEnteredCompletedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.startCheckout("50314b8e9bcf000000000000");
//        builder.updateCheckout(2, "Fedex", "Visa");
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PAYMENT_INFO_ENTERED.getValue())
//                .putValue(builder.buildCheckoutProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*ORDER_UPDATED*/
//    public void orderUpdatedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        updateOrder(builder);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.ORDER_UPDATED.getValue())
//                .putValue(builder.buildOrderProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*ORDER_COMPLETED*/
//    public void orderCompletedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        updateOrder(builder);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.ORDER_COMPLETED.getValue())
//                .putValue(builder.buildOrderProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*ORDER_REFUNDED ==> FULL*/
//    public void orderFullRefundedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.processFullRefund();
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.ORDER_REFUNDED.getValue())
//                .putValue(builder.buildForFullRefund())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*ORDER_REFUNDED ==> PARTIAL*/
//    public void orderPartialRefundedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.processPartialRefund(dummyProduct, dummyProduct);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.ORDER_REFUNDED.getValue())
//                .putValue(builder.buildForPartialReturn(30, "USD"))
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*ORDER_CANCELLED*/
//    public void orderCancelledTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.startCheckout("50314b8e9bcf000000000000");
//        updateOrder(builder);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.ORDER_CANCELLED.getValue())
//                .putValue(builder.buildOrderProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*COUPON_ENTERED*/
//    public void couponEnteredTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.addCoupon("may_deals_2016");
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.COUPON_ENTERED.getValue())
//                .putValue(builder.buildCouponProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*COUPON_APPLIED*/
//    public void couponAppliedTet() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.addCoupon("may_deals_2016");
//        builder.applyCoupon("May Deals 2016", 23.32f);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.COUPON_APPLIED.getValue())
//                .putValue(builder.buildCouponProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*COUPON_DENIED*/
//    public void couponDeniedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.addCoupon("may_deals_2016");
//        builder.applyCoupon("May Deals 2016", 23.32f);
//        builder.couponDenied("Coupon expired");
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.COUPON_DENIED.getValue())
//                .putValue(builder.buildCouponProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*COUPON_REMOVED*/
//    public void couponRemovedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        createOrder(builder);
//        builder.addCoupon("may_deals_2016");
//        builder.applyCoupon("May Deals 2016", 23.32f);
//        builder.removeCoupon();
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.COUPON_REMOVED.getValue())
//                .putValue(builder.buildCouponProperty())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_ADDED_TO_WISH_LIST*/
//    public void productAddedToWishListTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        builder.createWishList("skdjsidjsdkdj29j", "Loved Games");
//        builder.addProductToWishList(dummyProduct);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_ADDED_TO_WISH_LIST.getValue())
//                .putValue(builder.buildForWishList())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_REMOVED_FROM_WISH_LIST*/
//    public void productRemovedFromWishListTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        builder.createWishList("skdjsidjsdkdj29j", "Loved Games");
//        builder.addProductToWishList(dummyProduct);
//        builder.addProductToWishList(dummyProduct);
//        builder.addProductToWishList(dummyProduct);
//        builder.removeProductFromWishList(dummyProduct);
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_REMOVED_FROM_WISH_LIST.getValue())
//                .putValue(builder.buildForWishList())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*WISH_LIST_PRODUCT_ADDED_TO_CART*/
//    public void wishListProductAddedToCartTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        builder.createWishList("skdjsidjsdkdj29j", "Loved Games");
//        builder.addProductToWishList(dummyProduct);
//        builder.createCart("skdjsidjsdkdj29j");
//        builder.addWishListProductToCart();
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.WISH_LIST_PRODUCT_ADDED_TO_CART.getValue())
//                .putValue(builder.buildForWishListCart())
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_SHARED*/
//    public void productSharedTest() throws InterruptedException {
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_SHARED.getValue())
//                .putValue(ECommerceCartBuilder.instance().buildForProductShare(
//                        "email",
//                        "Hey, check out this item",
//                        "friend@gmail.com",
//                        dummyProduct
//                ))
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*CART_SHARED*/
//    public void cartSharedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        createCart(builder);
//        ECommerceProperty property = builder.buildForCartSharing(
//                "email",
//                "Hey, check out this item",
//                "friend@gmail.com"
//        );
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.CART_SHARED.getValue())
//                .putValue(property)
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//
//    @Test /*PRODUCT_REVIEWED*/
//    public void productReviewedTest() throws InterruptedException {
//        ECommerceCartBuilder builder = ECommerceCartBuilder.instance();
//        ECommerceProperty property = builder.buildForProductReview(
//                dummyProduct,
//                "kdfjrj39fj39jf3",
//                "I love this product",
//                "5"
//        );
//        RudderElement event = new RudderMessageBuilder()
//                .setChannel("Test Channel")
//                .setEvent(ECommerceEvents.PRODUCT_REVIEWED.getValue())
//                .putValue(property)
//                .build();
//        rudderClient.track(event);
//        rudderClient.flush();
//        Thread.sleep(2000);
//    }
//}
