package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceCheckout;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.gson.RudderGson;
import com.rudderstack.android.sdk.core.util.Utils;

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
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        if (this.checkout != null) {
            property.putValue(Utils.convertToMap(this.checkout));
        }
        return property;
    }
}
