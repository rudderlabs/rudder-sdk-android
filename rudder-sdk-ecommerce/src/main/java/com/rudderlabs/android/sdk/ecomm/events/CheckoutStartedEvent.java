package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCheckout;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommerceOrder;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

public class CheckoutStartedEvent extends ECommercePropertyBuilder {
    private ECommerceOrder order;

    public CheckoutStartedEvent(String orderId) {
        this.order = new ECommerceOrder(orderId);
    }

    public CheckoutStartedEvent withOrder(ECommerceOrder order) {
        this.order = order;
        return this;
    }

    private ECommerceCheckout checkout;

    public CheckoutStartedEvent withCheckoutId(ECommerceCheckout checkout) {
        this.checkout = checkout;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.CHECKOUT_STARTED;
    }

    @Override
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();
        if (this.order == null) throw new RudderException("Order can't be null");
        property.setProperty(this.order);
        if (this.checkout == null) throw new RudderException("Checkout can't be null");
        property.setProperty(this.checkout);
        return property;
    }
}
