package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceCoupon;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;

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
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.coupon != null) {
            property.put(ECommerceParamNames.COUPON_ID, this.coupon.getCouponId());
            if (this.coupon.getOrderId() != null)
                property.put(ECommerceParamNames.ORDER_ID, this.coupon.getOrderId());
            if (this.coupon.getCartId() != null)
                property.put(ECommerceParamNames.CART_ID, this.coupon.getCartId());
        }
        return property;
    }
}
