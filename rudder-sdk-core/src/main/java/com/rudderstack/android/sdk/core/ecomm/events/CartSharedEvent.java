package com.rudderstack.android.sdk.core.ecomm.events;

import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.ecomm.ECommerceCart;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;
import com.rudderstack.android.sdk.core.ecomm.ECommerceParamNames;
import com.rudderstack.android.sdk.core.ecomm.ECommerceProduct;
import com.rudderstack.android.sdk.core.ecomm.ECommercePropertyBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartSharedEvent extends ECommercePropertyBuilder {
    private ECommerceCart cart;

    public CartSharedEvent withCart(ECommerceCart cart) {
        this.cart = cart;
        return this;
    }

    public CartSharedEvent withCartBuilder(ECommerceCart.Builder builder) {
        this.cart = builder.build();
        return this;
    }

    private String socialChannel;

    public CartSharedEvent withSocialChannel(String socialChannel) {
        this.socialChannel = socialChannel;
        return this;
    }

    private String shareMessage;

    public CartSharedEvent withShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
        return this;
    }

    private String recipient;

    public CartSharedEvent withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public String event() {
        return ECommerceEvents.CART_SHARED;
    }

    @Override
    public RudderProperty properties() {
        RudderProperty property = new RudderProperty();

        property.put(ECommerceParamNames.CART_ID, this.cart.getCartId());
        ArrayList<Map<String, String>> productIds = new ArrayList<>();
        for (ECommerceProduct product : this.cart.getProducts()) {
            Map<String, String> productIdMap = new HashMap<>();
            productIdMap.put(ECommerceParamNames.PRODUCT_ID, product.getProductId());
            productIds.add(productIdMap);
        }
        property.put(ECommerceParamNames.PRODUCTS, productIds);
        if (this.socialChannel != null)
            property.put(ECommerceParamNames.SHARE_VIA, this.socialChannel);
        if (this.shareMessage != null)
            property.put(ECommerceParamNames.SHARE_MESSAGE, this.shareMessage);
        if (this.recipient != null)
            property.put(ECommerceParamNames.RECIPIENT, this.recipient);
        return property;
    }
}
