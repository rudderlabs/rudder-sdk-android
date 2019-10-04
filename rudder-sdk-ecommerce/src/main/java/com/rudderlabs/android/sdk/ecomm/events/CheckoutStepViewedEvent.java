package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCheckout;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

public class CheckoutStepViewedEvent extends ECommercePropertyBuilder {
    private ECommerceCheckout checkout;

    public CheckoutStepViewedEvent withCheckout(ECommerceCheckout checkout) {
        this.checkout = checkout;
        return this;
    }

    public CheckoutStepViewedEvent withCheckoutBuilder(ECommerceCheckout.Builder builder) {
        this.checkout = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.CHECKOUT_STEP_VIEWED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.checkout == null) throw new RudderException("Checkout can't be null");
        property.setProperty(this.checkout);
        return property;
    }
}
