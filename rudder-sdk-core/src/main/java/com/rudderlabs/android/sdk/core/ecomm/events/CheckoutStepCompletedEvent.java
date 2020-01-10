package com.rudderlabs.android.sdk.core.ecomm.events;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceCheckout;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderlabs.android.sdk.core.util.Utils;

public class CheckoutStepCompletedEvent extends ECommercePropertyBuilder {
    private ECommerceCheckout checkout;

    public CheckoutStepCompletedEvent withCheckout(ECommerceCheckout checkout) {
        this.checkout = checkout;
        return this;
    }

    public CheckoutStepCompletedEvent withCheckoutBuilder(ECommerceCheckout.Builder builder) {
        this.checkout = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.CHECKOUT_STEP_COMPLETED;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.checkout != null) {
            property.putValue(Utils.convertToMap(new Gson().toJson(this.checkout)));
        }
        return property;
    }
}
