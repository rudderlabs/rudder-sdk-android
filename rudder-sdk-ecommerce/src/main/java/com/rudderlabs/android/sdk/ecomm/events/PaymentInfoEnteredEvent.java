package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCheckout;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

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

    @Override
    public String event() {
        return ECommerceEvents.PAYMENT_INFO_ENTERED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        if (this.checkout == null) throw new RudderException("Checkout can't be null");
        RudderProperty property = new RudderProperty();
        property.setProperty(ECommerceParamNames.CHECKOUT_ID, this.checkout.getCheckoutId());
        property.setProperty(ECommerceParamNames.ORDER_ID, this.checkout.getOrderId());
        return property;
    }
}
