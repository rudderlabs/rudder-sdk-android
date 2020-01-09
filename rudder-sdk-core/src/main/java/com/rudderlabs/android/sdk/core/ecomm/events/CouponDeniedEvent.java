package com.rudderlabs.android.sdk.core.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceCoupon;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;

public class CouponDeniedEvent extends ECommercePropertyBuilder {
    private ECommerceCoupon coupon;

    public CouponDeniedEvent withCoupon(ECommerceCoupon coupon) {
        this.coupon = coupon;
        return this;
    }

    public CouponDeniedEvent withCouponBuilder(ECommerceCoupon.Builder builder) {
        this.coupon = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.COUPON_DENIED;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.coupon != null) {
            property.put(ECommerceParamNames.COUPON_ID, this.coupon.getCouponId());
            property.put(ECommerceParamNames.COUPON_NAME, this.coupon.getCouponName());
            if (this.coupon.getOrderId() != null)
                property.put(ECommerceParamNames.ORDER_ID, this.coupon.getOrderId());
            if (this.coupon.getCartId() != null)
                property.put(ECommerceParamNames.CART_ID, this.coupon.getCartId());
            if (this.coupon.getReason() != null)
                property.put(ECommerceParamNames.REASON, this.coupon.getReason());
        }
        return property;
    }
}
