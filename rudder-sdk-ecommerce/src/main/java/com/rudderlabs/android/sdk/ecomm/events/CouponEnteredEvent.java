package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCoupon;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

public class CouponEnteredEvent extends ECommercePropertyBuilder {
    private ECommerceCoupon coupon;

    public CouponEnteredEvent withCoupon(ECommerceCoupon coupon) {
        this.coupon = coupon;
        return this;
    }

    public CouponEnteredEvent withCouponBuilder(ECommerceCoupon.Builder builder) {
        this.coupon = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.COUPON_ENTERED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();

        if (this.coupon == null) throw new RudderException("Coupon can't be null");

        property.setProperty(ECommerceParamNames.COUPON_ID, this.coupon.getCouponId());
        if (this.coupon.getCartId() == null && this.coupon.getOrderId() == null)
            throw new RudderException("OrderId and CartId both can't be null");
        if (this.coupon.getOrderId() != null)
            property.setProperty(ECommerceParamNames.ORDER_ID, this.coupon.getOrderId());
        if (this.coupon.getCartId() != null)
            property.setProperty(ECommerceParamNames.CART_ID, this.coupon.getCartId());

        return property;
    }
}
