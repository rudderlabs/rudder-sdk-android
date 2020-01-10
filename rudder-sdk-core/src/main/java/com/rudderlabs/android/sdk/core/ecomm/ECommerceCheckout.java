package com.rudderlabs.android.sdk.core.ecomm;

import com.google.gson.annotations.SerializedName;

public class ECommerceCheckout {
    @SerializedName("checkout_id")
    private String checkoutId;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("step")
    private int step;
    @SerializedName("shipping_method")
    private String shippingMethod;
    @SerializedName("payment_method")
    private String paymentMethod;

    public ECommerceCheckout(String checkoutId, String orderId) {
        this.checkoutId = checkoutId;
        this.orderId = orderId;
    }

    public ECommerceCheckout(String checkoutId, String orderId, int step, String shippingMethod, String paymentMethod) {
        this.checkoutId = checkoutId;
        this.orderId = orderId;
        this.step = step;
        this.shippingMethod = shippingMethod;
        this.paymentMethod = paymentMethod;
    }

    public String getCheckoutId() {
        return checkoutId;
    }

    public void setCheckoutId(String checkoutId) {
        this.checkoutId = checkoutId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public static class Builder {
        private String checkoutId;

        public Builder withCheckoutId(String checkoutId) {
            this.checkoutId = checkoutId;
            return this;
        }

        private String orderId;

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        private int step;

        public Builder withStep(int step) {
            this.step = step;
            return this;
        }

        private String shippingMethod;

        public Builder withShippingMethod(String shippingMethod) {
            this.shippingMethod = shippingMethod;
            return this;
        }

        private String paymentMethod;

        public Builder withPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public ECommerceCheckout build() {
            return new ECommerceCheckout(
                    checkoutId,
                    orderId,
                    step,
                    shippingMethod,
                    paymentMethod
            );
        }
    }
}
