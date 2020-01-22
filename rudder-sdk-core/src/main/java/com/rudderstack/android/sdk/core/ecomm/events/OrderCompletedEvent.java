package com.rudderstack.android.sdk.core.ecomm.events;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceOrder;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.util.Utils;

public class OrderCompletedEvent extends ECommercePropertyBuilder {
    private ECommerceOrder order;

    public OrderCompletedEvent withOrder(ECommerceOrder order) {
        this.order = order;
        return this;
    }

    public OrderCompletedEvent withOrderBuilder(ECommerceOrder.Builder builder) {
        this.order = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.ORDER_COMPLETED;
    }

    @Override
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        if (this.order != null)
            property.putValue(Utils.convertToMap(new Gson().toJson(this.order)));
        return property;
    }
}
