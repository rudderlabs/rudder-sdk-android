package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCoupon;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

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
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.coupon == null) {
            throw new RudderException("Coupon can't be null");
        }
        property.setProperty(ECommerceParamNames.COUPON_ID, this.coupon.getCouponId());
        property.setProperty(ECommerceParamNames.COUPON_NAME, this.coupon.getCouponName());
        if (this.coupon.getCartId() == null && this.coupon.getOrderId() == null)
            throw new RudderException("OrderId and CartId both can't be null");
        if (this.coupon.getOrderId() != null)
            property.setProperty(ECommerceParamNames.ORDER_ID, this.coupon.getOrderId());
        if (this.coupon.getCartId() != null)
            property.setProperty(ECommerceParamNames.CART_ID, this.coupon.getCartId());
        if (this.coupon.getReason() != null)
            property.setProperty(ECommerceParamNames.REASON, this.coupon.getReason());

        return property;
    }
}
