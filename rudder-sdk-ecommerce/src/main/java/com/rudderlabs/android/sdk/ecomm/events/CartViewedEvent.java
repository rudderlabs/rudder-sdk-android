package com.rudderlabs.android.sdk.ecomm.events;

import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.ecomm.ECommerceCart;
import com.rudderlabs.android.sdk.ecomm.ECommerceEvents;
import com.rudderlabs.android.sdk.ecomm.ECommercePropertyBuilder;

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
    public RudderProperty build() throws RudderException {
        RudderProperty property = new RudderProperty();

        if (this.cart == null) throw new RudderException("Cart can not be null");
        property.setProperty(this.cart);

        return property;
    }
}
