package com.rudderstack.android.sdk.core.ecomm.events;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceCheckout;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceOrder;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.util.Utils;

public class CheckoutStartedEvent extends ECommercePropertyBuilder {
    private ECommerceOrder order;
    public CheckoutStartedEvent withOrder(ECommerceOrder order) {
        this.order = order;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.CHECKOUT_STARTED;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.order != null) {
            property.putValue(Utils.convertToMap(new Gson().toJson(this.order)));
        }
        return property;
    }
}
