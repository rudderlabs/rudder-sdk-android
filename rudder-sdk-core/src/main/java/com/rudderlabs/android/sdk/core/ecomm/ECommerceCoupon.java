package com.rudderlabs.android.sdk.core.ecomm;

import com.google.gson.annotations.SerializedName;

public class ECommerceCoupon {
    @SerializedName("cart_id")
    private String cartId;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("coupon_id")
    private String couponId;
    @SerializedName("coupon_name")
    private String couponName;
    @SerializedName("discount")
    private float discount;
    @SerializedName("reason")
    private String reason;

    public ECommerceCoupon(String cartId, String orderId, String couponId) {
        this.cartId = cartId;
        this.orderId = orderId;
        this.couponId = couponId;
    }

    public ECommerceCoupon(String cartId, String orderId, String couponId, String couponName, float discount, String reason) {
        this.cartId = cartId;
        this.orderId = orderId;
        this.couponId = couponId;
        this.couponName = couponName;
        this.discount = discount;
        this.reason = reason;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static class Builder {
        private String cartId;

        public Builder withCartId(String cartId) {
            this.cartId = cartId;
            return this;
        }

        private String orderId;

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        private String couponId;

        public Builder withCouponId(String couponId) {
            this.couponId = couponId;
            return this;
        }

        private String couponName;

        public Builder withCouponName(String couponName) {
            this.couponName = couponName;
            return this;
        }

        private float discount;

        public Builder withDiscount(float discount) {
            this.discount = discount;
            return this;
        }

        private String reason;

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public ECommerceCoupon build() {
            return new ECommerceCoupon(
                    cartId,
                    orderId,
                    couponId,
                    couponName,
                    discount,
                    reason
            );
        }
    }
}
