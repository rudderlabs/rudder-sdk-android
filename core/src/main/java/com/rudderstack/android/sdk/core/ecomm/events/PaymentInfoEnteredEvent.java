package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceCheckout;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;

public class PaymentInfoEnteredEvent extends ECommercePropertyBuilder {
    private ECommerceCheckout checkout;

    public PaymentInfoEnteredEvent withCheckout(ECommerceCheckout checkout) {
        this.checkout = checkout;
        return this;
    }

    public PaymentInfoEnteredEvent withCheckoutBuilder(ECommerceCheckout.Builder builder) {
        this.checkout = builder.build();
        return this;
    }

    private String checkoutId;
    public PaymentInfoEnteredEvent withCheckoutId(String checkoutId) {
        this.checkoutId = checkoutId;
        return this;
    }

    private String orderId;
    public PaymentInfoEnteredEvent withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.PAYMENT_INFO_ENTERED;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (this.checkout != null) {
            property.put(ECommerceParamNames.CHECKOUT_ID, this.checkout.getCheckoutId());
            property.put(ECommerceParamNames.ORDER_ID, this.checkout.getOrderId());
        } else if (checkoutId != null && orderId != null) {
            property.put(ECommerceParamNames.CHECKOUT_ID, this.checkoutId);
            property.put(ECommerceParamNames.ORDER_ID, this.orderId);
        }
        return property;
    }
}
