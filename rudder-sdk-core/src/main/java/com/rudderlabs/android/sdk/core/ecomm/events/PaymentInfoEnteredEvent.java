package com.rudderlabs.android.sdk.core.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceCheckout;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;

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
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.checkout != null) {
            property.put(ECommerceParamNames.CHECKOUT_ID, this.checkout.getCheckoutId());
            property.put(ECommerceParamNames.ORDER_ID, this.checkout.getOrderId());
        }
        return property;
    }
}
