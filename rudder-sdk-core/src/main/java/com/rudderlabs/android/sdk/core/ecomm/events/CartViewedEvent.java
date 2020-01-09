package com.rudderlabs.android.sdk.core.ecomm.events;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceCart;
import com.rudderlabs.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.core.ecomm.ECommercePropertyBuilder;
import com.rudderlabs.android.sdk.core.util.Utils;

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
    public RudderProperty build() {
        RudderProperty property = new RudderProperty();
        property.putValue(Utils.convertToMap(new Gson().toJson(this.cart)));
        return property;
    }
}
