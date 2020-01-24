package com.rudderstack.android.sdk.core.ecomm.events;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceCart;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderstack.android.sdk.core.util.Utils;

public class CartViewedEvent extends ECommercePropertyBuilder {
    private ECommerceCart cart;

    public CartViewedEvent withCart(ECommerceCart cart) {
        this.cart = cart;
        return this;
    }

    public CartViewedEvent withCartBuilder(ECommerceCart.Builder builder) {
        this.cart = builder.build();
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.CART_VIEWED;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();
        property.putValue(Utils.convertToMap(new Gson().toJson(this.cart)));
        return property;
    }
}
